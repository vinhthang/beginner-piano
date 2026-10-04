package com.beginnerpiano.practice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beginnerpiano.audio.AudioCaptureService
import com.beginnerpiano.audio.DetectedPitch
import com.beginnerpiano.audio.PianoAudioSynthesizer
import com.beginnerpiano.audio.PitchDetector
import com.beginnerpiano.audio.PitchDetectorType
import com.beginnerpiano.audio.SpicePitchDetector
import com.beginnerpiano.audio.YinPitchDetector
import com.beginnerpiano.data.models.NotationSystem
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.data.models.Song
import com.beginnerpiano.data.repository.SongRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticeViewModel(
    initialSong: Song = SongRepository.songs.first(),
    private val audioService: AudioCaptureService = AudioCaptureService(),
    private val pianoSynthesizer: PianoAudioSynthesizer = PianoAudioSynthesizer(),
    context: Context? = null
) : ViewModel() {

    private val yinDetector = YinPitchDetector()
    private var spiceDetector = SpicePitchDetector(context)

    constructor(context: Context?) : this(SongRepository.songs.first(), AudioCaptureService(), PianoAudioSynthesizer(), context)

    private val engine = PracticeEngine(initialSong)

    private val _practiceState = MutableStateFlow(engine.state)
    val practiceState: StateFlow<PracticeState> = _practiceState.asStateFlow()

    val detectedPitch: StateFlow<DetectedPitch> = audioService.pitchFlow
    val isRecording: StateFlow<Boolean> = audioService.isRecording

    private var demoJob: Job? = null

    init {
        // Collect real-time pitch detections from microphone
        viewModelScope.launch {
            audioService.pitchFlow.collect { pitch ->
                if (!pitch.isSilent && !_practiceState.value.isCompleted && !_practiceState.value.isPlayingDemo) {
                    val status = engine.onPitchDetected(pitch)
                    _practiceState.value = engine.state
                    if (status != NoteFeedbackStatus.IDLE) {
                        delay(350)
                        engine.clearFeedbackStatus()
                        _practiceState.value = engine.state
                    }
                }
            }
        }
    }

    fun initContext(context: Context) {
        spiceDetector = SpicePitchDetector(context.applicationContext)
        if (engine.state.pitchDetectorType == PitchDetectorType.SPICE) {
            audioService.setPitchDetector(spiceDetector)
        }
    }

    fun setPitchDetectorType(type: PitchDetectorType) {
        engine.setPitchDetectorType(type)
        _practiceState.value = engine.state
        val detector: PitchDetector = when (type) {
            PitchDetectorType.YIN -> yinDetector
            PitchDetectorType.SPICE -> spiceDetector
        }
        audioService.setPitchDetector(detector)
    }

    fun togglePitchDetectorType() {
        val nextType = if (engine.state.pitchDetectorType == PitchDetectorType.YIN) {
            PitchDetectorType.SPICE
        } else {
            PitchDetectorType.YIN
        }
        setPitchDetectorType(nextType)
    }

    fun startListening() {
        audioService.startCapture(viewModelScope)
    }

    fun stopListening() {
        audioService.stopCapture()
    }

    fun selectSong(song: Song) {
        if (_practiceState.value.isPlayingDemo) {
            stopDemoPlayback()
        }
        engine.setSong(song)
        _practiceState.value = engine.state
    }

    fun setNotationSystem(notation: NotationSystem) {
        engine.setNotationSystem(notation)
        _practiceState.value = engine.state
    }

    fun setMode(mode: PracticeMode) {
        engine.setMode(mode)
        _practiceState.value = engine.state
    }

    fun toggleMode() {
        val nextMode = if (engine.state.mode == PracticeMode.WAIT_FOR_NOTE) {
            PracticeMode.TEMPO
        } else {
            PracticeMode.WAIT_FOR_NOTE
        }
        setMode(nextMode)
    }

    fun restart() {
        if (_practiceState.value.isPlayingDemo) {
            stopDemoPlayback()
        }
        engine.reset()
        _practiceState.value = engine.state
    }

    fun toggleDemoPlayback() {
        if (_practiceState.value.isPlayingDemo) {
            stopDemoPlayback()
        } else {
            startDemoPlayback()
        }
    }

    fun startDemoPlayback() {
        if (_practiceState.value.isPlayingDemo) return

        // Pause mic capture so speaker sound is not picked up as user mic input
        audioService.stopCapture()

        if (_practiceState.value.isCompleted || _practiceState.value.currentNoteIndex >= _practiceState.value.song.notes.size) {
            engine.reset()
        }

        engine.setPlayingDemo(true)
        _practiceState.value = engine.state

        demoJob = viewModelScope.launch {
            val notes = _practiceState.value.song.notes
            val startIndex = _practiceState.value.currentNoteIndex

            for (i in startIndex until notes.size) {
                val note = notes[i]
                _practiceState.value = _practiceState.value.copy(
                    currentNoteIndex = i,
                    feedbackStatus = NoteFeedbackStatus.HIT,
                    lastDetectedMidi = note.midiNote,
                    lastDetectedName = NoteEvent.midiToNoteName(note.midiNote, _practiceState.value.notationSystem)
                )

                val bpm = _practiceState.value.song.defaultBpm.coerceAtLeast(30)
                val noteDurationMs = ((60_000.0 / bpm) * note.durationBeats).toLong().coerceAtLeast(200L)
                pianoSynthesizer.playNote(note.midiNote, (noteDurationMs * 0.85).toLong())
                delay(noteDurationMs)
            }

            engine.setPlayingDemo(false)
            _practiceState.value = _practiceState.value.copy(
                currentNoteIndex = notes.size,
                feedbackStatus = NoteFeedbackStatus.IDLE,
                isPlayingDemo = false,
                isCompleted = true
            )
            audioService.startCapture(viewModelScope)
        }
    }

    fun stopDemoPlayback() {
        demoJob?.cancel()
        demoJob = null
        pianoSynthesizer.stop()
        engine.setPlayingDemo(false)
        _practiceState.value = _practiceState.value.copy(
            isPlayingDemo = false,
            feedbackStatus = NoteFeedbackStatus.IDLE
        )
        audioService.startCapture(viewModelScope)
    }

    fun onKeyTapped(midiNote: Int) {
        pianoSynthesizer.playNote(midiNote, 350L)
        if (_practiceState.value.isPlayingDemo) {
            return
        }
        val status = engine.onKeyTapped(midiNote)
        _practiceState.value = engine.state
        viewModelScope.launch {
            if (status != NoteFeedbackStatus.IDLE) {
                delay(300)
                engine.clearFeedbackStatus()
                _practiceState.value = engine.state
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopDemoPlayback()
        pianoSynthesizer.release()
        audioService.stopCapture()
    }
}

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
import com.beginnerpiano.data.models.SongMastery
import com.beginnerpiano.data.repository.CustomSongRepository
import com.beginnerpiano.data.repository.SongProgressRepository
import com.beginnerpiano.data.repository.SongRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream

class PracticeViewModel(
    initialSong: Song = SongRepository.songs.first(),
    private val audioService: AudioCaptureService = AudioCaptureService(),
    private val pianoSynthesizer: PianoAudioSynthesizer = PianoAudioSynthesizer(),
    context: Context? = null
) : ViewModel() {

    private val yinDetector = YinPitchDetector()
    private var spiceDetector = SpicePitchDetector(context)

    private var progressRepo = SongProgressRepository(context)
    private var customSongRepo = CustomSongRepository(context)

    private val _masteryMap = MutableStateFlow(progressRepo.getAllMastery())
    val masteryMap: StateFlow<Map<String, SongMastery>> = _masteryMap.asStateFlow()

    private val _allSongs = MutableStateFlow(customSongRepo.getAllSongs())
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow()

    constructor(context: Context?) : this(SongRepository.songs.first(), AudioCaptureService(), PianoAudioSynthesizer(), context)

    private val engine = PracticeEngine(initialSong)

    private val _practiceState = MutableStateFlow(engine.state)
    val practiceState: StateFlow<PracticeState> = _practiceState.asStateFlow()

    val detectedPitch: StateFlow<DetectedPitch> = audioService.pitchFlow
    val isRecording: StateFlow<Boolean> = audioService.isRecording

    private var demoJob: Job? = null
    private var tempoJob: Job? = null

    init {
        // Collect real-time pitch detections from microphone
        viewModelScope.launch {
            audioService.pitchFlow.collect { pitch ->
                if (!pitch.isSilent && !_practiceState.value.isCompleted && !_practiceState.value.isPlayingDemo) {
                    val status = engine.onPitchDetected(pitch)
                    _practiceState.value = engine.state
                    checkSongCompletion()
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
        val appCtx = context.applicationContext
        spiceDetector = SpicePitchDetector(appCtx)
        if (engine.state.pitchDetectorType == PitchDetectorType.SPICE) {
            audioService.setPitchDetector(spiceDetector)
        }
        progressRepo = SongProgressRepository(appCtx)
        customSongRepo = CustomSongRepository(appCtx)
        _masteryMap.value = progressRepo.getAllMastery()
        _allSongs.value = customSongRepo.getAllSongs()
    }

    private fun checkSongCompletion() {
        if (engine.state.isCompleted && !_practiceState.value.isPlayingDemo) {
            progressRepo.recordCompletion(
                songId = engine.state.song.id,
                accuracyPercent = engine.state.accuracyPercent,
                streak = engine.state.bestStreak
            )
            _masteryMap.value = progressRepo.getAllMastery()
            stopTempoClock()
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
        stopDemoPlayback()
        stopTempoClock()
        engine.setSong(song)
        _practiceState.value = engine.state
    }

    fun setNotationSystem(notation: NotationSystem) {
        engine.setNotationSystem(notation)
        _practiceState.value = engine.state
    }

    fun setMode(mode: PracticeMode) {
        stopTempoClock()
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

    fun setTempoMultiplier(multiplier: Float) {
        engine.setTempoMultiplier(multiplier)
        _practiceState.value = engine.state
    }

    fun cycleTempoMultiplier() {
        val current = engine.state.tempoMultiplier
        val next = when {
            current < 0.70f -> 0.75f
            current < 0.95f -> 1.0f
            current < 1.20f -> 1.25f
            else -> 0.5f
        }
        setTempoMultiplier(next)
    }

    fun onNoteTappedForLoop(noteIndex: Int) {
        engine.onNoteTappedForLoop(noteIndex)
        _practiceState.value = engine.state
    }

    fun toggleLoopRange() {
        if (engine.state.isLoopActive) {
            engine.clearLoop()
        } else {
            val start = engine.state.currentNoteIndex.coerceIn(0, engine.state.song.notes.lastIndex)
            val end = minOf(start + 3, engine.state.song.notes.lastIndex)
            engine.setLoopRange(start, end)
        }
        _practiceState.value = engine.state
    }

    fun clearLoop() {
        engine.clearLoop()
        _practiceState.value = engine.state
    }

    fun setMetronomeEnabled(enabled: Boolean) {
        engine.setMetronomeEnabled(enabled)
        _practiceState.value = engine.state
    }

    fun toggleMetronome() {
        setMetronomeEnabled(!engine.state.metronomeEnabled)
    }

    fun toggleTempoClock() {
        if (engine.state.isTempoRunning) {
            stopTempoClock()
        } else {
            startTempoClock()
        }
    }

    fun startTempoClock() {
        if (engine.state.isTempoRunning) return
        stopDemoPlayback()

        if (_practiceState.value.isCompleted) {
            restart()
        }
        engine.setTempoRunning(true)
        _practiceState.value = engine.state

        tempoJob?.cancel()
        tempoJob = viewModelScope.launch {
            var beatCounter = 0
            while (engine.state.isTempoRunning && !engine.state.isCompleted) {
                val currentTarget = engine.state.currentTargetNote ?: break
                val effectiveBpm = engine.state.effectiveBpm
                val beatDurationMs = (60_000.0 / effectiveBpm).toLong().coerceAtLeast(100L)
                val isDownbeat = (beatCounter % 4 == 0)

                if (engine.state.metronomeEnabled) {
                    pianoSynthesizer.playMetronomeClick(isDownbeat = isDownbeat)
                }
                beatCounter++

                val noteDurationMs = (beatDurationMs * currentTarget.durationBeats).toLong().coerceAtLeast(150L)
                val targetIndexBefore = engine.state.currentNoteIndex

                delay(noteDurationMs)

                if (!engine.state.isTempoRunning || engine.state.isCompleted) break

                if (engine.state.currentNoteIndex == targetIndexBefore) {
                    val status = engine.onBeatTimeout()
                    _practiceState.value = engine.state
                    checkSongCompletion()
                    if (status != NoteFeedbackStatus.IDLE) {
                        delay(200L)
                        engine.clearFeedbackStatus()
                        _practiceState.value = engine.state
                    }
                }
            }
            stopTempoClock()
        }
    }

    fun stopTempoClock() {
        tempoJob?.cancel()
        tempoJob = null
        if (engine.state.isTempoRunning) {
            engine.setTempoRunning(false)
            _practiceState.value = engine.state
        }
    }

    fun importMidiSong(inputStream: InputStream, fileName: String? = null): Song? {
        return try {
            val imported = customSongRepo.importMidi(inputStream, fileName)
            _allSongs.value = customSongRepo.getAllSongs()
            imported
        } catch (e: Throwable) {
            null
        }
    }

    fun restart() {
        stopDemoPlayback()
        stopTempoClock()
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
        stopTempoClock()

        // Pause mic capture so speaker sound is not picked up as user mic input
        audioService.stopCapture()

        val isLoop = _practiceState.value.isLoopActive
        if (_practiceState.value.isCompleted || _practiceState.value.currentNoteIndex >= _practiceState.value.song.notes.size) {
            engine.reset()
        }

        engine.setPlayingDemo(true)
        _practiceState.value = engine.state

        demoJob = viewModelScope.launch {
            val notes = _practiceState.value.song.notes
            val startIdx = if (isLoop) (_practiceState.value.loopStartIndex ?: 0) else _practiceState.value.currentNoteIndex
            val endIdx = if (isLoop) (_practiceState.value.loopEndIndex ?: notes.lastIndex) else notes.lastIndex

            var i = startIdx
            while (_practiceState.value.isPlayingDemo && i in notes.indices) {
                val note = notes[i]
                _practiceState.value = _practiceState.value.copy(
                    currentNoteIndex = i,
                    feedbackStatus = NoteFeedbackStatus.HIT,
                    lastDetectedMidi = note.midiNote,
                    lastDetectedName = NoteEvent.midiToNoteName(note.midiNote, _practiceState.value.notationSystem)
                )

                val bpm = _practiceState.value.effectiveBpm.coerceAtLeast(20)
                val noteDurationMs = ((60_000.0 / bpm) * note.durationBeats).toLong().coerceAtLeast(150L)
                pianoSynthesizer.playNote(note.midiNote, (noteDurationMs * 0.85).toLong())
                delay(noteDurationMs)

                if (!_practiceState.value.isPlayingDemo) break

                if (isLoop && i >= endIdx) {
                    i = startIdx
                } else if (!isLoop && i >= notes.lastIndex) {
                    break
                } else {
                    i++
                }
            }

            engine.setPlayingDemo(false)
            _practiceState.value = _practiceState.value.copy(
                currentNoteIndex = if (isLoop) startIdx else notes.size,
                feedbackStatus = NoteFeedbackStatus.IDLE,
                isPlayingDemo = false,
                isCompleted = !isLoop
            )
            audioService.startCapture(viewModelScope)
        }
    }

    fun stopDemoPlayback() {
        demoJob?.cancel()
        demoJob = null
        pianoSynthesizer.stop()
        if (engine.state.isPlayingDemo) {
            engine.setPlayingDemo(false)
            _practiceState.value = _practiceState.value.copy(
                isPlayingDemo = false,
                feedbackStatus = NoteFeedbackStatus.IDLE
            )
        }
        audioService.startCapture(viewModelScope)
    }

    fun onKeyTapped(midiNote: Int) {
        pianoSynthesizer.playNote(midiNote, 350L)
        if (_practiceState.value.isPlayingDemo) {
            return
        }
        val status = engine.onKeyTapped(midiNote)
        _practiceState.value = engine.state
        checkSongCompletion()
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
        stopTempoClock()
        pianoSynthesizer.release()
        audioService.stopCapture()
    }
}

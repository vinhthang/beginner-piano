package com.beginnerpiano.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beginnerpiano.audio.AudioCaptureService
import com.beginnerpiano.audio.DetectedPitch
import com.beginnerpiano.data.models.Song
import com.beginnerpiano.data.repository.SongRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticeViewModel(
    initialSong: Song = SongRepository.songs.first(),
    private val audioService: AudioCaptureService = AudioCaptureService()
) : ViewModel() {

    private val engine = PracticeEngine(initialSong)

    private val _practiceState = MutableStateFlow(engine.state)
    val practiceState: StateFlow<PracticeState> = _practiceState.asStateFlow()

    val detectedPitch: StateFlow<DetectedPitch> = audioService.pitchFlow
    val isRecording: StateFlow<Boolean> = audioService.isRecording

    init {
        // Collect real-time pitch detections from microphone
        viewModelScope.launch {
            audioService.pitchFlow.collect { pitch ->
                if (!pitch.isSilent && !_practiceState.value.isCompleted) {
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

    fun startListening() {
        audioService.startCapture(viewModelScope)
    }

    fun stopListening() {
        audioService.stopCapture()
    }

    fun selectSong(song: Song) {
        engine.setSong(song)
        _practiceState.value = engine.state
    }

    fun toggleMode() {
        val nextMode = if (engine.state.mode == PracticeMode.WAIT_FOR_NOTE) {
            PracticeMode.TEMPO
        } else {
            PracticeMode.WAIT_FOR_NOTE
        }
        engine.setMode(nextMode)
        _practiceState.value = engine.state
    }

    fun restart() {
        engine.reset()
        _practiceState.value = engine.state
    }

    fun onKeyTapped(midiNote: Int) {
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
        audioService.stopCapture()
    }
}

package com.beginnerpiano.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioCaptureService(
    private val pitchDetector: PitchDetector = YinPitchDetector(),
    private val sampleRate: Int = 44100,
    private val bufferSizeSamples: Int = 2048
) {
    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null
    private val _pitchFlow = MutableStateFlow(DetectedPitch.SILENCE)
    val pitchFlow: StateFlow<DetectedPitch> = _pitchFlow.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startCapture(scope: CoroutineScope) {
        if (_isRecording.value) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize, bufferSizeSamples * 2)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            _isRecording.value = true

            captureJob = scope.launch(Dispatchers.Default) {
                val shortBuffer = ShortArray(bufferSizeSamples)
                val floatBuffer = FloatArray(bufferSizeSamples)

                while (isActive && _isRecording.value) {
                    val readSamples = audioRecord?.read(shortBuffer, 0, bufferSizeSamples) ?: -1
                    if (readSamples > 0) {
                        for (i in 0 until readSamples) {
                            floatBuffer[i] = shortBuffer[i] / 32768.0f
                        }
                        val pitch = pitchDetector.detectPitch(floatBuffer, sampleRate)
                        _pitchFlow.value = pitch
                    }
                }
            }
        } catch (e: SecurityException) {
            _isRecording.value = false
        } catch (e: Exception) {
            _isRecording.value = false
        }
    }

    fun stopCapture() {
        _isRecording.value = false
        captureJob?.cancel()
        captureJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore cleanup exceptions
        }
        audioRecord = null
        _pitchFlow.value = DetectedPitch.SILENCE
    }
}

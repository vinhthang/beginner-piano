package com.beginnerpiano.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Real-time acoustic piano audio synthesizer using additive harmonic synthesis and AudioTrack.
 */
class PianoAudioSynthesizer(
    private val sampleRate: Int = 44100
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentTrack: AudioTrack? = null
    private val trackLock = Any()

    /**
     * Plays a piano tone for [midiNote] asynchronously for [durationMs].
     */
    fun playNote(midiNote: Int, durationMs: Long) {
        scope.launch {
            try {
                val samples = generatePcmSamples(midiNote, durationMs, sampleRate)
                if (samples.isEmpty()) return@launch

                stop()

                val bufferSize = samples.size * 2 // 2 bytes per 16-bit PCM sample
                val track = try {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()
                } catch (e: Throwable) {
                    null
                } ?: return@launch

                synchronized(trackLock) {
                    currentTrack = track
                }

                track.write(samples, 0, samples.size)
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.play()
                }

                delay(durationMs + 60L)
                synchronized(trackLock) {
                    if (currentTrack == track) {
                        currentTrack = null
                    }
                }
                try {
                    track.stop()
                    track.release()
                } catch (e: Throwable) {
                    // Ignore release errors
                }
            } catch (e: Throwable) {
                // Graceful fallback when AudioTrack is not available
            }
        }
    }

    /**
     * Stops active audio playback immediately.
     */
    fun stop() {
        synchronized(trackLock) {
            try {
                currentTrack?.let { track ->
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.stop()
                    }
                    track.release()
                }
            } catch (e: Throwable) {
                // Ignore cleanup errors
            } finally {
                currentTrack = null
            }
        }
    }

    /**
     * Releases synthesizer resources and cancels coroutines.
     */
    fun release() {
        stop()
        scope.cancel()
    }

    companion object {
        /**
         * Calculates fundamental frequency f0 for standard MIDI note (A4 = 69 -> 440.0 Hz).
         */
        fun midiToFrequency(midiNote: Int): Double {
            return 440.0 * 2.0.pow((midiNote - 69).toDouble() / 12.0)
        }

        /**
         * Pure function generating 16-bit PCM mono samples with additive harmonic piano synthesis
         * and ADSR envelope.
         */
        fun generatePcmSamples(
            midiNote: Int,
            durationMs: Long,
            sampleRate: Int = 44100
        ): ShortArray {
            if (durationMs <= 0) return ShortArray(0)
            val numSamples = ((sampleRate.toDouble() * durationMs) / 1000.0).toInt()
            if (numSamples <= 0) return ShortArray(0)

            val f0 = midiToFrequency(midiNote)
            val samples = ShortArray(numSamples)

            val attackMs = 8.0
            val attackSamples = ((sampleRate * attackMs) / 1000.0).toInt().coerceAtLeast(1)
            val releaseMs = 12.0
            val releaseSamples = ((sampleRate * releaseMs) / 1000.0).toInt().coerceIn(1, maxOf(1, numSamples / 4))

            // Peak amplitude comfortably within 16-bit signed range (-32768 to 32767)
            val peakAmplitude = 24000.0
            val totalHarmonicWeight = 1.0 + 0.5 + 0.25 + 0.125 // 1.875

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate

                // Additive harmonics with physically-inspired faster overtone decay
                val h0 = 1.0 * sin(2.0 * PI * f0 * t) * exp(-2.5 * t)
                val h1 = 0.5 * sin(2.0 * PI * (2.0 * f0) * t) * exp(-4.0 * t)
                val h2 = 0.25 * sin(2.0 * PI * (3.0 * f0) * t) * exp(-6.0 * t)
                val h3 = 0.125 * sin(2.0 * PI * (4.0 * f0) * t) * exp(-8.0 * t)

                val rawHarmonicSum = (h0 + h1 + h2 + h3) / totalHarmonicWeight

                // ADSR envelope: smooth attack
                val attack = if (i < attackSamples) {
                    i.toDouble() / attackSamples
                } else {
                    1.0
                }

                // Smooth release to eliminate ending pop/click
                val release = if (i >= numSamples - releaseSamples) {
                    (numSamples - 1 - i).toDouble() / releaseSamples
                } else {
                    1.0
                }

                val finalSample = (rawHarmonicSum * attack * release * peakAmplitude)
                    .roundToInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

                samples[i] = finalSample.toShort()
            }

            return samples
        }
    }
}

package com.beginnerpiano.audio

import com.beginnerpiano.data.models.NoteEvent
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.math.sqrt

class YinPitchDetector(
    private val threshold: Double = 0.15,
    private val minFrequency: Double = 50.0,   // Low bass (~G1)
    private val maxFrequency: Double = 2100.0, // High treble (~C7)
    private val rmsNoiseFloor: Double = 0.015
) : PitchDetector {

    override val name: String = "YIN (DSP)"
    override val type: PitchDetectorType = PitchDetectorType.YIN

    override fun detectPitch(buffer: FloatArray, sampleRate: Int): DetectedPitch {
        val bufferSize = buffer.size
        if (bufferSize < 512) return DetectedPitch.SILENCE

        // Step 1: RMS Energy Gate
        var sumSquares = 0.0
        for (i in 0 until bufferSize) {
            sumSquares += buffer[i] * buffer[i]
        }
        val rms = sqrt(sumSquares / bufferSize)
        if (rms < rmsNoiseFloor) {
            return DetectedPitch.SILENCE
        }

        val halfBufferSize = bufferSize / 2
        val tauMin = (sampleRate / maxFrequency).toInt().coerceAtLeast(2)
        val tauMax = (sampleRate / minFrequency).toInt().coerceAtMost(halfBufferSize - 1)

        if (tauMin >= tauMax) return DetectedPitch.SILENCE

        // Step 2: Difference Function
        val diff = DoubleArray(halfBufferSize)
        for (tau in 0 until halfBufferSize) {
            var sum = 0.0
            for (j in 0 until halfBufferSize) {
                val delta = buffer[j] - buffer[j + tau]
                sum += delta * delta
            }
            diff[tau] = sum
        }

        // Step 3: Cumulative Mean Normalized Difference Function (CMNDF)
        val cmndf = DoubleArray(halfBufferSize)
        cmndf[0] = 1.0
        var runningSum = 0.0
        for (tau in 1 until halfBufferSize) {
            runningSum += diff[tau]
            cmndf[tau] = if (runningSum > 0.0) {
                diff[tau] / (runningSum / tau)
            } else {
                1.0
            }
        }

        // Step 4: Absolute Thresholding
        var tauEstimate = -1
        for (tau in tauMin until tauMax) {
            if (cmndf[tau] < threshold) {
                // Find local minimum
                var localMinTau = tau
                while (localMinTau + 1 < tauMax && cmndf[localMinTau + 1] < cmndf[localMinTau]) {
                    localMinTau++
                }
                tauEstimate = localMinTau
                break
            }
        }

        // Fallback: Global minimum if no point fell below threshold
        if (tauEstimate == -1) {
            var minVal = Double.MAX_VALUE
            for (tau in tauMin until tauMax) {
                if (cmndf[tau] < minVal) {
                    minVal = cmndf[tau]
                    tauEstimate = tau
                }
            }
            if (minVal > 0.5) {
                return DetectedPitch.SILENCE
            }
        }

        // Step 5: Parabolic Interpolation for Sub-Bin Precision
        val refinedTau: Double = if (tauEstimate > 0 && tauEstimate < halfBufferSize - 1) {
            val s0 = cmndf[tauEstimate - 1]
            val s1 = cmndf[tauEstimate]
            val s2 = cmndf[tauEstimate + 1]
            val denominator = 2.0 * (2.0 * s1 - s0 - s2)
            if (abs(denominator) > 1e-6) {
                val delta = (s2 - s0) / denominator
                tauEstimate + delta
            } else {
                tauEstimate.toDouble()
            }
        } else {
            tauEstimate.toDouble()
        }

        if (refinedTau <= 0.0) return DetectedPitch.SILENCE

        val frequency = sampleRate / refinedTau
        if (frequency < minFrequency || frequency > maxFrequency) {
            return DetectedPitch.SILENCE
        }

        // Step 6: Convert to MIDI Note & Cents Offset
        val exactMidi = NoteEvent.frequencyToMidi(frequency)
        val roundedMidi = exactMidi.roundToInt()
        val centsOffset = (exactMidi - roundedMidi) * 100.0
        val certainty = (1.0 - cmndf[tauEstimate].coerceIn(0.0, 1.0))

        return DetectedPitch(
            frequencyHz = frequency,
            midiNote = roundedMidi,
            centsOffset = centsOffset,
            certainty = certainty,
            isSilent = false
        )
    }
}

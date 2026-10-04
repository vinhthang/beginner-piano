package com.beginnerpiano.audio

import android.content.Context
import com.beginnerpiano.data.models.NoteEvent
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SpicePitchDetector(
    context: Context? = null,
    private val rmsNoiseFloor: Double = 0.015
) : PitchDetector {

    override val name: String = "SPICE (AI)"
    override val type: PitchDetectorType = PitchDetectorType.SPICE

    private var interpreter: Interpreter? = null

    init {
        if (context != null) {
            try {
                val modelBuffer = loadModelFile(context)
                if (modelBuffer != null) {
                    val options = Interpreter.Options()
                    interpreter = Interpreter(modelBuffer, options)
                }
            } catch (e: Throwable) {
                interpreter = null
            }
        }
    }

    private fun loadModelFile(context: Context): MappedByteBuffer? {
        return try {
            val fileDescriptor = context.assets.openFd("lite-model_spice_1.tflite")
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Throwable) {
            null
        }
    }

    @Synchronized
    override fun detectPitch(buffer: FloatArray, sampleRate: Int): DetectedPitch {
        if (buffer.size < 512 || interpreter == null) {
            return DetectedPitch.SILENCE
        }

        // Step 1: Check RMS Energy Gate
        var sumSquares = 0.0
        for (i in buffer.indices) {
            sumSquares += buffer[i] * buffer[i]
        }
        val rms = sqrt(sumSquares / buffer.size)
        if (rms < rmsNoiseFloor) {
            return DetectedPitch.SILENCE
        }

        // Step 2: Run TFLite inference
        val outputSize = Math.ceil(buffer.size / 512.0).toInt().coerceAtLeast(1)
        val pitches = FloatArray(outputSize)
        val uncertainties = FloatArray(outputSize)
        val outputs = mutableMapOf<Int, Any>(0 to pitches, 1 to uncertainties)

        try {
            interpreter?.runForMultipleInputsOutputs(arrayOf(buffer), outputs)
        } catch (e: Throwable) {
            return DetectedPitch.SILENCE
        }

        // Step 3: Extract highest confidence frame
        var maxConfidence = 0.0
        var bestPitchOutput = 0f

        for (i in 0 until outputSize) {
            val confidence = uncertaintyToConfidence(uncertainties[i])
            if (confidence > maxConfidence) {
                maxConfidence = confidence
                bestPitchOutput = pitches[i]
            }
        }

        if (maxConfidence >= CONFIDENCE_THRESHOLD) {
            val frequency = outputToFrequencyHz(bestPitchOutput)
            if (frequency < 20.0 || frequency > 4200.0) {
                return DetectedPitch.SILENCE
            }

            val exactMidi = NoteEvent.frequencyToMidi(frequency)
            val roundedMidi = exactMidi.roundToInt()
            val centsOffset = (exactMidi - roundedMidi) * 100.0

            return DetectedPitch(
                frequencyHz = frequency,
                midiNote = roundedMidi,
                centsOffset = centsOffset,
                certainty = maxConfidence,
                isSilent = false
            )
        }

        return DetectedPitch.SILENCE
    }

    fun close() {
        try {
            interpreter?.close()
        } catch (e: Throwable) {
            // Ignore close exceptions
        }
        interpreter = null
    }

    companion object {
        const val PT_OFFSET = 25.58
        const val PT_SLOPE = 63.07
        const val FMIN = 10.0
        const val BINS_PER_OCTAVE = 12.0
        const val CONFIDENCE_THRESHOLD = 0.80

        fun outputToFrequencyHz(pitchOutput: Float): Double {
            val cqtBin = pitchOutput * PT_SLOPE + PT_OFFSET
            return FMIN * 2.0.pow(cqtBin / BINS_PER_OCTAVE)
        }

        fun uncertaintyToConfidence(uncertainty: Float): Double {
            return (1.0 - uncertainty.toDouble()).coerceIn(0.0, 1.0)
        }
    }
}

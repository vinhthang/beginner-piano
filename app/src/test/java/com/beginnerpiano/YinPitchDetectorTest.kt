package com.beginnerpiano

import com.beginnerpiano.audio.YinPitchDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class YinPitchDetectorTest {

    private val sampleRate = 44100
    private val bufferSize = 2048
    private val detector = YinPitchDetector()

    private fun generateSineWave(frequency: Double, durationSamples: Int): FloatArray {
        val buffer = FloatArray(durationSamples)
        for (i in 0 until durationSamples) {
            val angle = 2.0 * PI * frequency * i / sampleRate
            buffer[i] = (sin(angle) * 0.8).toFloat()
        }
        return buffer
    }

    @Test
    fun testDetectA4_440Hz() {
        val buffer = generateSineWave(440.0, bufferSize)
        val result = detector.detectPitch(buffer, sampleRate)

        assertFalse("Expected sound, got silence", result.isSilent)
        assertEquals(69, result.midiNote) // A4 = MIDI 69
        assertTrue("Frequency deviation too high: ${result.frequencyHz}", abs(result.frequencyHz - 440.0) < 3.0)
        assertTrue("Cents offset too high: ${result.centsOffset}", abs(result.centsOffset) < 15.0)
    }

    @Test
    fun testDetectMiddleC_261Hz() {
        val buffer = generateSineWave(261.63, bufferSize)
        val result = detector.detectPitch(buffer, sampleRate)

        assertFalse("Expected sound, got silence", result.isSilent)
        assertEquals(60, result.midiNote) // C4 = MIDI 60
        assertTrue("Frequency deviation too high: ${result.frequencyHz}", abs(result.frequencyHz - 261.63) < 2.0)
    }

    @Test
    fun testDetectE4_329Hz() {
        val buffer = generateSineWave(329.63, bufferSize)
        val result = detector.detectPitch(buffer, sampleRate)

        assertFalse("Expected sound, got silence", result.isSilent)
        assertEquals(64, result.midiNote) // E4 = MIDI 64
        assertTrue("Frequency deviation too high: ${result.frequencyHz}", abs(result.frequencyHz - 329.63) < 2.5)
    }

    @Test
    fun testSilenceRejection() {
        val silentBuffer = FloatArray(bufferSize) { 0.001f }
        val result = detector.detectPitch(silentBuffer, sampleRate)

        assertTrue("Expected silence for low-energy buffer", result.isSilent)
        assertEquals(-1, result.midiNote)
    }
}

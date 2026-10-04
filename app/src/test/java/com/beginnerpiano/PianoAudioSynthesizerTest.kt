package com.beginnerpiano

import com.beginnerpiano.audio.PianoAudioSynthesizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PianoAudioSynthesizerTest {

    @Test
    fun testFrequencyCalculation() {
        val a4 = PianoAudioSynthesizer.midiToFrequency(69)
        assertEquals(440.0, a4, 0.01)

        val c4 = PianoAudioSynthesizer.midiToFrequency(60)
        assertEquals(261.63, c4, 0.05)

        val a5 = PianoAudioSynthesizer.midiToFrequency(81)
        assertEquals(880.0, a5, 0.01)
    }

    @Test
    fun testSampleCountForDuration() {
        val sampleRate = 44100
        val durationMs = 1000L
        val samples = PianoAudioSynthesizer.generatePcmSamples(69, durationMs, sampleRate)
        assertEquals(44100, samples.size)

        val halfSecSamples = PianoAudioSynthesizer.generatePcmSamples(60, 500L, sampleRate)
        assertEquals(22050, halfSecSamples.size)
    }

    @Test
    fun testZeroOrNegativeDuration() {
        val zeroSamples = PianoAudioSynthesizer.generatePcmSamples(69, 0L)
        assertEquals(0, zeroSamples.size)

        val negativeSamples = PianoAudioSynthesizer.generatePcmSamples(69, -150L)
        assertEquals(0, negativeSamples.size)
    }

    @Test
    fun testSampleAmplitudeBoundsAndNonSilent() {
        val testNotes = listOf(36, 48, 60, 69, 72, 84)
        for (midi in testNotes) {
            val samples = PianoAudioSynthesizer.generatePcmSamples(midi, 250L)
            assertTrue("Samples should not be empty for MIDI $midi", samples.isNotEmpty())

            var maxObserved = 0
            for (sample in samples) {
                val s = sample.toInt()
                assertTrue(
                    "Sample $s out of 16-bit range for MIDI $midi",
                    s in Short.MIN_VALUE..Short.MAX_VALUE
                )
                if (Math.abs(s) > maxObserved) {
                    maxObserved = Math.abs(s)
                }
            }

            assertTrue(
                "Tone should have audible amplitude for MIDI $midi, observed $maxObserved",
                maxObserved > 1000
            )
        }
    }

    @Test
    fun testEnvelopeSmoothStartAndEnd() {
        val samples = PianoAudioSynthesizer.generatePcmSamples(69, 300L)
        assertTrue(samples.isNotEmpty())

        // Initial sample starts at 0 due to attack ramp
        assertEquals(0.toShort(), samples[0])

        // Final sample fades smoothly to 0 due to release ramp
        val lastSample = samples[samples.size - 1].toInt()
        assertEquals(0, lastSample)
    }
}

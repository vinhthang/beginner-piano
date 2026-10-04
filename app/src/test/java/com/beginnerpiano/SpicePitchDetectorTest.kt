package com.beginnerpiano

import com.beginnerpiano.audio.PitchDetectorType
import com.beginnerpiano.audio.SpicePitchDetector
import com.beginnerpiano.data.models.NotationSystem
import com.beginnerpiano.data.models.NoteEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.roundToInt

class SpicePitchDetectorTest {

    private val detector = SpicePitchDetector(null)

    @Test
    fun testDetectorMetadata() {
        assertEquals("SPICE (AI)", detector.name)
        assertEquals(PitchDetectorType.SPICE, detector.type)
    }

    @Test
    fun testOutputToFrequencyHzCalculations() {
        // Test base zero output
        val freqZero = SpicePitchDetector.outputToFrequencyHz(0.0f)
        assertTrue("Expected freq ~43.82 Hz for 0.0, got $freqZero", abs(freqZero - 43.82) < 0.5)

        // Test A4 (440.0 Hz) output calculation
        // cqtBin = 12 * log2(44) ≈ 65.51318 -> (65.51318 - 25.58) / 63.07 ≈ 0.633156
        val a4Output = 0.633156f
        val freqA4 = SpicePitchDetector.outputToFrequencyHz(a4Output)
        assertTrue("Expected freq ~440 Hz, got $freqA4", abs(freqA4 - 440.0) < 0.5)
        assertEquals(69, NoteEvent.frequencyToMidi(freqA4).roundToInt())

        // Test Middle C (C4, 261.63 Hz) output calculation
        // cqtBin = 12 * log2(26.163) ≈ 56.5134 -> (56.5134 - 25.58) / 63.07 ≈ 0.49046
        val c4Output = 0.49046f
        val freqC4 = SpicePitchDetector.outputToFrequencyHz(c4Output)
        assertTrue("Expected freq ~261.63 Hz, got $freqC4", abs(freqC4 - 261.63) < 0.5)
        assertEquals(60, NoteEvent.frequencyToMidi(freqC4).roundToInt())

        // Test E4 (329.63 Hz)
        val e4Midi = 64
        val e4FreqExpected = NoteEvent.midiToFrequency(e4Midi)
        // cqtBin = 12 * log2(32.963) ≈ 60.5132 -> (60.5132 - 25.58) / 63.07 ≈ 0.55388
        val e4Output = 0.55388f
        val freqE4 = SpicePitchDetector.outputToFrequencyHz(e4Output)
        assertTrue("Expected freq ~$e4FreqExpected, got $freqE4", abs(freqE4 - e4FreqExpected) < 0.5)
        assertEquals(64, NoteEvent.frequencyToMidi(freqE4).roundToInt())
    }

    @Test
    fun testUncertaintyToConfidence() {
        // Zero uncertainty means full 1.0 confidence
        assertEquals(1.0, SpicePitchDetector.uncertaintyToConfidence(0.0f), 0.001)

        // 0.2 uncertainty means 0.8 confidence
        assertEquals(0.8, SpicePitchDetector.uncertaintyToConfidence(0.2f), 0.001)

        // 1.0 uncertainty means 0.0 confidence
        assertEquals(0.0, SpicePitchDetector.uncertaintyToConfidence(1.0f), 0.001)

        // Out-of-bounds uncertainty is coerced to [0.0, 1.0]
        assertEquals(0.0, SpicePitchDetector.uncertaintyToConfidence(1.5f), 0.001)
        assertEquals(1.0, SpicePitchDetector.uncertaintyToConfidence(-0.5f), 0.001)
    }

    @Test
    fun testSilenceHandlingWhenContextIsNull() {
        val buffer = FloatArray(1024) { 0.5f }
        val result = detector.detectPitch(buffer, 16000)

        assertTrue("Expected silence when model interpreter is null", result.isSilent)
        assertEquals(-1, result.midiNote)
        assertEquals(0.0, result.certainty, 0.001)
    }

    @Test
    fun testSmallBufferRejection() {
        val smallBuffer = FloatArray(256) { 0.5f }
        val result = detector.detectPitch(smallBuffer, 16000)

        assertTrue("Expected silence for small buffer", result.isSilent)
        assertEquals(-1, result.midiNote)
    }

    @Test
    fun testMidiSemitoneConversionAcrossPianoNotes() {
        val notesToTest = listOf(
            21 to "A0",
            36 to "C2",
            48 to "C3",
            60 to "C4",
            69 to "A4",
            72 to "C5",
            84 to "C6",
            108 to "C8"
        )

        for ((midi, expectedName) in notesToTest) {
            val freq = NoteEvent.midiToFrequency(midi)
            val computedMidi = NoteEvent.frequencyToMidi(freq).roundToInt()
            assertEquals("MIDI mismatch for $expectedName", midi, computedMidi)
            assertEquals("Note name mismatch", expectedName, NoteEvent.midiToNoteName(computedMidi, NotationSystem.LETTERS))
        }
    }
}

package com.beginnerpiano

import com.beginnerpiano.data.models.NotationSystem
import com.beginnerpiano.data.models.NoteEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class NotationSystemTest {

    @Test
    fun testSolfegeChromaticScale() {
        val expectedSolfege = mapOf(
            60 to "Do4",
            61 to "Do#4",
            62 to "Re4",
            63 to "Re#4",
            64 to "Mi4",
            65 to "Fa4",
            66 to "Fa#4",
            67 to "Sol4",
            68 to "Sol#4",
            69 to "La4",
            70 to "La#4",
            71 to "Si4",
            72 to "Do5"
        )

        for ((midi, expectedName) in expectedSolfege) {
            val actual = NoteEvent.midiToNoteName(midi, NotationSystem.SOLFEGE)
            assertEquals("Solfège mapping mismatch for MIDI $midi", expectedName, actual)
        }
    }

    @Test
    fun testLettersScale() {
        val expectedLetters = mapOf(
            60 to "C4",
            69 to "A4",
            71 to "B4"
        )

        for ((midi, expectedName) in expectedLetters) {
            val actual = NoteEvent.midiToNoteName(midi, NotationSystem.LETTERS)
            assertEquals("Letters mapping mismatch for MIDI $midi", expectedName, actual)
        }
    }

    @Test
    fun testDefaultNotationIsSolfege() {
        assertEquals("Default parameter should be Solfège Do4", "Do4", NoteEvent.midiToNoteName(60))
        assertEquals("Default parameter should be Solfège La4", "La4", NoteEvent.midiToNoteName(69))
        assertEquals("Default parameter should be Solfège Si4", "Si4", NoteEvent.midiToNoteName(71))

        val note = NoteEvent(midiNote = 60)
        assertEquals("NoteEvent noteName should default to Solfège", "Do4", note.noteName)
        assertEquals("NoteEvent getNoteName() should default to Solfège", "Do4", note.getNoteName())
        assertEquals("NoteEvent getNoteName(LETTERS) should return C4", "C4", note.getNoteName(NotationSystem.LETTERS))
    }
}

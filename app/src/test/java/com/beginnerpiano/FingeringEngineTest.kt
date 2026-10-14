package com.beginnerpiano

import androidx.compose.ui.graphics.Color
import com.beginnerpiano.data.models.ClefType
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.practice.FingeringEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class FingeringEngineTest {

    @Test
    fun testExplicitFingerHintIsRespected() {
        val noteWithHint1 = NoteEvent(midiNote = 67, fingerHint = 1) // G4 played with Thumb (cross-under)
        assertEquals(1, FingeringEngine.resolveFinger(noteWithHint1))

        val noteWithHint3 = NoteEvent(midiNote = 60, fingerHint = 3) // C4 played with Middle finger
        assertEquals(3, FingeringEngine.resolveFinger(noteWithHint3))

        val noteWithHint5 = NoteEvent(midiNote = 62, fingerHint = 5) // D4 with Pinky
        assertEquals(5, FingeringEngine.resolveFinger(noteWithHint5))
    }

    @Test
    fun testInvalidFingerHintFallsBackToHeuristic() {
        // fingerHint < 1 or > 5 should fall back to heuristic
        val noteZero = NoteEvent(midiNote = 60, fingerHint = 0)
        assertEquals(1, FingeringEngine.resolveFinger(noteZero))

        val noteSix = NoteEvent(midiNote = 64, fingerHint = 6)
        assertEquals(3, FingeringEngine.resolveFinger(noteSix))

        val noteNegative = NoteEvent(midiNote = 67, fingerHint = -1)
        assertEquals(5, FingeringEngine.resolveFinger(noteNegative))
    }

    @Test
    fun testBeginnerFiveFingerFallbackMapping() {
        // C4 = 60 -> 1 (Thumb)
        assertEquals(1, FingeringEngine.resolveFinger(NoteEvent(midiNote = 60)))
        // D4 = 62 -> 2 (Index)
        assertEquals(2, FingeringEngine.resolveFinger(NoteEvent(midiNote = 62)))
        // E4 = 64 -> 3 (Middle)
        assertEquals(3, FingeringEngine.resolveFinger(NoteEvent(midiNote = 64)))
        // F4 = 65 -> 4 (Ring)
        assertEquals(4, FingeringEngine.resolveFinger(NoteEvent(midiNote = 65)))
        // G4 = 67 -> 5 (Pinky)
        assertEquals(5, FingeringEngine.resolveFinger(NoteEvent(midiNote = 67)))
    }

    @Test
    fun testBoundaryAndChromaticNotesClamped() {
        // Below C4 -> clamped to 1
        assertEquals(1, FingeringEngine.resolveFinger(NoteEvent(midiNote = 59)))
        assertEquals(1, FingeringEngine.resolveFinger(NoteEvent(midiNote = 48)))

        // C#4 (61) -> 2
        assertEquals(2, FingeringEngine.resolveFinger(NoteEvent(midiNote = 61)))
        // D#4 (63) -> 3
        assertEquals(3, FingeringEngine.resolveFinger(NoteEvent(midiNote = 63)))
        // F#4 (66) -> 4
        assertEquals(4, FingeringEngine.resolveFinger(NoteEvent(midiNote = 66)))

        // Above G4 -> clamped to 5
        assertEquals(5, FingeringEngine.resolveFinger(NoteEvent(midiNote = 68)))
        assertEquals(5, FingeringEngine.resolveFinger(NoteEvent(midiNote = 72)))
        assertEquals(5, FingeringEngine.resolveFinger(NoteEvent(midiNote = 84)))
    }

    @Test
    fun testFingerColors() {
        assertEquals(Color(0xFF3B82F6), FingeringEngine.getFingerColor(1))
        assertEquals(Color(0xFF10B981), FingeringEngine.getFingerColor(2))
        assertEquals(Color(0xFFF59E0B), FingeringEngine.getFingerColor(3))
        assertEquals(Color(0xFFF97316), FingeringEngine.getFingerColor(4))
        assertEquals(Color(0xFFEC4899), FingeringEngine.getFingerColor(5))
        // Fallback for invalid finger
        assertEquals(Color(0xFF94A3B8), FingeringEngine.getFingerColor(0))
        assertEquals(Color(0xFF94A3B8), FingeringEngine.getFingerColor(6))
    }

    @Test
    fun testFingerNames() {
        assertEquals("Thumb (1)", FingeringEngine.getFingerName(1))
        assertEquals("Index (2)", FingeringEngine.getFingerName(2))
        assertEquals("Middle (3)", FingeringEngine.getFingerName(3))
        assertEquals("Ring (4)", FingeringEngine.getFingerName(4))
        assertEquals("Pinky (5)", FingeringEngine.getFingerName(5))
    }
}

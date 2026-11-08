package com.beginnerpiano

import com.beginnerpiano.ui.components.NoteHeadType
import com.beginnerpiano.ui.components.NoteNotationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteNotationHelperTest {

    @Test
    fun testNoteHeadTypeMapping() {
        assertEquals(NoteHeadType.WHOLE, NoteNotationHelper.getNoteHeadType(4.0))
        assertEquals(NoteHeadType.HALF, NoteNotationHelper.getNoteHeadType(3.0))
        assertEquals(NoteHeadType.HALF, NoteNotationHelper.getNoteHeadType(2.0))
        assertEquals(NoteHeadType.QUARTER, NoteNotationHelper.getNoteHeadType(1.5))
        assertEquals(NoteHeadType.QUARTER, NoteNotationHelper.getNoteHeadType(1.0))
        assertEquals(NoteHeadType.EIGHTH, NoteNotationHelper.getNoteHeadType(0.5))
        assertEquals(NoteHeadType.EIGHTH, NoteNotationHelper.getNoteHeadType(0.25))
    }

    @Test
    fun testIsDotted() {
        assertTrue(NoteNotationHelper.isDotted(1.5))
        assertTrue(NoteNotationHelper.isDotted(3.0))
        assertFalse(NoteNotationHelper.isDotted(1.0))
        assertFalse(NoteNotationHelper.isDotted(2.0))
        assertFalse(NoteNotationHelper.isDotted(4.0))
        assertFalse(NoteNotationHelper.isDotted(0.5))
    }

    @Test
    fun testIsSharp() {
        // Sharp pitches: C#4 (61), D#4 (63), F#4 (66), G#4 (68), A#4 (70)
        assertTrue(NoteNotationHelper.isSharp(61))
        assertTrue(NoteNotationHelper.isSharp(63))
        assertTrue(NoteNotationHelper.isSharp(66))
        assertTrue(NoteNotationHelper.isSharp(68))
        assertTrue(NoteNotationHelper.isSharp(70))

        // Natural pitches: C4 (60), D4 (62), E4 (64), F4 (65), G4 (67), A4 (69), B4 (71), C5 (72)
        assertFalse(NoteNotationHelper.isSharp(60))
        assertFalse(NoteNotationHelper.isSharp(62))
        assertFalse(NoteNotationHelper.isSharp(64))
        assertFalse(NoteNotationHelper.isSharp(65))
        assertFalse(NoteNotationHelper.isSharp(67))
        assertFalse(NoteNotationHelper.isSharp(69))
        assertFalse(NoteNotationHelper.isSharp(71))
        assertFalse(NoteNotationHelper.isSharp(72))
    }
}

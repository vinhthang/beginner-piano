package com.beginnerpiano

import com.beginnerpiano.data.models.ClefType
import com.beginnerpiano.data.models.DifficultyLevel
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.data.models.Song
import com.beginnerpiano.practice.NoteFeedbackStatus
import com.beginnerpiano.practice.PracticeEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeEngineTest {

    private fun createTestSong(): Song {
        val notes = listOf(
            NoteEvent(midiNote = 60, clef = ClefType.TREBLE), // C4
            NoteEvent(midiNote = 62, clef = ClefType.TREBLE), // D4
            NoteEvent(midiNote = 64, clef = ClefType.TREBLE)  // E4
        )
        return Song(
            id = "test_song",
            title = "Test",
            composer = "Test",
            difficulty = DifficultyLevel.BEGINNER,
            notes = notes
        )
    }

    @Test
    fun testInitialState() {
        val engine = PracticeEngine(createTestSong())
        assertEquals(0, engine.state.currentNoteIndex)
        assertEquals(60, engine.state.currentTargetNote?.midiNote)
        assertFalse(engine.state.isCompleted)
        assertFalse(engine.state.isPlayingDemo)
        assertEquals(0, engine.state.totalHits)
        assertEquals(0, engine.state.totalMisses)
    }

    @Test
    fun testCorrectNoteAdvancesPlayhead() {
        val engine = PracticeEngine(createTestSong())
        val status = engine.onKeyTapped(60) // Play C4

        assertEquals(NoteFeedbackStatus.HIT, status)
        assertEquals(1, engine.state.currentNoteIndex)
        assertEquals(62, engine.state.currentTargetNote?.midiNote)
        assertEquals(1, engine.state.totalHits)
        assertEquals(1, engine.state.currentStreak)
    }

    @Test
    fun testWrongNoteDoesNotAdvancePlayhead() {
        val engine = PracticeEngine(createTestSong())
        val status = engine.onKeyTapped(65) // Play F4 instead of C4

        assertEquals(NoteFeedbackStatus.MISMATCH, status)
        assertEquals(0, engine.state.currentNoteIndex) // Paused on C4!
        assertEquals(60, engine.state.currentTargetNote?.midiNote)
        assertEquals(0, engine.state.totalHits)
        assertEquals(1, engine.state.totalMisses)
        assertEquals(0, engine.state.currentStreak)
    }

    @Test
    fun testSongCompletion() {
        val engine = PracticeEngine(createTestSong())
        engine.onKeyTapped(60) // C4
        engine.onKeyTapped(62) // D4
        val status = engine.onKeyTapped(64) // E4

        assertEquals(NoteFeedbackStatus.HIT, status)
        assertEquals(3, engine.state.currentNoteIndex)
        assertTrue(engine.state.isCompleted)
        assertEquals(3, engine.state.totalHits)
        assertEquals(100, engine.state.accuracyPercent)
    }

    @Test
    fun testDemoPlaybackState() {
        val engine = PracticeEngine(createTestSong())
        assertFalse(engine.state.isPlayingDemo)

        engine.setPlayingDemo(true)
        assertTrue(engine.state.isPlayingDemo)

        engine.setPlayingDemo(false)
        assertFalse(engine.state.isPlayingDemo)
    }

    @Test
    fun testNotationSystemStateAndDetection() {
        val engine = PracticeEngine(createTestSong())
        assertEquals(com.beginnerpiano.data.models.NotationSystem.SOLFEGE, engine.state.notationSystem)

        // Play C4 (60) with default Solfège
        engine.onKeyTapped(60)
        assertEquals("Do4", engine.state.lastDetectedName)

        // Switch to Letters notation
        engine.setNotationSystem(com.beginnerpiano.data.models.NotationSystem.LETTERS)
        assertEquals(com.beginnerpiano.data.models.NotationSystem.LETTERS, engine.state.notationSystem)

        // Play D4 (62) with Letters
        engine.onKeyTapped(62)
        assertEquals("D4", engine.state.lastDetectedName)
    }
}

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

    @Test
    fun testTempoMultiplierAndEffectiveBpm() {
        val engine = PracticeEngine(createTestSong()) // defaultBpm = 60
        assertEquals(1.0f, engine.state.tempoMultiplier, 0.001f)
        assertEquals(60, engine.state.effectiveBpm)

        engine.setTempoMultiplier(0.5f)
        assertEquals(0.5f, engine.state.tempoMultiplier, 0.001f)
        assertEquals(30, engine.state.effectiveBpm)

        engine.setTempoMultiplier(1.25f)
        assertEquals(1.25f, engine.state.tempoMultiplier, 0.001f)
        assertEquals(75, engine.state.effectiveBpm)

        // Clamping bounds: 0.5f..1.5f
        engine.setTempoMultiplier(0.1f)
        assertEquals(0.5f, engine.state.tempoMultiplier, 0.001f)
        assertEquals(30, engine.state.effectiveBpm)

        engine.setTempoMultiplier(2.5f)
        assertEquals(1.5f, engine.state.tempoMultiplier, 0.001f)
        assertEquals(90, engine.state.effectiveBpm)
    }

    @Test
    fun testLoopRangeAndNavigation() {
        val engine = PracticeEngine(createTestSong()) // 3 notes: index 0, 1, 2
        assertFalse(engine.state.isLoopActive)

        // Set loop [0..1]
        engine.setLoopRange(0, 1)
        assertTrue(engine.state.isLoopActive)
        assertEquals(0, engine.state.loopStartIndex)
        assertEquals(1, engine.state.loopEndIndex)

        // Hit note 0 (60)
        val s0 = engine.onKeyTapped(60)
        assertEquals(NoteFeedbackStatus.HIT, s0)
        assertEquals(1, engine.state.currentNoteIndex)
        assertFalse(engine.state.isCompleted)

        // Hit note 1 (62, end of loop) -> wraps back to 0 without completing!
        val s1 = engine.onKeyTapped(62)
        assertEquals(NoteFeedbackStatus.HIT, s1)
        assertEquals(0, engine.state.currentNoteIndex)
        assertFalse(engine.state.isCompleted)

        // Clear loop -> allows finishing note 0, 1, 2
        engine.clearLoop()
        assertFalse(engine.state.isLoopActive)
        engine.onKeyTapped(60) // index 1
        engine.onKeyTapped(62) // index 2
        engine.onKeyTapped(64) // index 3 -> completed!
        assertTrue(engine.state.isCompleted)
    }

    @Test
    fun testOnNoteTappedForLoop() {
        val engine = PracticeEngine(createTestSong()) // 3 notes: indices 0, 1, 2
        assertFalse(engine.state.isLoopActive)

        // First tap note 1: sets range [1 .. minOf(1+3, 2)] = [1..2]
        engine.onNoteTappedForLoop(1)
        assertTrue(engine.state.isLoopActive)
        assertEquals(1, engine.state.loopStartIndex)
        assertEquals(2, engine.state.loopEndIndex)

        // Tap before start (note 0): moves start to 0 -> [0..2]
        engine.onNoteTappedForLoop(0)
        assertEquals(0, engine.state.loopStartIndex)
        assertEquals(2, engine.state.loopEndIndex)

        // Tap after start (note 1): moves end to 1 -> [0..1]
        engine.onNoteTappedForLoop(1)
        assertEquals(0, engine.state.loopStartIndex)
        assertEquals(1, engine.state.loopEndIndex)

        // Tap start index (note 0): clears loop
        engine.onNoteTappedForLoop(0)
        assertFalse(engine.state.isLoopActive)
    }

    @Test
    fun testOnBeatTimeoutInTempoMode() {
        val engine = PracticeEngine(createTestSong())
        engine.setMode(com.beginnerpiano.practice.PracticeMode.TEMPO)

        // On timeout at note 0: records mismatch, advances to 1
        val status = engine.onBeatTimeout()
        assertEquals(NoteFeedbackStatus.MISMATCH, status)
        assertEquals(1, engine.state.totalMisses)
        assertEquals(0, engine.state.currentStreak)
        assertEquals(1, engine.state.currentNoteIndex)
        assertFalse(engine.state.isCompleted)

        // Set loop [1..1]
        engine.setLoopRange(1, 1)
        // Timeout at loop end wraps back to loop start (1)
        val statusLoop = engine.onBeatTimeout()
        assertEquals(NoteFeedbackStatus.MISMATCH, statusLoop)
        assertEquals(1, engine.state.currentNoteIndex)
        assertFalse(engine.state.isCompleted)

        // Clear loop and advance to end
        engine.clearLoop()
        engine.onBeatTimeout() // advances to 2
        assertEquals(2, engine.state.currentNoteIndex)
        assertFalse(engine.state.isCompleted)

        engine.onBeatTimeout() // advances past 2 -> completed!
        assertEquals(3, engine.state.currentNoteIndex)
        assertTrue(engine.state.isCompleted)
    }

    @Test
    fun testEarnedStarsCalculation() {
        assertEquals(3, com.beginnerpiano.practice.PracticeState.calculateStars(100))
        assertEquals(3, com.beginnerpiano.practice.PracticeState.calculateStars(95))
        assertEquals(2, com.beginnerpiano.practice.PracticeState.calculateStars(94))
        assertEquals(2, com.beginnerpiano.practice.PracticeState.calculateStars(80))
        assertEquals(1, com.beginnerpiano.practice.PracticeState.calculateStars(79))
        assertEquals(1, com.beginnerpiano.practice.PracticeState.calculateStars(60))
        assertEquals(0, com.beginnerpiano.practice.PracticeState.calculateStars(59))
        assertEquals(0, com.beginnerpiano.practice.PracticeState.calculateStars(0))

        val engine = PracticeEngine(createTestSong())
        // Initially 100% (0 total notes hit or missed -> accuracyPercent is 100)
        assertEquals(3, engine.state.earnedStars)
    }

    @Test
    fun testResetPreservesTempoAndLoop() {
        val engine = PracticeEngine(createTestSong())
        engine.setTempoMultiplier(0.75f)
        engine.setMetronomeEnabled(false)
        engine.setLoopRange(1, 2)
        engine.onKeyTapped(62) // advances to 2

        engine.reset()
        assertEquals(0.75f, engine.state.tempoMultiplier, 0.001f)
        assertFalse(engine.state.metronomeEnabled)
        assertEquals(1, engine.state.loopStartIndex)
        assertEquals(2, engine.state.loopEndIndex)
        assertEquals(1, engine.state.currentNoteIndex) // resets to loopStartIndex
    }
}

package com.beginnerpiano.practice

import com.beginnerpiano.audio.DetectedPitch
import com.beginnerpiano.audio.PitchDetectorType
import com.beginnerpiano.data.models.NotationSystem
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.data.models.Song

enum class PracticeMode {
    WAIT_FOR_NOTE,
    TEMPO
}

enum class NoteFeedbackStatus {
    IDLE,
    HIT,
    MISMATCH
}

data class PracticeState(
    val song: Song,
    val currentNoteIndex: Int = 0,
    val mode: PracticeMode = PracticeMode.WAIT_FOR_NOTE,
    val pitchDetectorType: PitchDetectorType = PitchDetectorType.YIN,
    val notationSystem: NotationSystem = NotationSystem.SOLFEGE,
    val feedbackStatus: NoteFeedbackStatus = NoteFeedbackStatus.IDLE,
    val lastDetectedMidi: Int? = null,
    val lastDetectedName: String? = null,
    val totalHits: Int = 0,
    val totalMisses: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isCompleted: Boolean = false,
    val isPlayingDemo: Boolean = false
) {
    val currentTargetNote: NoteEvent?
        get() = if (currentNoteIndex in song.notes.indices) song.notes[currentNoteIndex] else null

    val progressFraction: Float
        get() = if (song.notes.isNotEmpty()) (currentNoteIndex.toFloat() / song.notes.size) else 0f

    val accuracyPercent: Int
        get() {
            val total = totalHits + totalMisses
            return if (total > 0) ((totalHits.toDouble() / total) * 100).toInt() else 100
        }

    val streak: Int
        get() = currentStreak
}

class PracticeEngine(initialSong: Song) {

    var state: PracticeState = PracticeState(song = initialSong)
        private set

    fun reset() {
        state = PracticeState(
            song = state.song,
            mode = state.mode,
            pitchDetectorType = state.pitchDetectorType,
            notationSystem = state.notationSystem
        )
    }

    fun setSong(song: Song) {
        state = PracticeState(
            song = song,
            mode = state.mode,
            pitchDetectorType = state.pitchDetectorType,
            notationSystem = state.notationSystem
        )
    }

    fun setMode(mode: PracticeMode) {
        state = state.copy(mode = mode)
    }

    fun setPitchDetectorType(type: PitchDetectorType) {
        state = state.copy(pitchDetectorType = type)
    }

    fun setNotationSystem(notation: NotationSystem) {
        state = state.copy(notationSystem = notation)
    }

    fun setPlayingDemo(isPlaying: Boolean) {
        state = state.copy(isPlayingDemo = isPlaying)
    }

    fun clearFeedbackStatus() {
        if (state.feedbackStatus != NoteFeedbackStatus.IDLE) {
            state = state.copy(feedbackStatus = NoteFeedbackStatus.IDLE)
        }
    }

    fun onPitchDetected(detected: DetectedPitch): NoteFeedbackStatus {
        if (state.isCompleted || detected.isSilent) return NoteFeedbackStatus.IDLE
        return evaluateMidi(detected.midiNote)
    }

    fun onKeyTapped(midiNote: Int): NoteFeedbackStatus {
        if (state.isCompleted) return NoteFeedbackStatus.IDLE
        return evaluateMidi(midiNote)
    }

    private fun evaluateMidi(playedMidi: Int): NoteFeedbackStatus {
        val target = state.currentTargetNote ?: return NoteFeedbackStatus.IDLE
        val playedName = NoteEvent.midiToNoteName(playedMidi, state.notationSystem)

        return if (playedMidi == target.midiNote) {
            // Correct note hit!
            val newIndex = state.currentNoteIndex + 1
            val newHits = state.totalHits + 1
            val newStreak = state.currentStreak + 1
            val bestStreak = maxOf(state.bestStreak, newStreak)
            val isDone = newIndex >= state.song.notes.size

            state = state.copy(
                currentNoteIndex = newIndex,
                feedbackStatus = NoteFeedbackStatus.HIT,
                lastDetectedMidi = playedMidi,
                lastDetectedName = playedName,
                totalHits = newHits,
                currentStreak = newStreak,
                bestStreak = bestStreak,
                isCompleted = isDone
            )
            NoteFeedbackStatus.HIT
        } else {
            // Mismatch
            state = state.copy(
                feedbackStatus = NoteFeedbackStatus.MISMATCH,
                lastDetectedMidi = playedMidi,
                lastDetectedName = playedName,
                totalMisses = state.totalMisses + 1,
                currentStreak = 0
            )
            NoteFeedbackStatus.MISMATCH
        }
    }
}

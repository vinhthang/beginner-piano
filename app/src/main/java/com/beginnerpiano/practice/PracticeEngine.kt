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
    val isPlayingDemo: Boolean = false,
    val tempoMultiplier: Float = 1.0f,
    val loopStartIndex: Int? = null,
    val loopEndIndex: Int? = null,
    val metronomeEnabled: Boolean = true,
    val isTempoRunning: Boolean = false
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

    val effectiveBpm: Int
        get() = (song.defaultBpm * tempoMultiplier).toInt().coerceAtLeast(20)

    val isLoopActive: Boolean
        get() = loopStartIndex != null && loopEndIndex != null && loopStartIndex <= loopEndIndex

    val earnedStars: Int
        get() = calculateStars(accuracyPercent)

    companion object {
        fun calculateStars(accuracyPercent: Int): Int {
            return when {
                accuracyPercent >= 95 -> 3
                accuracyPercent >= 80 -> 2
                accuracyPercent >= 60 -> 1
                else -> 0
            }
        }
    }
}

class PracticeEngine(initialSong: Song) {

    var state: PracticeState = PracticeState(song = initialSong)
        private set

    fun reset() {
        state = PracticeState(
            song = state.song,
            currentNoteIndex = state.loopStartIndex ?: 0,
            mode = state.mode,
            pitchDetectorType = state.pitchDetectorType,
            notationSystem = state.notationSystem,
            tempoMultiplier = state.tempoMultiplier,
            loopStartIndex = state.loopStartIndex,
            loopEndIndex = state.loopEndIndex,
            metronomeEnabled = state.metronomeEnabled
        )
    }

    fun setSong(song: Song) {
        state = PracticeState(
            song = song,
            mode = state.mode,
            pitchDetectorType = state.pitchDetectorType,
            notationSystem = state.notationSystem,
            tempoMultiplier = state.tempoMultiplier,
            metronomeEnabled = state.metronomeEnabled
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

    fun setTempoMultiplier(multiplier: Float) {
        state = state.copy(tempoMultiplier = multiplier.coerceIn(0.5f, 1.5f))
    }

    fun setLoopRange(startIndex: Int?, endIndex: Int?) {
        if (startIndex == null || endIndex == null || state.song.notes.isEmpty()) {
            state = state.copy(loopStartIndex = null, loopEndIndex = null)
            return
        }
        val clampedStart = startIndex.coerceIn(0, state.song.notes.lastIndex)
        val clampedEnd = endIndex.coerceIn(0, state.song.notes.lastIndex)
        val normStart = minOf(clampedStart, clampedEnd)
        val normEnd = maxOf(clampedStart, clampedEnd)
        val newNoteIndex = if (state.currentNoteIndex !in normStart..normEnd) normStart else state.currentNoteIndex
        state = state.copy(
            loopStartIndex = normStart,
            loopEndIndex = normEnd,
            currentNoteIndex = newNoteIndex,
            isCompleted = false
        )
    }

    fun onNoteTappedForLoop(noteIndex: Int) {
        if (state.song.notes.isEmpty()) return
        val idx = noteIndex.coerceIn(0, state.song.notes.lastIndex)
        val start = state.loopStartIndex
        val end = state.loopEndIndex
        if (!state.isLoopActive || start == null || end == null) {
            val defaultEnd = minOf(idx + 3, state.song.notes.lastIndex)
            setLoopRange(idx, defaultEnd)
        } else {
            if (idx == start && idx == end) {
                clearLoop()
            } else if (idx < start) {
                setLoopRange(idx, end)
            } else if (idx > start) {
                setLoopRange(start, idx)
            } else if (idx == start) {
                clearLoop()
            }
        }
    }

    fun clearLoop() {
        state = state.copy(loopStartIndex = null, loopEndIndex = null)
    }

    fun setMetronomeEnabled(enabled: Boolean) {
        state = state.copy(metronomeEnabled = enabled)
    }

    fun setTempoRunning(running: Boolean) {
        state = state.copy(isTempoRunning = running)
    }

    fun onBeatTimeout(): NoteFeedbackStatus {
        if (state.isCompleted) return NoteFeedbackStatus.IDLE
        val target = state.currentTargetNote ?: return NoteFeedbackStatus.IDLE

        val isLoop = state.isLoopActive
        val loopStart = state.loopStartIndex ?: 0
        val loopEnd = state.loopEndIndex ?: state.song.notes.lastIndex

        val (nextIndex, isDone) = if (isLoop && state.currentNoteIndex >= loopEnd) {
            Pair(loopStart, false)
        } else {
            val idx = state.currentNoteIndex + 1
            val done = idx >= state.song.notes.size
            Pair(idx, done)
        }

        state = state.copy(
            currentNoteIndex = nextIndex,
            feedbackStatus = NoteFeedbackStatus.MISMATCH,
            totalMisses = state.totalMisses + 1,
            currentStreak = 0,
            isCompleted = isDone
        )
        return NoteFeedbackStatus.MISMATCH
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
            val isLoop = state.isLoopActive
            val loopStart = state.loopStartIndex ?: 0
            val loopEnd = state.loopEndIndex ?: state.song.notes.lastIndex

            val (newIndex, isDone) = if (isLoop && state.currentNoteIndex >= loopEnd) {
                Pair(loopStart, false)
            } else {
                val next = state.currentNoteIndex + 1
                val done = next >= state.song.notes.size
                Pair(next, done)
            }

            val newHits = state.totalHits + 1
            val newStreak = state.currentStreak + 1
            val bestStreak = maxOf(state.bestStreak, newStreak)

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

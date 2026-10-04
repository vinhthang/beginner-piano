package com.beginnerpiano.data.models

import java.util.UUID

enum class ClefType {
    TREBLE,
    BASS
}

enum class DifficultyLevel {
    BEGINNER,
    ELEMENTARY,
    INTERMEDIATE
}

enum class NotationSystem(val label: String) {
    SOLFEGE("Solfège (Do-Re-Mi)"),
    LETTERS("Letters (C-D-E)")
}

data class NoteEvent(
    val id: String = UUID.randomUUID().toString(),
    val midiNote: Int,
    val durationBeats: Double = 1.0,
    val clef: ClefType = ClefType.TREBLE,
    val fingerHint: Int? = null,
    val description: String = ""
) {
    val noteName: String
        get() = midiToNoteName(midiNote, NotationSystem.SOLFEGE)

    fun getNoteName(notation: NotationSystem = NotationSystem.SOLFEGE): String =
        midiToNoteName(midiNote, notation)

    companion object {
        private val LETTER_NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        private val SOLFEGE_NAMES = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")

        fun midiToNoteName(midi: Int, notation: NotationSystem = NotationSystem.SOLFEGE): String {
            if (midi < 21 || midi > 108) return "Unknown"
            val noteIndex = (midi - 12) % 12
            val octave = (midi - 12) / 12
            val names = when (notation) {
                NotationSystem.SOLFEGE -> SOLFEGE_NAMES
                NotationSystem.LETTERS -> LETTER_NAMES
            }
            return "${names[noteIndex]}$octave"
        }

        fun midiToFrequency(midi: Int): Double {
            return 440.0 * Math.pow(2.0, (midi - 69) / 12.0)
        }

        fun frequencyToMidi(freq: Double): Double {
            if (freq <= 0.0) return 0.0
            return 69.0 + 12.0 * (Math.log(freq / 440.0) / Math.log(2.0))
        }
    }
}

data class Song(
    val id: String,
    val title: String,
    val composer: String,
    val difficulty: DifficultyLevel,
    val defaultBpm: Int = 60,
    val notes: List<NoteEvent>
)

package com.beginnerpiano.practice

import androidx.compose.ui.graphics.Color
import com.beginnerpiano.data.models.NoteEvent

object FingeringEngine {

    /**
     * Resolves the recommended finger for playing a given note.
     * Respects [NoteEvent.fingerHint] if present and in range 1..5.
     * Otherwise falls back to beginner 5-finger right-hand C position:
     * C4 (60) = 1 (Thumb)
     * D4 (62) = 2 (Index)
     * E4 (64) = 3 (Middle)
     * F4 (65) = 4 (Ring)
     * G4 (67) = 5 (Pinky)
     * Clamped to 1..5.
     */
    fun resolveFinger(note: NoteEvent): Int {
        val hint = note.fingerHint
        if (hint != null && hint in 1..5) {
            return hint
        }
        return when {
            note.midiNote <= 60 -> 1
            note.midiNote in 61..62 -> 2
            note.midiNote in 63..64 -> 3
            note.midiNote in 65..66 -> 4
            else -> 5
        }
    }

    /**
     * Returns the educational signature color for a given finger (1..5).
     */
    fun getFingerColor(finger: Int): Color {
        return when (finger) {
            1 -> Color(0xFF3B82F6) // Blue (Thumb)
            2 -> Color(0xFF10B981) // Emerald (Index)
            3 -> Color(0xFFF59E0B) // Amber (Middle)
            4 -> Color(0xFFF97316) // Orange (Ring)
            5 -> Color(0xFFEC4899) // Rose (Pinky)
            else -> Color(0xFF94A3B8) // Slate fallback
        }
    }

    /**
     * Returns human-readable label for the finger.
     */
    fun getFingerName(finger: Int): String {
        return when (finger) {
            1 -> "Thumb (1)"
            2 -> "Index (2)"
            3 -> "Middle (3)"
            4 -> "Ring (4)"
            5 -> "Pinky (5)"
            else -> "Finger $finger"
        }
    }
}

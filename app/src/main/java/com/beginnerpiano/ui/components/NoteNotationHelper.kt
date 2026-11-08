package com.beginnerpiano.ui.components

import kotlin.math.abs

enum class NoteHeadType {
    WHOLE,
    HALF,
    QUARTER,
    EIGHTH
}

object NoteNotationHelper {

    private val SHARP_PITCH_CLASSES = setOf(1, 3, 6, 8, 10)

    fun getNoteHeadType(durationBeats: Double): NoteHeadType {
        return when {
            durationBeats >= 3.5 -> NoteHeadType.WHOLE
            durationBeats >= 1.75 -> NoteHeadType.HALF
            durationBeats >= 0.85 -> NoteHeadType.QUARTER
            else -> NoteHeadType.EIGHTH
        }
    }

    fun isDotted(durationBeats: Double): Boolean {
        return abs(durationBeats - 1.5) <= 0.1 || abs(durationBeats - 3.0) <= 0.1
    }

    fun isSharp(midiNote: Int): Boolean {
        val pitchClass = ((midiNote % 12) + 12) % 12
        return pitchClass in SHARP_PITCH_CLASSES
    }
}

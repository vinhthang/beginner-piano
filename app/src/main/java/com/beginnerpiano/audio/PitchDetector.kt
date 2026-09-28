package com.beginnerpiano.audio

import com.beginnerpiano.data.models.NoteEvent

data class DetectedPitch(
    val frequencyHz: Double,
    val midiNote: Int,
    val centsOffset: Double,
    val certainty: Double,
    val isSilent: Boolean
) {
    val noteName: String
        get() = if (isSilent) "--" else NoteEvent.midiToNoteName(midiNote)

    companion object {
        val SILENCE = DetectedPitch(
            frequencyHz = 0.0,
            midiNote = -1,
            centsOffset = 0.0,
            certainty = 0.0,
            isSilent = true
        )
    }
}

interface PitchDetector {
    fun detectPitch(buffer: FloatArray, sampleRate: Int): DetectedPitch
}

package com.beginnerpiano.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.practice.NoteFeedbackStatus
import com.beginnerpiano.ui.theme.Emerald500
import com.beginnerpiano.ui.theme.Indigo500
import com.beginnerpiano.ui.theme.Rose500
import com.beginnerpiano.ui.theme.Slate400
import com.beginnerpiano.ui.theme.Slate700

@Composable
fun StaffCanvas(
    notes: List<NoteEvent>,
    activeIndex: Int,
    feedbackStatus: NoteFeedbackStatus,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
) {
    val staffLineColor = Slate400
    val activeColor = when (feedbackStatus) {
        NoteFeedbackStatus.HIT -> Emerald500
        NoteFeedbackStatus.MISMATCH -> Rose500
        NoteFeedbackStatus.IDLE -> Indigo500
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val lineSpacing = 14.dp.toPx()
        val noteSpacing = 55.dp.toPx()
        val anchorX = width * 0.28f

        // Draw 5 Staff lines for Treble Clef
        // Line 1 (top): F5, Line 5 (bottom): E4
        val line5Y = centerY + 2 * lineSpacing
        for (i in 0 until 5) {
            val lineY = centerY + (i - 2) * lineSpacing
            drawLine(
                color = staffLineColor.copy(alpha = 0.6f),
                start = Offset(16.dp.toPx(), lineY),
                end = Offset(width - 16.dp.toPx(), lineY),
                strokeWidth = 1.8f
            )
        }

        // Draw Playhead guide line at anchor position
        drawLine(
            color = activeColor.copy(alpha = 0.4f),
            start = Offset(anchorX, 10.dp.toPx()),
            end = Offset(anchorX, height - 10.dp.toPx()),
            strokeWidth = 2.5f
        )

        // Draw Treble Clef symbol indicator
        drawTrebleClefIndicator(centerY, lineSpacing)

        // Draw Notes
        notes.forEachIndexed { index, note ->
            val noteX = anchorX + (index - activeIndex) * noteSpacing
            if (noteX in -40f..(width + 40f)) {
                val step = diatonicStepFromE4(note.midiNote)
                val noteY = line5Y - step * (lineSpacing / 2f)
                val isCurrent = (index == activeIndex)
                val isPast = (index < activeIndex)

                val noteColor = when {
                    isCurrent -> activeColor
                    isPast -> Slate400.copy(alpha = 0.4f)
                    else -> Slate700
                }

                // Middle C Ledger Line (C4 has midi 60, step = -2)
                if (note.midiNote == 60) {
                    drawLine(
                        color = noteColor,
                        start = Offset(noteX - 16.dp.toPx(), noteY),
                        end = Offset(noteX + 16.dp.toPx(), noteY),
                        strokeWidth = 2.2f
                    )
                }

                // Draw Note Head (Tilted Ellipse approximation)
                drawOval(
                    color = noteColor,
                    topLeft = Offset(noteX - 9.dp.toPx(), noteY - 6.5.dp.toPx()),
                    size = Size(18.dp.toPx(), 13.dp.toPx())
                )

                // Draw Note Stem (pointing up)
                drawLine(
                    color = noteColor,
                    start = Offset(noteX + 8.dp.toPx(), noteY),
                    end = Offset(noteX + 8.dp.toPx(), noteY - 36.dp.toPx()),
                    strokeWidth = 2.2f
                )

                // Draw Note Name above current note
                if (isCurrent) {
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = activeColor.hashCode()
                            textSize = 34f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                        drawText(note.noteName, noteX, noteY - 44.dp.toPx(), paint)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawTrebleClefIndicator(centerY: Float, lineSpacing: Float) {
    // Stylized G-clef anchor glyph on left
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 140f
            textAlign = android.graphics.Paint.Align.LEFT
        }
        drawText("𝄞", 24.dp.toPx(), centerY + 1.8f * lineSpacing, paint)
    }
}

/**
 * Calculates vertical step offset on the Treble staff where E4 = 0.
 * E4 = 64 (step 0), F4 = 65 (step 1), G4 = 67 (step 2), A4 = 69 (step 3),
 * B4 = 71 (step 4), C5 = 72 (step 5), D5 = 74 (step 6), E5 = 76 (step 7),
 * F5 = 77 (step 8), G5 = 79 (step 9).
 * Below staff: D4 = 62 (step -1), C4 = 60 (step -2).
 */
private fun diatonicStepFromE4(midi: Int): Int {
    return when (midi) {
        60 -> -2 // C4 (Middle C)
        61 -> -2 // C#4
        62 -> -1 // D4
        63 -> -1 // D#4
        64 -> 0  // E4 (Line 1)
        65 -> 1  // F4 (Space 1)
        66 -> 1  // F#4
        67 -> 2  // G4 (Line 2)
        68 -> 2  // G#4
        69 -> 3  // A4 (Space 2)
        70 -> 3  // A#4
        71 -> 4  // B4 (Line 3)
        72 -> 5  // C5 (Space 3)
        73 -> 5  // C#5
        74 -> 6  // D5 (Line 4)
        75 -> 6  // D#5
        76 -> 7  // E5 (Space 4)
        77 -> 8  // F5 (Line 5)
        78 -> 8  // F#5
        79 -> 9  // G5 (Space above)
        else -> 0
    }
}

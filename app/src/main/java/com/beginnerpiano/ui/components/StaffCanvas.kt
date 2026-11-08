package com.beginnerpiano.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.beginnerpiano.data.models.NotationSystem
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.practice.FingeringEngine
import com.beginnerpiano.practice.NoteFeedbackStatus
import com.beginnerpiano.ui.theme.Emerald500
import com.beginnerpiano.ui.theme.Indigo500
import com.beginnerpiano.ui.theme.Rose500
import com.beginnerpiano.ui.theme.Slate400
import com.beginnerpiano.ui.theme.Slate700
import kotlin.math.roundToInt

@Composable
fun StaffCanvas(
    notes: List<NoteEvent>,
    activeIndex: Int,
    feedbackStatus: NoteFeedbackStatus,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(160.dp),
    notation: NotationSystem = NotationSystem.SOLFEGE,
    loopStartIndex: Int? = null,
    loopEndIndex: Int? = null,
    onNoteTapped: ((Int) -> Unit)? = null
) {
    val staffLineColor = Slate400
    val activeColor = when (feedbackStatus) {
        NoteFeedbackStatus.HIT -> Emerald500
        NoteFeedbackStatus.MISMATCH -> Rose500
        NoteFeedbackStatus.IDLE -> Indigo500
    }

    val pointerModifier = if (onNoteTapped != null && notes.isNotEmpty()) {
        Modifier.pointerInput(notes, activeIndex) {
            detectTapGestures { tapOffset ->
                val noteSpacing = 55.dp.toPx()
                val anchorX = size.width * 0.28f
                val tappedIndex = ((tapOffset.x - anchorX) / noteSpacing + activeIndex).roundToInt()
                    .coerceIn(0, notes.lastIndex)
                onNoteTapped(tappedIndex)
            }
        }
    } else Modifier

    Canvas(modifier = modifier.then(pointerModifier)) {
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

        // Draw Active A-B Loop highlight bracket & region
        if (loopStartIndex != null && loopEndIndex != null && loopStartIndex <= loopEndIndex) {
            val loopStartX = anchorX + (loopStartIndex - activeIndex) * noteSpacing - 22.dp.toPx()
            val loopEndX = anchorX + (loopEndIndex - activeIndex) * noteSpacing + 22.dp.toPx()

            drawRect(
                color = Indigo500.copy(alpha = 0.08f),
                topLeft = Offset(loopStartX, 12.dp.toPx()),
                size = Size(maxOf(0f, loopEndX - loopStartX), height - 24.dp.toPx())
            )
            drawLine(
                color = Indigo500.copy(alpha = 0.8f),
                start = Offset(loopStartX, 12.dp.toPx()),
                end = Offset(loopStartX, height - 12.dp.toPx()),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Indigo500.copy(alpha = 0.8f),
                start = Offset(loopEndX, 12.dp.toPx()),
                end = Offset(loopEndX, height - 12.dp.toPx()),
                strokeWidth = 2.5f
            )
            drawContext.canvas.nativeCanvas.apply {
                val tagPaint = android.graphics.Paint().apply {
                    color = Indigo500.hashCode()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
                drawText("A", loopStartX, 24.dp.toPx(), tagPaint)
                drawText("B", loopEndX, 24.dp.toPx(), tagPaint)
            }
        }

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

                // Accidentals: Draw Sharp symbol if note is sharp (REQ-005)
                if (NoteNotationHelper.isSharp(note.midiNote)) {
                    drawContext.canvas.nativeCanvas.apply {
                        val sharpPaint = android.graphics.Paint().apply {
                            color = noteColor.hashCode()
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isFakeBoldText = true
                        }
                        drawText("♯", noteX - 11.dp.toPx(), noteY + 5.dp.toPx(), sharpPaint)
                    }
                }

                // Duration-accurate Note Head (REQ-005)
                val headType = NoteNotationHelper.getNoteHeadType(note.durationBeats)
                if (headType == NoteHeadType.WHOLE || headType == NoteHeadType.HALF) {
                    // Hollow oval for WHOLE and HALF
                    drawOval(
                        color = noteColor,
                        topLeft = Offset(noteX - 9.dp.toPx(), noteY - 6.5.dp.toPx()),
                        size = Size(18.dp.toPx(), 13.dp.toPx()),
                        style = Stroke(width = 2.4f)
                    )
                } else {
                    // Filled oval for QUARTER and EIGHTH
                    drawOval(
                        color = noteColor,
                        topLeft = Offset(noteX - 9.dp.toPx(), noteY - 6.5.dp.toPx()),
                        size = Size(18.dp.toPx(), 13.dp.toPx())
                    )
                }

                // Augmentation Dot (REQ-005)
                if (NoteNotationHelper.isDotted(note.durationBeats)) {
                    drawCircle(
                        color = noteColor,
                        radius = 2.5.dp.toPx(),
                        center = Offset(noteX + 14.dp.toPx(), noteY)
                    )
                }

                // Note Stem and Flag (REQ-005)
                if (headType != NoteHeadType.WHOLE) {
                    // Vertical stem pointing up
                    drawLine(
                        color = noteColor,
                        start = Offset(noteX + 8.dp.toPx(), noteY),
                        end = Offset(noteX + 8.dp.toPx(), noteY - 36.dp.toPx()),
                        strokeWidth = 2.2f
                    )

                    // Eighth-note flag
                    if (headType == NoteHeadType.EIGHTH) {
                        val flagPath = Path().apply {
                            moveTo(noteX + 8.dp.toPx(), noteY - 36.dp.toPx())
                            quadraticTo(
                                noteX + 18.dp.toPx(), noteY - 26.dp.toPx(),
                                noteX + 16.dp.toPx(), noteY - 14.dp.toPx()
                            )
                        }
                        drawPath(path = flagPath, color = noteColor, style = Stroke(width = 2.5f))
                    }
                }

                // Draw subtle finger number for note
                val finger = FingeringEngine.resolveFinger(note)
                val fingerColor = FingeringEngine.getFingerColor(finger)
                drawContext.canvas.nativeCanvas.apply {
                    val fPaint = android.graphics.Paint().apply {
                        color = if (isCurrent) fingerColor.hashCode() else Slate400.copy(alpha = 0.65f).hashCode()
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = isCurrent
                    }
                    drawText("$finger", noteX, noteY + 18.dp.toPx(), fPaint)
                }

                // Draw Note Name and Hand Fingering above current note
                if (isCurrent) {
                    val handCenterY = (noteY - 60.dp.toPx()).coerceAtLeast(24.dp.toPx())
                    drawHandFingering(
                        finger = finger,
                        centerX = noteX,
                        centerY = handCenterY,
                        size = 28.dp.toPx(),
                        activeColor = fingerColor,
                        showNumberBadge = true
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = activeColor.hashCode()
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                        val textY = (noteY - 38.dp.toPx()).coerceAtLeast(handCenterY + 22.dp.toPx())
                        drawText(NoteEvent.midiToNoteName(note.midiNote, notation), noteX, textY, paint)
                    }
                }
            }
        }
    }
}

internal fun DrawScope.drawTrebleClefIndicator(
    centerY: Float,
    lineSpacing: Float,
    x: Float = 24.dp.toPx(),
    textSize: Float = 140f
) {
    // Stylized G-clef anchor glyph on left
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            this.textSize = textSize
            textAlign = android.graphics.Paint.Align.LEFT
        }
        drawText("𝄞", x, centerY + 1.8f * lineSpacing, paint)
    }
}

internal fun diatonicStepFromE4(midi: Int): Int {
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

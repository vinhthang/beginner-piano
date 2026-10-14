package com.beginnerpiano.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
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

/**
 * Multi-system wrapped sheet music renderer for landscape and score view.
 * Renders the entire musical piece divided across 2 to 4 systems (lines) of 5-line treble staves,
 * allowing players to read the full score while practicing.
 */
@Composable
fun WrappedStaffCanvas(
    notes: List<NoteEvent>,
    activeIndex: Int,
    feedbackStatus: NoteFeedbackStatus,
    modifier: Modifier = Modifier,
    notation: NotationSystem = NotationSystem.SOLFEGE
) {
    if (notes.isEmpty()) return

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        val totalNotes = notes.size
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        val leftMargin = 48.dp
        val rightMargin = 20.dp
        val usableWidth = (availableWidth - leftMargin - rightMargin).coerceAtLeast(180.dp)

        // Compute notes per line and number of systems to wrap cleanly
        val targetNotesPerRow = (usableWidth / 52.dp).toInt().coerceIn(6, 12)
        val systemCount = when {
            totalNotes <= 8 -> 1
            totalNotes <= 16 -> 2
            totalNotes <= 27 -> 3
            totalNotes <= 40 -> 4
            else -> ((totalNotes + targetNotesPerRow - 1) / targetNotesPerRow).coerceAtLeast(1)
        }
        val notesPerRow = ((totalNotes + systemCount - 1) / systemCount).coerceAtLeast(4)

        // System height allocation
        val minSystemHeight = 84.dp
        val computedSystemHeight = if (availableHeight > 0.dp && availableHeight != Dp.Infinity) {
            (availableHeight / systemCount).coerceAtLeast(minSystemHeight)
        } else {
            minSystemHeight
        }
        val totalCanvasHeight = computedSystemHeight * systemCount
        val isScrollable = totalCanvasHeight > availableHeight && availableHeight > 0.dp && availableHeight != Dp.Infinity
        val scrollState = rememberScrollState()

        val activeSystem = (activeIndex / notesPerRow).coerceIn(0, (systemCount - 1).coerceAtLeast(0))

        LaunchedEffect(activeSystem) {
            if (isScrollable) {
                val targetPx = with(density) { (computedSystemHeight * activeSystem).toPx() }
                scrollState.animateScrollTo(targetPx.toInt())
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isScrollable) Modifier.verticalScroll(scrollState) else Modifier)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalCanvasHeight)
            ) {
                val width = size.width
                val leftMarginPx = leftMargin.toPx()
                val rightMarginPx = rightMargin.toPx()
                val sysHeightPx = computedSystemHeight.toPx()

                val staffLineColor = Slate400
                val activeColor = when (feedbackStatus) {
                    NoteFeedbackStatus.HIT -> Emerald500
                    NoteFeedbackStatus.MISMATCH -> Rose500
                    NoteFeedbackStatus.IDLE -> Indigo500
                }

                for (s in 0 until systemCount) {
                    val sysTop = s * sysHeightPx
                    val sysCenterY = sysTop + sysHeightPx / 2f
                    val lineSpacing = 11.dp.toPx()
                    val line1Y = sysCenterY - 2 * lineSpacing
                    val line5Y = sysCenterY + 2 * lineSpacing

                    // 1. Draw 5 Staff lines per system
                    for (lineIdx in 0 until 5) {
                        val lineY = sysCenterY + (lineIdx - 2) * lineSpacing
                        drawLine(
                            color = staffLineColor.copy(alpha = 0.55f),
                            start = Offset(leftMarginPx, lineY),
                            end = Offset(width - rightMarginPx, lineY),
                            strokeWidth = 1.6f
                        )
                    }

                    // 2. Start & End Barlines
                    drawLine(
                        color = staffLineColor.copy(alpha = 0.8f),
                        start = Offset(leftMarginPx, line1Y),
                        end = Offset(leftMarginPx, line5Y),
                        strokeWidth = 2.2f
                    )
                    drawLine(
                        color = staffLineColor.copy(alpha = 0.8f),
                        start = Offset(width - rightMarginPx, line1Y),
                        end = Offset(width - rightMarginPx, line5Y),
                        strokeWidth = 2.2f
                    )

                    // 3. Treble Clef glyph indicator
                    drawTrebleClefIndicator(
                        centerY = sysCenterY,
                        lineSpacing = lineSpacing,
                        x = leftMarginPx + 4.dp.toPx(),
                        textSize = 100f
                    )

                    // 4. Notes in system
                    val startIdx = s * notesPerRow
                    val endIdx = minOf((s + 1) * notesPerRow, totalNotes)
                    val count = endIdx - startIdx
                    if (count <= 0) continue

                    val notesStartX = leftMarginPx + 38.dp.toPx()
                    val notesEndX = width - rightMarginPx - 20.dp.toPx()
                    val colSpacing = if (notesPerRow > 1) {
                        (notesEndX - notesStartX) / (notesPerRow - 1)
                    } else {
                        48.dp.toPx()
                    }

                    for (col in 0 until count) {
                        val noteIdx = startIdx + col
                        val note = notes[noteIdx]
                        val noteX = notesStartX + col * colSpacing
                        val isCurrent = (noteIdx == activeIndex)
                        val isPast = (noteIdx < activeIndex)

                        // Measure divider barline (every 4 notes, if not the last note in system)
                        if (col % 4 == 3 && col < count - 1) {
                            val barX = noteX + colSpacing / 2f
                            drawLine(
                                color = staffLineColor.copy(alpha = 0.4f),
                                start = Offset(barX, line1Y),
                                end = Offset(barX, line5Y),
                                strokeWidth = 1.2f
                            )
                        }

                        val step = diatonicStepFromE4(note.midiNote)
                        val noteY = line5Y - step * (lineSpacing / 2f)

                        val noteColor = when {
                            isCurrent -> activeColor
                            isPast -> Slate400.copy(alpha = 0.45f)
                            else -> Slate700
                        }

                        // Active note highlight glow / playhead outline
                        if (isCurrent) {
                            drawRoundRect(
                                color = activeColor.copy(alpha = 0.12f),
                                topLeft = Offset(noteX - 16.dp.toPx(), sysCenterY - 32.dp.toPx()),
                                size = Size(32.dp.toPx(), 64.dp.toPx()),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                            drawRoundRect(
                                color = activeColor.copy(alpha = 0.7f),
                                topLeft = Offset(noteX - 16.dp.toPx(), sysCenterY - 32.dp.toPx()),
                                size = Size(32.dp.toPx(), 64.dp.toPx()),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                                style = Stroke(width = 2f)
                            )
                        }

                        // Middle C Ledger Line
                        if (note.midiNote == 60) {
                            drawLine(
                                color = noteColor,
                                start = Offset(noteX - 14.dp.toPx(), noteY),
                                end = Offset(noteX + 14.dp.toPx(), noteY),
                                strokeWidth = 2.2f
                            )
                        }

                        // Note Head
                        drawOval(
                            color = noteColor,
                            topLeft = Offset(noteX - 8.dp.toPx(), noteY - 6.dp.toPx()),
                            size = Size(16.dp.toPx(), 12.dp.toPx())
                        )

                        // Note Stem (pointing up)
                        drawLine(
                            color = noteColor,
                            start = Offset(noteX + 7.dp.toPx(), noteY),
                            end = Offset(noteX + 7.dp.toPx(), noteY - 28.dp.toPx()),
                            strokeWidth = 2f
                        )

                        // Subtle finger badge below note
                        val finger = FingeringEngine.resolveFinger(note)
                        val fingerColor = FingeringEngine.getFingerColor(finger)
                        drawContext.canvas.nativeCanvas.apply {
                            val fPaint = android.graphics.Paint().apply {
                                color = if (isCurrent) fingerColor.hashCode() else Slate400.copy(alpha = 0.65f).hashCode()
                                textSize = 22f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = isCurrent
                            }
                            drawText("$finger", noteX, noteY + 16.dp.toPx(), fPaint)
                        }

                        // Active Note Hand Fingering and Note Name
                        if (isCurrent) {
                            val handCenterY = (noteY - 56.dp.toPx()).coerceAtLeast(sysTop + 14.dp.toPx())
                            drawHandFingering(
                                finger = finger,
                                centerX = noteX,
                                centerY = handCenterY,
                                size = 26.dp.toPx(),
                                activeColor = fingerColor,
                                showNumberBadge = true
                            )

                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = activeColor.hashCode()
                                    textSize = 28f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                                val textY = (noteY - 34.dp.toPx()).coerceAtLeast(handCenterY + 18.dp.toPx())
                                drawText(NoteEvent.midiToNoteName(note.midiNote, notation), noteX, textY, paint)
                            }
                        }
                    }
                }
            }
        }
    }
}

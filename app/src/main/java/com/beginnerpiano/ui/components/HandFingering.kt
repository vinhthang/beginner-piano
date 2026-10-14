package com.beginnerpiano.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beginnerpiano.practice.FingeringEngine

/**
 * Draws a stylized 5-finger hand graphic on Compose Canvas.
 *
 * @param finger The active finger to highlight (1 = Thumb .. 5 = Pinky).
 * @param centerX Horizontal center coordinate.
 * @param centerY Vertical center coordinate.
 * @param size Overall scale/diameter of the hand graphic.
 * @param activeColor Color used to highlight the active finger. Defaults to [FingeringEngine.getFingerColor].
 * @param inactiveFillColor Color used for non-active finger fills.
 * @param inactiveStrokeColor Color used for non-active finger outlines.
 * @param showNumberBadge When true and [finger] in 1..5, renders a circular digit badge in the palm.
 */
fun DrawScope.drawHandFingering(
    finger: Int,
    centerX: Float,
    centerY: Float,
    size: Float = 32f,
    activeColor: Color = FingeringEngine.getFingerColor(finger),
    inactiveFillColor: Color = Color(0x3394A3B8),
    inactiveStrokeColor: Color = Color(0x8894A3B8),
    showNumberBadge: Boolean = true
) {
    val w = size
    val h = size * 1.08f
    val strokeWidth = (size * 0.045f).coerceAtLeast(1.5f)
    val stroke = Stroke(width = strokeWidth)

    // Palm base
    val palmTop = centerY - h * 0.05f
    val palmHeight = h * 0.48f
    val palmWidth = w * 0.72f
    val palmLeft = centerX - palmWidth / 2f
    val palmCorner = CornerRadius(w * 0.20f, w * 0.20f)

    drawRoundRect(
        color = inactiveFillColor,
        topLeft = Offset(palmLeft, palmTop),
        size = Size(palmWidth, palmHeight),
        cornerRadius = palmCorner
    )
    drawRoundRect(
        color = inactiveStrokeColor,
        topLeft = Offset(palmLeft, palmTop),
        size = Size(palmWidth, palmHeight),
        cornerRadius = palmCorner,
        style = stroke
    )

    // 5 Fingers definition (Pianist's perspective, right hand):
    // 1 = Thumb (left, angled outward)
    // 2 = Index (left-center)
    // 3 = Middle (center, tallest)
    // 4 = Ring (right-center)
    // 5 = Pinky (right, shortest)
    val fingerWidth = w * 0.13f
    val fingerCorner = CornerRadius(fingerWidth / 2f, fingerWidth / 2f)

    data class FingerDef(
        val index: Int,
        val xOffset: Float,
        val topOffset: Float,
        val fingerHeight: Float,
        val angle: Float = 0f
    )

    val fingers = listOf(
        FingerDef(1, -w * 0.36f, -h * 0.16f, h * 0.40f, -22f), // Thumb
        FingerDef(2, -w * 0.16f, -h * 0.42f, h * 0.48f, -4f),  // Index
        FingerDef(3, 0f,         -h * 0.50f, h * 0.55f, 0f),   // Middle
        FingerDef(4, w * 0.16f,  -h * 0.43f, h * 0.49f, 4f),   // Ring
        FingerDef(5, w * 0.31f,  -h * 0.30f, h * 0.38f, 12f)   // Pinky
    )

    for (f in fingers) {
        val isActive = (f.index == finger)
        val fillCol = if (isActive) activeColor else inactiveFillColor
        val strokeCol = if (isActive) activeColor else inactiveStrokeColor

        withTransform({
            if (f.angle != 0f) {
                rotate(f.angle, pivot = Offset(centerX + f.xOffset, centerY))
            }
        }) {
            val fLeft = centerX + f.xOffset - fingerWidth / 2f
            val fTop = centerY + f.topOffset
            val fSize = Size(fingerWidth, f.fingerHeight)

            // Fill
            drawRoundRect(
                color = fillCol,
                topLeft = Offset(fLeft, fTop),
                size = fSize,
                cornerRadius = fingerCorner
            )
            // Stroke
            drawRoundRect(
                color = strokeCol,
                topLeft = Offset(fLeft, fTop),
                size = fSize,
                cornerRadius = fingerCorner,
                style = stroke
            )

            // Subtle highlight on active fingertip
            if (isActive) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.65f),
                    radius = fingerWidth * 0.22f,
                    center = Offset(centerX + f.xOffset, fTop + fingerWidth * 0.55f)
                )
            }
        }
    }

    // Palm center badge showing active finger digit
    if (showNumberBadge && finger in 1..5) {
        val badgeCenterY = centerY + h * 0.19f
        val badgeRadius = w * 0.18f

        drawCircle(
            color = activeColor,
            radius = badgeRadius,
            center = Offset(centerX, badgeCenterY)
        )

        drawContext.canvas.nativeCanvas.apply {
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = size * 0.26f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            val yPos = badgeCenterY - (textPaint.descent() + textPaint.ascent()) / 2f
            drawText("$finger", centerX, yPos, textPaint)
        }
    }
}

/**
 * Composable badge displaying the hand icon and finger label.
 */
@Composable
fun HandFingeringBadge(
    finger: Int,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val fingerColor = FingeringEngine.getFingerColor(finger)
    val fingerName = FingeringEngine.getFingerName(finger)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(fingerColor.copy(alpha = 0.12f))
            .border(1.dp, fingerColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val s = minOf(this.size.width, this.size.height)
            drawHandFingering(
                finger = finger,
                centerX = this.size.width / 2f,
                centerY = this.size.height / 2f,
                size = s * 0.85f,
                activeColor = fingerColor
            )
        }
        Text(
            text = fingerName,
            color = fingerColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

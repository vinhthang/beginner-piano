package com.beginnerpiano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.practice.NoteFeedbackStatus
import com.beginnerpiano.ui.theme.Emerald500
import com.beginnerpiano.ui.theme.Indigo100
import com.beginnerpiano.ui.theme.Indigo500
import com.beginnerpiano.ui.theme.Rose500
import com.beginnerpiano.ui.theme.Slate800
import com.beginnerpiano.ui.theme.Slate900

private data class KeySpec(
    val midi: Int,
    val name: String,
    val isBlack: Boolean,
    val blackKeyOffsetDp: Float = 0f
)

@Composable
fun PianoKeyboard(
    targetMidi: Int?,
    detectedMidi: Int?,
    feedbackStatus: NoteFeedbackStatus,
    onKeyTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // 2-Octave Keyboard (C4 to B5: MIDI 60 to 83)
    val whiteKeyWidth = 44.dp
    val blackKeyWidth = 26.dp
    val whiteKeyHeight = 150.dp
    val blackKeyHeight = 92.dp

    val keys = listOf(
        KeySpec(60, "C4", false),
        KeySpec(61, "C#4", true, 31f),
        KeySpec(62, "D4", false),
        KeySpec(63, "D#4", true, 75f),
        KeySpec(64, "E4", false),
        KeySpec(65, "F4", false),
        KeySpec(66, "F#4", true, 163f),
        KeySpec(67, "G4", false),
        KeySpec(68, "G#4", true, 207f),
        KeySpec(69, "A4", false),
        KeySpec(70, "A#4", true, 251f),
        KeySpec(71, "B4", false),

        KeySpec(72, "C5", false),
        KeySpec(73, "C#5", true, 339f),
        KeySpec(74, "D5", false),
        KeySpec(75, "D#5", true, 383f),
        KeySpec(76, "E5", false),
        KeySpec(77, "F5", false),
        KeySpec(78, "F#5", true, 471f),
        KeySpec(79, "G5", false),
        KeySpec(80, "G#5", true, 515f),
        KeySpec(81, "A5", false),
        KeySpec(82, "A#5", true, 559f),
        KeySpec(83, "B5", false)
    )

    val whiteKeys = keys.filter { !it.isBlack }
    val blackKeys = keys.filter { it.isBlack }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Slate900)
            .padding(10.dp)
            .horizontalScroll(scrollState)
    ) {
        // White Keys Layer
        Row {
            whiteKeys.forEach { key ->
                val isTarget = (key.midi == targetMidi)
                val isDetected = (key.midi == detectedMidi)

                val bgColor = when {
                    isDetected && feedbackStatus == NoteFeedbackStatus.HIT -> Emerald500
                    isDetected && feedbackStatus == NoteFeedbackStatus.MISMATCH -> Rose500
                    isTarget -> Indigo100
                    else -> Color.White
                }

                val borderColor = if (isTarget) Indigo500 else Color(0xFFCBD5E1)

                Box(
                    modifier = Modifier
                        .width(whiteKeyWidth)
                        .height(whiteKeyHeight)
                        .padding(horizontal = 1.dp)
                        .shadow(2.dp, RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                        .background(bgColor, RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                        .border(
                            width = if (isTarget) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp)
                        )
                        .clickable { onKeyTapped(key.midi) },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = key.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTarget) Indigo500 else Slate800,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }

        // Black Keys Overlay
        blackKeys.forEach { key ->
            val isTarget = (key.midi == targetMidi)
            val isDetected = (key.midi == detectedMidi)

            val bgColor = when {
                isDetected && feedbackStatus == NoteFeedbackStatus.HIT -> Emerald500
                isDetected && feedbackStatus == NoteFeedbackStatus.MISMATCH -> Rose500
                isTarget -> Indigo500
                else -> Color(0xFF1E293B)
            }

            Box(
                modifier = Modifier
                    .offset(x = key.blackKeyOffsetDp.dp)
                    .width(blackKeyWidth)
                    .height(blackKeyHeight)
                    .zIndex(10f)
                    .shadow(4.dp, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                    .background(bgColor, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                    .clickable { onKeyTapped(key.midi) }
            )
        }
    }
}

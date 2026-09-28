package com.beginnerpiano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beginnerpiano.audio.DetectedPitch
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.practice.NoteFeedbackStatus
import com.beginnerpiano.practice.PracticeState
import com.beginnerpiano.ui.theme.Emerald500
import com.beginnerpiano.ui.theme.Indigo100
import com.beginnerpiano.ui.theme.Indigo500
import com.beginnerpiano.ui.theme.Rose500
import com.beginnerpiano.ui.theme.Slate400
import com.beginnerpiano.ui.theme.Slate700

@Composable
fun PracticeHud(
    practiceState: PracticeState,
    detectedPitch: DetectedPitch,
    modifier: Modifier = Modifier
) {
    val targetNote = practiceState.currentTargetNote
    val targetName = targetNote?.noteName ?: "--"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Target Note Card
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Indigo100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = targetName,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Indigo500
                    )
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "TARGET",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                    Text(
                        text = targetName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 2. Microphone / Detected Note Card
        Box(
            modifier = Modifier
                .weight(1.3f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            val statusColor = when (practiceState.feedbackStatus) {
                NoteFeedbackStatus.HIT -> Emerald500
                NoteFeedbackStatus.MISMATCH -> Rose500
                NoteFeedbackStatus.IDLE -> if (!detectedPitch.isSilent) Indigo500 else Slate400
            }

            val statusText = when (practiceState.feedbackStatus) {
                NoteFeedbackStatus.HIT -> "Match: ${practiceState.lastDetectedName}"
                NoteFeedbackStatus.MISMATCH -> "Heard: ${practiceState.lastDetectedName} (Expected: $targetName)"
                NoteFeedbackStatus.IDLE -> if (!detectedPitch.isSilent) "Heard: ${detectedPitch.noteName}" else "Listening..."
            }

            Column {
                Text(
                    text = "MICROPHONE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400
                )
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    maxLines = 1
                )
            }
        }

        // 3. Progress Card
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PROGRESS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                    Text(
                        text = "${practiceState.currentNoteIndex}/${practiceState.song.notes.size}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { practiceState.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Indigo500,
                    trackColor = Slate400.copy(alpha = 0.2f)
                )
            }
        }
    }
}

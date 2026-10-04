package com.beginnerpiano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.beginnerpiano.audio.PitchDetectorType
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
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    onTogglePitchDetector: () -> Unit = onOpenSettings
) {
    val notation = practiceState.notationSystem
    val targetNote = practiceState.currentTargetNote
    val targetName = targetNote?.let { NoteEvent.midiToNoteName(it.midiNote, notation) } ?: "--"

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
            val statusColor = when {
                practiceState.isPlayingDemo -> Emerald500
                practiceState.feedbackStatus == NoteFeedbackStatus.HIT -> Emerald500
                practiceState.feedbackStatus == NoteFeedbackStatus.MISMATCH -> Rose500
                else -> if (!detectedPitch.isSilent) Indigo500 else Slate400
            }

            val headerText = if (practiceState.isPlayingDemo) "DEMO PLAYBACK" else "MICROPHONE"
            val detectedNoteName = if (!detectedPitch.isSilent) NoteEvent.midiToNoteName(detectedPitch.midiNote, notation) else "--"

            val statusText = when {
                practiceState.isPlayingDemo -> "Playing: $targetName"
                practiceState.feedbackStatus == NoteFeedbackStatus.HIT -> "Match: ${practiceState.lastDetectedName}"
                practiceState.feedbackStatus == NoteFeedbackStatus.MISMATCH -> "Heard: ${practiceState.lastDetectedName} (Expected: $targetName)"
                else -> if (!detectedPitch.isSilent) "Heard: $detectedNoteName" else "Listening..."
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = headerText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (practiceState.isPlayingDemo) Emerald500 else Slate400
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(enabled = !practiceState.isPlayingDemo, onClick = onOpenSettings)
                            .background(Indigo500.copy(alpha = 0.12f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (practiceState.pitchDetectorType == PitchDetectorType.YIN) "⚡ YIN (DSP)" else "🧠 SPICE (AI)",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Indigo500
                        )
                    }
                }
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

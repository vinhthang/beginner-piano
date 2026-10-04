package com.beginnerpiano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.beginnerpiano.audio.PitchDetectorType
import com.beginnerpiano.data.models.NotationSystem
import com.beginnerpiano.practice.PracticeMode
import com.beginnerpiano.ui.theme.Emerald500
import com.beginnerpiano.ui.theme.Indigo500
import com.beginnerpiano.ui.theme.Slate400

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentNotation: NotationSystem,
    onNotationChanged: (NotationSystem) -> Unit,
    currentPitchDetector: PitchDetectorType,
    onPitchDetectorChanged: (PitchDetectorType) -> Unit,
    currentMode: PracticeMode,
    onModeChanged: (PracticeMode) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙ App Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Section 1: Note Naming (i18n)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "NOTE NOTATION SYSTEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentNotation == NotationSystem.SOLFEGE,
                            onClick = { onNotationChanged(NotationSystem.SOLFEGE) },
                            label = {
                                Text(
                                    text = "🎼 Solfège (Do-Re-Mi)",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentNotation == NotationSystem.SOLFEGE) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentNotation == NotationSystem.LETTERS,
                            onClick = { onNotationChanged(NotationSystem.LETTERS) },
                            label = {
                                Text(
                                    text = "🔤 Letters (C-D-E)",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentNotation == NotationSystem.LETTERS) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section 2: Pitch Detection Engine
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PITCH DETECTION ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentPitchDetector == PitchDetectorType.YIN,
                            onClick = { onPitchDetectorChanged(PitchDetectorType.YIN) },
                            label = {
                                Text(
                                    text = "⚡ YIN (Fast DSP)",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentPitchDetector == PitchDetectorType.YIN) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentPitchDetector == PitchDetectorType.SPICE,
                            onClick = { onPitchDetectorChanged(PitchDetectorType.SPICE) },
                            label = {
                                Text(
                                    text = "🧠 SPICE (AI)",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentPitchDetector == PitchDetectorType.SPICE) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section 3: Practice Mode
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PRACTICE MODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentMode == PracticeMode.WAIT_FOR_NOTE,
                            onClick = { onModeChanged(PracticeMode.WAIT_FOR_NOTE) },
                            label = {
                                Text(
                                    text = "⏳ Wait-For-Note",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentMode == PracticeMode.WAIT_FOR_NOTE) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentMode == PracticeMode.TEMPO,
                            onClick = { onModeChanged(PracticeMode.TEMPO) },
                            label = {
                                Text(
                                    text = "⏱ Tempo",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentMode == PracticeMode.TEMPO) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Done Button
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
                ) {
                    Text(
                        text = "Done",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

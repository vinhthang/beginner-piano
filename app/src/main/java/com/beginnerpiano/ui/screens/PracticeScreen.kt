package com.beginnerpiano.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.beginnerpiano.audio.PitchDetectorType
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.practice.FingeringEngine
import com.beginnerpiano.practice.PracticeMode
import com.beginnerpiano.practice.PracticeViewModel
import com.beginnerpiano.ui.components.HandFingeringBadge
import com.beginnerpiano.ui.components.PianoKeyboard
import com.beginnerpiano.ui.components.PracticeHud
import com.beginnerpiano.ui.components.SettingsDialog
import com.beginnerpiano.ui.components.StaffCanvas
import com.beginnerpiano.ui.components.WrappedStaffCanvas
import com.beginnerpiano.ui.theme.Amber500
import com.beginnerpiano.ui.theme.Emerald500
import com.beginnerpiano.ui.theme.Indigo500
import com.beginnerpiano.ui.theme.Rose500
import com.beginnerpiano.ui.theme.Slate400

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeScreen(
    viewModel: PracticeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val practiceState by viewModel.practiceState.collectAsState()
    val detectedPitch by viewModel.detectedPitch.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(if (isLandscape) 10.dp else 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (isLandscape) {
            // Landscape Top Action Bar: compact unified Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button + Title & composer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("← Songs", fontSize = 11.sp)
                    }
                    Text(
                        text = "${practiceState.song.title} • ${practiceState.song.composer}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Inline HUD badges & controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Speed Multiplier Cycle Button (REQ-006)
                    OutlinedButton(
                        onClick = { viewModel.cycleTempoMultiplier() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("⚡ ${practiceState.tempoMultiplier}x", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // A-B Loop Toggle Button (REQ-007)
                    OutlinedButton(
                        onClick = { viewModel.toggleLoopRange() },
                        shape = RoundedCornerShape(8.dp),
                        colors = if (practiceState.isLoopActive) ButtonDefaults.outlinedButtonColors(containerColor = Indigo500.copy(alpha = 0.15f)) else ButtonDefaults.outlinedButtonColors(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val loopLabel = if (practiceState.isLoopActive) {
                            "🔁 [${practiceState.loopStartIndex}..${practiceState.loopEndIndex}]"
                        } else {
                            "🔁 Loop"
                        }
                        Text(loopLabel, fontSize = 11.sp, fontWeight = if (practiceState.isLoopActive) FontWeight.Bold else FontWeight.Normal)
                    }

                    // Tempo Mode Start/Pause & Metronome Toggle (REQ-008)
                    if (practiceState.mode == PracticeMode.TEMPO) {
                        Button(
                            onClick = { viewModel.toggleTempoClock() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (practiceState.isTempoRunning) Rose500 else Indigo500
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(if (practiceState.isTempoRunning) "⏸ Pause" else "▶ Beat", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.toggleMetronome() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(if (practiceState.metronomeEnabled) "🔔" else "🔕", fontSize = 11.sp)
                        }
                    }

                    // Play Demo / Stop button
                    Button(
                        onClick = { viewModel.toggleDemoPlayback() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (practiceState.isPlayingDemo) Rose500 else Emerald500
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (practiceState.isPlayingDemo) "⏹ Stop" else "▶ Demo",
                            fontSize = 11.sp
                        )
                    }

                    // Restart button
                    OutlinedButton(
                        onClick = { viewModel.restart() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Restart", fontSize = 11.sp)
                    }

                    // Settings button
                    OutlinedButton(
                        onClick = { showSettings = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("⚙", fontSize = 11.sp)
                    }
                }
            }
        } else {
            // 1. Portrait Top Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("← Songs", fontSize = 12.sp)
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = practiceState.song.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = practiceState.song.composer,
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Play Demo / Stop Button
                    Button(
                        onClick = { viewModel.toggleDemoPlayback() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (practiceState.isPlayingDemo) Rose500 else Emerald500
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (practiceState.isPlayingDemo) "⏹ Stop" else "▶ Demo",
                            fontSize = 11.sp
                        )
                    }

                    // Restart Button
                    OutlinedButton(
                        onClick = { viewModel.restart() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("Restart", fontSize = 11.sp)
                    }

                    // Settings Button
                    OutlinedButton(
                        onClick = { showSettings = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("⚙", fontSize = 11.sp)
                    }
                }
            }

            // Practice Toolbar Row: Speed + A-B Loop + Tempo clock controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed Chip
                OutlinedButton(
                    onClick = { viewModel.cycleTempoMultiplier() },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("⚡ ${practiceState.tempoMultiplier}x", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // A-B Loop Chip
                OutlinedButton(
                    onClick = { viewModel.toggleLoopRange() },
                    shape = RoundedCornerShape(8.dp),
                    colors = if (practiceState.isLoopActive) ButtonDefaults.outlinedButtonColors(containerColor = Indigo500.copy(alpha = 0.15f)) else ButtonDefaults.outlinedButtonColors(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    val label = if (practiceState.isLoopActive) {
                        "🔁 [${practiceState.loopStartIndex}..${practiceState.loopEndIndex}]"
                    } else {
                        "🔁 A-B Loop"
                    }
                    Text(label, fontSize = 11.sp, fontWeight = if (practiceState.isLoopActive) FontWeight.Bold else FontWeight.Normal)
                }

                // In Tempo Mode: Beat Clock + Metronome
                if (practiceState.mode == PracticeMode.TEMPO) {
                    Button(
                        onClick = { viewModel.toggleTempoClock() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (practiceState.isTempoRunning) Rose500 else Indigo500
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(if (practiceState.isTempoRunning) "⏸ Pause" else "▶ Beat", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.toggleMetronome() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(if (practiceState.metronomeEnabled) "🔔 Click" else "🔕 Mute", fontSize = 11.sp)
                    }
                }
            }

            // 2. Portrait Real-Time HUD
            PracticeHud(
                practiceState = practiceState,
                detectedPitch = detectedPitch,
                onOpenSettings = { showSettings = true },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // 3. Musical Staff Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = if (isLandscape) 4.dp else 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (isLandscape) {
                WrappedStaffCanvas(
                    notes = practiceState.song.notes,
                    activeIndex = practiceState.currentNoteIndex,
                    feedbackStatus = practiceState.feedbackStatus,
                    notation = practiceState.notationSystem,
                    loopStartIndex = practiceState.loopStartIndex,
                    loopEndIndex = practiceState.loopEndIndex,
                    onNoteTapped = { viewModel.onNoteTappedForLoop(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp, horizontal = 6.dp)
                )
            } else {
                StaffCanvas(
                    notes = practiceState.song.notes,
                    activeIndex = practiceState.currentNoteIndex,
                    feedbackStatus = practiceState.feedbackStatus,
                    notation = practiceState.notationSystem,
                    loopStartIndex = practiceState.loopStartIndex,
                    loopEndIndex = practiceState.loopEndIndex,
                    onNoteTapped = { viewModel.onNoteTappedForLoop(it) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp)
                )
            }
        }

        // 4. Interactive Reference Piano Keyboard
        PianoKeyboard(
            targetMidi = practiceState.currentTargetNote?.midiNote,
            detectedMidi = practiceState.lastDetectedMidi,
            feedbackStatus = practiceState.feedbackStatus,
            notation = practiceState.notationSystem,
            onKeyTapped = { midi -> viewModel.onKeyTapped(midi) },
            modifier = Modifier
                .padding(top = if (isLandscape) 4.dp else 8.dp)
                .height(if (isLandscape) 100.dp else 130.dp)
        )
    }

    // Settings Dialog
    if (showSettings) {
        SettingsDialog(
            currentNotation = practiceState.notationSystem,
            onNotationChanged = { viewModel.setNotationSystem(it) },
            currentPitchDetector = practiceState.pitchDetectorType,
            onPitchDetectorChanged = { viewModel.setPitchDetectorType(it) },
            currentMode = practiceState.mode,
            onModeChanged = { viewModel.setMode(it) },
            onDismissRequest = { showSettings = false },
            currentTempoMultiplier = practiceState.tempoMultiplier,
            onTempoMultiplierChanged = { viewModel.setTempoMultiplier(it) },
            metronomeEnabled = practiceState.metronomeEnabled,
            onMetronomeEnabledChanged = { viewModel.setMetronomeEnabled(it) }
        )
    }

    // Completion Dialog with 3-Star Rating (REQ-009)
    if (practiceState.isCompleted) {
        Dialog(onDismissRequest = { /* force action */ }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val stars = practiceState.earnedStars
                    val starString = "★".repeat(stars) + "☆".repeat(3 - stars)

                    Text(
                        text = "🌟 Exercise Completed!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    // 3-Star Rating Badge (REQ-009)
                    Text(
                        text = starString,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Amber500
                    )

                    Text(
                        text = "Great job playing ${practiceState.song.title}!",
                        fontSize = 13.sp,
                        color = Slate400
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Accuracy", fontSize = 11.sp, color = Slate400)
                            Text("${practiceState.accuracyPercent}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Emerald500)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Best Streak", fontSize = 11.sp, color = Slate400)
                            Text("${practiceState.bestStreak}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Indigo500)
                        }
                    }

                    Button(
                        onClick = { viewModel.restart() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500)
                    ) {
                        Text("Practice Again")
                    }

                    TextButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose Another Song")
                    }
                }
            }
        }
    }
}

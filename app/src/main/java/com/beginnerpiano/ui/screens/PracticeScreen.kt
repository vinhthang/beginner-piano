package com.beginnerpiano.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.beginnerpiano.audio.PitchDetectorType
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.practice.PracticeMode
import com.beginnerpiano.practice.PracticeViewModel
import com.beginnerpiano.ui.components.PianoKeyboard
import com.beginnerpiano.ui.components.PracticeHud
import com.beginnerpiano.ui.components.SettingsDialog
import com.beginnerpiano.ui.components.StaffCanvas
import androidx.compose.ui.platform.LocalConfiguration
import com.beginnerpiano.ui.theme.Emerald500
import com.beginnerpiano.ui.theme.Indigo500
import com.beginnerpiano.ui.theme.Rose500
import com.beginnerpiano.ui.theme.Slate400

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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Target note badge
                    val targetName = practiceState.currentTargetNote?.let {
                        NoteEvent.midiToNoteName(it.midiNote, practiceState.notationSystem)
                    } ?: "-"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Indigo500.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Target: $targetName",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Indigo500
                        )
                    }

                    // Streak badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Emerald500.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔥 ${practiceState.streak}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald500
                        )
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
                            text = if (practiceState.isPlayingDemo) "⏹ Stop" else "▶ Play Demo",
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
                        Text("⚙ Settings", fontSize = 11.sp)
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            text = if (practiceState.isPlayingDemo) "⏹ Stop" else "▶ Play Demo",
                            fontSize = 11.sp
                        )
                    }

                    // Restart Button
                    OutlinedButton(
                        onClick = { viewModel.restart() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Restart", fontSize = 11.sp)
                    }

                    // Settings Button
                    OutlinedButton(
                        onClick = { showSettings = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("⚙ Settings", fontSize = 11.sp)
                    }
                }
            }

            // 2. Portrait Real-Time HUD
            PracticeHud(
                practiceState = practiceState,
                detectedPitch = detectedPitch,
                onOpenSettings = { showSettings = true },
                modifier = Modifier.padding(vertical = 8.dp)
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
            StaffCanvas(
                notes = practiceState.song.notes,
                activeIndex = practiceState.currentNoteIndex,
                feedbackStatus = practiceState.feedbackStatus,
                notation = practiceState.notationSystem,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = if (isLandscape) 8.dp else 12.dp)
            )
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
            onDismissRequest = { showSettings = false }
        )
    }

    // Completion Dialog
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
                    Text(
                        text = "🌟 Exercise Completed!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
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

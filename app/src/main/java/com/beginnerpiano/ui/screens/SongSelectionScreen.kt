package com.beginnerpiano.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.beginnerpiano.data.models.DifficultyLevel
import com.beginnerpiano.data.models.NotationSystem
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.data.models.Song
import com.beginnerpiano.data.models.SongMastery
import com.beginnerpiano.data.repository.SongRepository
import com.beginnerpiano.ui.theme.Amber500
import com.beginnerpiano.ui.theme.Emerald500
import com.beginnerpiano.ui.theme.Indigo500
import com.beginnerpiano.ui.theme.Slate400

@Composable
fun SongSelectionScreen(
    songs: List<Song> = SongRepository.songs,
    masteryMap: Map<String, SongMastery> = emptyMap(),
    onSongSelected: (Song) -> Unit,
    onImportMidiRequested: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    notationSystem: NotationSystem = NotationSystem.SOLFEGE,
    onOpenSettings: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    var selectedFilter by remember { mutableStateOf<DifficultyLevel?>(null) }

    val displayedSongs = remember(songs, selectedFilter) {
        if (selectedFilter == null) {
            songs
        } else {
            songs.filter { it.difficulty == selectedFilter }
        }
    }

    val totalStars = remember(masteryMap) {
        masteryMap.values.sumOf { it.bestStars }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isLandscape) 4.dp else 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎹 Beginner Piano",
                        fontSize = if (isLandscape) 20.sp else 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Total Stars Mastery Badge (REQ-009)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Amber500.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⭐ $totalStars",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Amber500
                        )
                    }
                }
                Text(
                    text = "Progressive repertoire designed for new players",
                    fontSize = if (isLandscape) 11.sp else 13.sp,
                    color = Slate400
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // + Import MIDI Button (REQ-009)
                if (onImportMidiRequested != null) {
                    Button(
                        onClick = onImportMidiRequested,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("+ Import MIDI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("⚙ Settings", fontSize = 12.sp)
                }
            }
        }

        // Level Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = if (isLandscape) 4.dp else 8.dp)
        ) {
            item {
                FilterChip(
                    selected = (selectedFilter == null),
                    onClick = { selectedFilter = null },
                    label = { Text("All (${songs.size})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                val count = songs.count { it.difficulty == DifficultyLevel.BEGINNER }
                FilterChip(
                    selected = (selectedFilter == DifficultyLevel.BEGINNER),
                    onClick = { selectedFilter = DifficultyLevel.BEGINNER },
                    label = { Text("Level 1: 5-Finger ($count)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                val count = songs.count { it.difficulty == DifficultyLevel.ELEMENTARY }
                FilterChip(
                    selected = (selectedFilter == DifficultyLevel.ELEMENTARY),
                    onClick = { selectedFilter = DifficultyLevel.ELEMENTARY },
                    label = { Text("Level 2: 1-Octave ($count)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                val count = songs.count { it.difficulty == DifficultyLevel.INTERMEDIATE }
                FilterChip(
                    selected = (selectedFilter == DifficultyLevel.INTERMEDIATE),
                    onClick = { selectedFilter = DifficultyLevel.INTERMEDIATE },
                    label = { Text("Level 3: Classical ($count)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Text(
            text = "PROGRESSIVE CURRICULUM",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Slate400,
            modifier = Modifier.padding(top = if (isLandscape) 4.dp else 8.dp, bottom = if (isLandscape) 4.dp else 8.dp)
        )

        if (isLandscape) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedSongs) { song ->
                    SongCard(
                        song = song,
                        mastery = masteryMap[song.id],
                        notationSystem = notationSystem,
                        onClick = { onSongSelected(song) }
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedSongs) { song ->
                    SongCard(
                        song = song,
                        mastery = masteryMap[song.id],
                        notationSystem = notationSystem,
                        onClick = { onSongSelected(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SongCard(
    song: Song,
    mastery: SongMastery?,
    notationSystem: NotationSystem,
    onClick: () -> Unit
) {
    val (badgeText, badgeColor) = when (song.difficulty) {
        DifficultyLevel.BEGINNER -> "Level 1: 5-Finger" to Emerald500
        DifficultyLevel.ELEMENTARY -> "Level 2: 1-Octave" to Indigo500
        DifficultyLevel.INTERMEDIATE -> "Level 3: Classical" to Amber500
    }

    val noteRangeSummary = remember(song, notationSystem) {
        val uniqueNotes = song.notes.map { it.midiNote }.distinct().sorted()
        if (uniqueNotes.isNotEmpty()) {
            val minName = NoteEvent.midiToNoteName(uniqueNotes.first(), notationSystem)
            val maxName = NoteEvent.midiToNoteName(uniqueNotes.last(), notationSystem)
            "Range: $minName – $maxName"
        } else {
            ""
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = song.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }

                    // Star Rating Badge (REQ-009)
                    val stars = mastery?.bestStars ?: 0
                    if (stars > 0) {
                        Text(
                            text = "★".repeat(stars) + "☆".repeat(3 - stars),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Amber500
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 3.dp)
                ) {
                    Text(
                        text = "${song.composer} • ${song.notes.size} notes • ${song.defaultBpm} BPM",
                        fontSize = 12.sp,
                        color = Slate400
                    )

                    // Best Accuracy Badge (REQ-009)
                    if (mastery != null && mastery.playCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Emerald500.copy(alpha = 0.12f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${mastery.bestAccuracy}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald500
                            )
                        }
                    }
                }

                if (noteRangeSummary.isNotEmpty()) {
                    Text(
                        text = noteRangeSummary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = badgeColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Play", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

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
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
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
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.data.models.Song
import com.beginnerpiano.data.repository.SongRepository
import com.beginnerpiano.ui.theme.Amber500
import com.beginnerpiano.ui.theme.Emerald500
import com.beginnerpiano.ui.theme.Indigo500
import com.beginnerpiano.ui.theme.Slate400

@Composable
fun SongSelectionScreen(
    onSongSelected: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    var selectedFilter by remember { mutableStateOf<DifficultyLevel?>(null) }

    val displayedSongs = remember(selectedFilter) {
        if (selectedFilter == null) {
            SongRepository.songs
        } else {
            SongRepository.getSongsByDifficulty(selectedFilter!!)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // App Header
        Column(modifier = Modifier.padding(vertical = if (isLandscape) 4.dp else 10.dp)) {
            Text(
                text = "🎹 Beginner Piano",
                fontSize = if (isLandscape) 22.sp else 26.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Progressive repertoire designed for new players",
                fontSize = if (isLandscape) 12.sp else 13.sp,
                color = Slate400
            )
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
                    label = { Text("All (${SongRepository.songs.size})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                val count = SongRepository.getSongsByDifficulty(DifficultyLevel.BEGINNER).size
                FilterChip(
                    selected = (selectedFilter == DifficultyLevel.BEGINNER),
                    onClick = { selectedFilter = DifficultyLevel.BEGINNER },
                    label = { Text("Level 1: 5-Finger ($count)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                val count = SongRepository.getSongsByDifficulty(DifficultyLevel.ELEMENTARY).size
                FilterChip(
                    selected = (selectedFilter == DifficultyLevel.ELEMENTARY),
                    onClick = { selectedFilter = DifficultyLevel.ELEMENTARY },
                    label = { Text("Level 2: 1-Octave ($count)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                val count = SongRepository.getSongsByDifficulty(DifficultyLevel.INTERMEDIATE).size
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
    onClick: () -> Unit
) {
    val (badgeText, badgeColor) = when (song.difficulty) {
        DifficultyLevel.BEGINNER -> "Level 1: 5-Finger" to Emerald500
        DifficultyLevel.ELEMENTARY -> "Level 2: 1-Octave" to Indigo500
        DifficultyLevel.INTERMEDIATE -> "Level 3: Classical" to Amber500
    }

    val noteRangeSummary = remember(song) {
        val uniqueNotes = song.notes.map { it.midiNote }.distinct().sorted()
        if (uniqueNotes.isNotEmpty()) {
            val minName = NoteEvent.midiToNoteName(uniqueNotes.first())
            val maxName = NoteEvent.midiToNoteName(uniqueNotes.last())
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
                }
                Text(
                    text = "${song.composer} • ${song.notes.size} notes • ${song.defaultBpm} BPM",
                    fontSize = 12.sp,
                    color = Slate400,
                    modifier = Modifier.padding(top = 3.dp)
                )
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

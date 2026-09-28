package com.beginnerpiano

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.beginnerpiano.data.models.Song
import com.beginnerpiano.practice.PracticeViewModel
import com.beginnerpiano.ui.screens.PracticeScreen
import com.beginnerpiano.ui.screens.SongSelectionScreen
import com.beginnerpiano.ui.theme.BeginnerPianoTheme

enum class Screen {
    SONG_SELECTION,
    PRACTICE
}

class MainActivity : ComponentActivity() {

    private val practiceViewModel: PracticeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BeginnerPianoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf(Screen.SONG_SELECTION) }
                    var hasAudioPermission by remember {
                        mutableStateOf(
                            ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        hasAudioPermission = isGranted
                        if (isGranted) {
                            practiceViewModel.startListening()
                        }
                    }

                    LaunchedEffect(Unit) {
                        if (!hasAudioPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            practiceViewModel.startListening()
                        }
                    }

                    DisposableEffect(Unit) {
                        onDispose {
                            practiceViewModel.stopListening()
                        }
                    }

                    when (currentScreen) {
                        Screen.SONG_SELECTION -> {
                            SongSelectionScreen(
                                onSongSelected = { song ->
                                    practiceViewModel.selectSong(song)
                                    practiceViewModel.restart()
                                    currentScreen = Screen.PRACTICE
                                }
                            )
                        }
                        Screen.PRACTICE -> {
                            PracticeScreen(
                                viewModel = practiceViewModel,
                                onNavigateBack = {
                                    currentScreen = Screen.SONG_SELECTION
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            practiceViewModel.startListening()
        }
    }

    override fun onPause() {
        super.onPause()
        practiceViewModel.stopListening()
    }
}

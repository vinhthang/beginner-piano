# Beginner Piano Android App 🎹

An Android application built with Kotlin and Jetpack Compose designed to teach beginner piano players by listening to physical piano notes in real-time through the microphone and providing instant visual feedback on an interactive musical staff.

---

## Features

* **Real-Time Microphone Pitch Detection**: Uses the YIN algorithm with sub-bin parabolic interpolation and RMS noise gating to detect monophonic piano notes with low latency.
* **Interactive Musical Staff**: 5-line Treble Clef canvas showing notes, ledger lines (Middle C), and an animated playhead ribbon.
* **Wait-For-Note Practice Mode**: The sheet music pauses on each note until you strike the correct key on your piano.
* **Responsive Reference Piano**: On-screen 2-octave piano keyboard highlighting the active target key and showing real-time feedback (green on hit, red on mismatch). Also supports touch taps for testing without a real piano.
* **Curated Beginner Curriculum**:
  * *Ode to Joy* (Ludwig van Beethoven)
  * *Twinkle, Twinkle, Little Star* (Traditional)
  * *Mary Had a Little Lamb* (Traditional)
  * *C Major Scale Drill* (1-Octave Ascending & Descending)

---

## Project Structure

```
beginner-piano/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Audio permissions & launcher
│   │   ├── java/com/beginnerpiano/
│   │   │   ├── MainActivity.kt          # App entrypoint & permission handling
│   │   │   ├── audio/
│   │   │   │   ├── PitchDetector.kt     # Interface & DetectedPitch data class
│   │   │   │   ├── YinPitchDetector.kt  # Real-time YIN DSP algorithm
│   │   │   │   └── AudioCaptureService.kt # AudioRecord lifecycle & Flow
│   │   │   ├── data/
│   │   │   │   ├── models/Song.kt       # Song, NoteEvent, and pitch conversion
│   │   │   │   └── repository/SongRepository.kt # Bundled beginner library
│   │   │   ├── practice/
│   │   │   │   ├── PracticeEngine.kt    # Wait-for-Note state machine
│   │   │   │   └── PracticeViewModel.kt # StateFlow bridge
│   │   │   └── ui/
│   │   │       ├── theme/               # Material 3 colors, typography, theme
│   │   │       ├── components/
│   │   │       │   ├── StaffCanvas.kt   # 5-line musical staff renderer
│   │   │       │   ├── PianoKeyboard.kt # 2-octave reference keyboard
│   │   │       │   └── PracticeHud.kt   # Real-time target and mic HUD
│   │   │       └── screens/
│   │   │           ├── SongSelectionScreen.kt
│   │   │           └── PracticeScreen.kt
│   │   └── res/                         # Strings, icons, themes
│   └── src/test/
│       └── java/com/beginnerpiano/
│           ├── YinPitchDetectorTest.kt  # Synthetic sine wave pitch tests
│           ├── PracticeEngineTest.kt    # Progression & state machine tests
│           └── SongRepositoryTest.kt    # Song data integrity tests
├── build.gradle.kts                     # Root build configuration
├── settings.gradle.kts                  # Module settings & repos
├── gradle.properties                    # Memory & AndroidX flags
└── gradle/libs.versions.toml            # Version catalog
```

---

## How to Run in Android Studio

1. **Launch Android Studio**:
   Open **Android Studio** from your Applications folder (or run `open -a "Android Studio"` in Terminal).
2. **Open Project**:
   Click **Open** and select this directory: `/Users/thanghoang/github/beginner piano`.
3. **Gradle Sync**:
   Android Studio will automatically download required Android SDK platforms and sync dependencies.
4. **Create an Emulator**:
   * Open **Device Manager** in Android Studio (top-right toolbar).
   * Click **Create Device** (e.g. Pixel 8 with API 34 or 35).
5. **Run the App**:
   * Click the green **Run ▶** button.
   * Allow the microphone permission prompt on the device.
   * Play your piano or tap the on-screen keys to test!

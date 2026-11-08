# Execution Checklist: Pro Practice Suite

- [x] **Task 1: Rhythmic Notation & Sharp Accidental Classifier & Canvas Rendering (`REQ-005`)**
  - [x] Write unit tests in `NoteNotationHelperTest.kt` verifying `NoteHeadType` (`WHOLE`, `HALF`, `QUARTER`, `EIGHTH`), `isDotted`, and `isSharp` across MIDI notes and beat durations (`REQ-005`).
  - [x] Implement `NoteNotationHelper.kt` and update `StaffCanvas.kt` and `WrappedStaffCanvas.kt` to draw hollow whole/half notes, eighth-note flags, augmentation dots, and `♯` accidentals (`REQ-005`).
- [x] **Task 2: Tempo Speed Multiplier & A–B Section Looping in `PracticeEngine` (`REQ-006`, `REQ-007`)**
  - [x] Write unit tests in `PracticeEngineTest.kt` for `setTempoMultiplier`, `effectiveBpm`, `setLoopRange`, `onNoteTappedForLoop`, `clearLoop`, and A–B loop wrap-around on note hit (`REQ-006`, `REQ-007`).
  - [x] Implement `tempoMultiplier`, `effectiveBpm`, `loopStartIndex`, `loopEndIndex`, and loop wrap-around logic in `PracticeEngine.kt` and `PracticeViewModel.kt`, plus visual loop brackets and tap-to-loop gestures on `StaffCanvas.kt` and `WrappedStaffCanvas.kt` (`REQ-006`, `REQ-007`).
- [x] **Task 3: Active Tempo Mode Clock & Toggleable Metronome (`REQ-008`)**
  - [x] Write unit tests in `PracticeEngineTest.kt` and `PianoAudioSynthesizerTest.kt` for `onBeatTimeout()`, metronome toggle state, and `generateMetronomeClickSamples()` (`REQ-008`).
  - [x] Implement `onBeatTimeout()` in `PracticeEngine.kt`, `playMetronomeClick()` in `PianoAudioSynthesizer.kt`, and the beat-window coroutine loop in `PracticeViewModel.kt` (`REQ-008`).
- [x] **Task 4: Persistent 3-Star Song Mastery & Custom MIDI Import (`REQ-009`)**
  - [x] Write unit tests in `SongProgressRepositoryTest.kt` and `CustomSongRepositoryTest.kt` verifying star calculation (`95%` -> 3 stars, `80%` -> 2 stars, `60%` -> 1 star), best-score retention, and MIDI stream import persistence (`REQ-009`).
  - [x] Implement `SongProgressRepository.kt` and `CustomSongRepository.kt`, and wire 3-star badges, `+ Import MIDI` file picker, speed/loop controls, and completion stars into `SongSelectionScreen.kt`, `PracticeScreen.kt`, `SettingsDialog.kt`, and `MainActivity.kt` (`REQ-009`).
- [x] **Task 5: Full Regression Verification & Traceability Evidence (`REQ-005` .. `REQ-009`)**
  - [x] Execute `./gradlew testDebugUnitTest assembleDebug` and record verbatim test and build output in `docs/specs/2026-10-30-pro-practice-suite/verification.md` (`REQ-005`, `REQ-006`, `REQ-007`, `REQ-008`, `REQ-009`).

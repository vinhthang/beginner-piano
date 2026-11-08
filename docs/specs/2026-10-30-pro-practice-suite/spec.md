# Specification: Pro Practice Suite (Notation, A–B Looping, Tempo Clock, Mastery & MIDI Import)

## 1. Overview
Based on competitive research across leading piano learning applications (*Flowkey*, *Simply Piano*, *Skoove*, *Piano Marvel*, and *Synthesia*) and an architectural audit of the `Beginner Piano` codebase, this specification defines five cohesive practice and learning upgrades:

1. **True Musical Notation & Accidentals on Staff (`REQ-005`)**: Duration-accurate note heads (whole, half, quarter, eighth), stems, flags, augmentation dots, and sharp symbols (`♯`) on both `StaffCanvas` and `WrappedStaffCanvas`.
2. **Tempo Speed Multiplier (`REQ-006`)**: Adjustable playback and practice speed scaling (`0.5x`, `0.75x`, `1.0x`, `1.25x`) for slow-motion muscle-memory building.
3. **Interactive A–B Section Looping (`REQ-007`)**: Sub-phrase `[A..B]` loop selection via tap-on-staff or loop toolbar controls with continuous wrap-around in Wait-for-Note, Tempo, and Demo modes.
4. **Active Tempo Mode Beat Clock & Metronome (`REQ-008`)**: Real-time beat-driven note advancement in `PracticeMode.TEMPO` with hit/miss window grading and a toggleable synthesized metronome click.
5. **Persistent 3-Star Song Mastery & Custom MIDI Library (`REQ-009`)**: Persistent per-song star ratings (`1..3` stars), best accuracy %, and custom `.mid`/`.midi` file importing and storage.

---

## 2. Requirements & Acceptance Criteria

### `REQ-005`: True Musical Notation & Accidentals on Staff
- **Description**: Render standard rhythm notation and sharp accidentals (`♯`) for all `NoteEvent` items on both `StaffCanvas` (portrait scrolling view) and `WrappedStaffCanvas` (landscape multi-system score view).
- **Acceptance Criteria**:
  - `NoteNotationHelper` classifies `durationBeats` into visual rhythmic shapes:
    - `WHOLE` (`durationBeats >= 3.5`): Hollow oval note head, no stem.
    - `HALF` (`durationBeats in 1.75..<3.5`): Hollow oval note head with vertical stem.
    - `QUARTER` (`durationBeats in 0.85..<1.75`): Filled oval note head with vertical stem.
    - `EIGHTH` (`durationBeats < 0.85`): Filled oval note head with vertical stem and eighth-note flag.
  - Dotted durations (`durationBeats` within `0.1` of `1.5` or `3.0`) render an augmentation dot immediately to the right of the note head.
  - Notes with sharp MIDI pitch classes (`C#`, `D#`, `F#`, `G#`, `A#`, i.e., `midiNote % 12 in setOf(1, 3, 6, 8, 10)`) render a sharp glyph (`♯`) immediately to the left of the note head on both `StaffCanvas` and `WrappedStaffCanvas`.

### `REQ-006`: Tempo Speed Multiplier (`0.5x`, `0.75x`, `1.0x`, `1.25x`)
- **Description**: Allow users to scale the active song's BPM across `0.5x`, `0.75x`, `1.0x`, and `1.25x` for slow-motion practice and demo listening.
- **Acceptance Criteria**:
  - `PracticeState` tracks `tempoMultiplier: Float = 1.0f` and exposes `effectiveBpm: Int` (`(song.defaultBpm * tempoMultiplier).toInt().coerceAtLeast(20)`).
  - `PracticeEngine.setTempoMultiplier(multiplier: Float)` clamps or selects valid multipliers (`0.5f..1.5f`).
  - Both `Play Demo` and `PracticeMode.TEMPO` compute beat durations using `effectiveBpm` instead of raw `song.defaultBpm`.
  - Users can cycle or select the speed multiplier directly from `PracticeScreen` and `SettingsDialog`.

### `REQ-007`: Interactive A–B Section Looping
- **Description**: Enable isolating and repeating any sub-range of notes `[loopStartIndex..loopEndIndex]` within a song.
- **Acceptance Criteria**:
  - `PracticeState` tracks `loopStartIndex: Int? = null` and `loopEndIndex: Int? = null` with helper `isLoopActive: Boolean`.
  - `PracticeEngine.setLoopRange(startIndex: Int?, endIndex: Int?)` normalizes valid bounds within `song.notes.indices` (ensuring `start <= end`) and positions `currentNoteIndex` within `[start..end]` when activated.
  - When `isLoopActive` is true and the user hits (or tempo-advances past) `loopEndIndex`, `PracticeEngine` wraps `currentNoteIndex` back to `loopStartIndex` instead of completing the song.
  - Users can tap notes on `StaffCanvas` / `WrappedStaffCanvas` or use the A–B loop bar on `PracticeScreen` to set/clear loop points `A` and `B`.
  - Active loop boundaries `[A..B]` are visually highlighted on both `StaffCanvas` and `WrappedStaffCanvas`.

### `REQ-008`: Active Tempo Mode Beat Clock & Toggleable Metronome
- **Description**: Implement real-time beat-driven progression for `PracticeMode.TEMPO` alongside a toggleable metronome click.
- **Acceptance Criteria**:
  - In `PracticeMode.TEMPO`, when the user starts or resumes tempo practice, a coroutine beat clock runs at `effectiveBpm`.
  - If the user hits the target note before its beat duration expires, `PracticeEngine` records a `HIT` for that note and advances on beat (or immediately upon hit with beat synchronization).
  - If a note's beat window expires without a matching hit, `PracticeEngine.onBeatTimeout()` records a `MISMATCH` (`totalMisses + 1`, `currentStreak = 0`) and advances `currentNoteIndex` to the next note (respecting A–B loop wrap-around or completing the song at the end).
  - `PracticeState` includes `metronomeEnabled: Boolean = true`. When enabled during `PracticeMode.TEMPO`, `PianoAudioSynthesizer.playMetronomeClick(isDownbeat: Boolean)` emits a short percussive click on each beat without triggering false pitch hits.

### `REQ-009`: Persistent 3-Star Song Mastery & Custom MIDI Song Import
- **Description**: Persist song completion star ratings (`1..3` stars) and best accuracy scores across sessions, and allow importing custom `.mid`/`.midi` songs into the persistent song library.
- **Acceptance Criteria**:
  - Star calculation follows:
    - `accuracyPercent >= 95` -> `3` stars
    - `accuracyPercent in 80..94` -> `2` stars
    - `accuracyPercent in 60..79` -> `1` star
    - `accuracyPercent < 60` -> `0` stars
  - `SongProgressRepository` persists `SongMastery(songId, bestStars, bestAccuracy, bestStreak, playCount)` and retains the highest stars/accuracy/streak achieved per song.
  - `SongSelectionScreen` displays earned stars (`★`/`☆`) and best accuracy badge on each `SongCard`, plus an overall star counter in the header.
  - `SongSelectionScreen` provides an `+ Import MIDI` button using Android's `ActivityResultContracts.GetContent()`, parses the selected file via `MidiFileParser.parse()`, saves the imported song in `CustomSongRepository` (persisted on disk), and adds it immediately to the selectable song list.

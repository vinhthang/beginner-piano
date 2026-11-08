# Architecture Decision Records (ADR): Pro Practice Suite

## ADR-005: Pure Canvas Rhythmic Notation & Accidental Rendering
- **Context**: Both `StaffCanvas` and `WrappedStaffCanvas` previously drew every note as an identical filled oval with a stem and omitted sharp (`♯`) accidentals, making rhythmic values (`0.5` eighth, `1.0` quarter, `2.0` half, `4.0` whole, `1.5`/`3.0` dotted) and black-key notes visually indistinguishable on the staff.
- **Decision**: Introduce `NoteNotationHelper` to classify `NoteEvent` rhythm (`WHOLE`, `HALF`, `QUARTER`, `EIGHTH`, `isDotted`) and accidental status (`isSharp`), and update `StaffCanvas` and `WrappedStaffCanvas` to draw hollow vs. filled note heads, eighth-note flags, augmentation dots, and `♯` prefixes directly on the Compose `Canvas`.
- **Trade-offs & Risks**: Custom Canvas drawing requires careful spacing around the note head so the `♯` symbol on the left and augmentation dot on the right do not collide with adjacent notes or ledger lines. We mitigate this by offsetting the `♯` glyph `-18.dp` left and the dot `+13.dp` right of the note center.
- **Rejected Alternatives**:
  - *External MusicXML / SMuFL font rendering engine (e.g., Verovio WebView)*: Rejected because embedding a WebView or heavy native C++ engraving library bloats APK size, breaks instant Compose state reactivity, and complicates touch hit-testing on individual notes.
- **Traceability**: Addresses `REQ-005`.

---

## ADR-006: Unified Tempo Speed Multiplier (`0.5x` – `1.25x`) in `PracticeState`
- **Context**: Beginners practicing new pieces or tricky passages need slow-motion playback and tempo tracking without pitch distortion (as popularized by *Flowkey* and *Piano Marvel*).
- **Decision**: Store `tempoMultiplier: Float = 1.0f` (with presets `0.5f`, `0.75f`, `1.0f`, `1.25f`) inside `PracticeState` and compute `effectiveBpm = (song.defaultBpm * tempoMultiplier).toInt().coerceAtLeast(20)`. Both Demo playback and Tempo Mode beat scheduling read `effectiveBpm`.
- **Trade-offs & Risks**: Changing `tempoMultiplier` mid-playback must take effect on the very next note without restarting the song. Because the coroutine loop reads `_practiceState.value.effectiveBpm` dynamically on every note iteration, speed changes apply seamlessly on the next note boundary.
- **Rejected Alternatives**:
  - *Mutating `Song.defaultBpm` in place*: Rejected because `Song` is an immutable domain model and mutating it would lose the composer's original tempo reference.
- **Traceability**: Addresses `REQ-006`.

---

## ADR-007: Non-Destructive A–B Section Loop State Machine in `PracticeEngine`
- **Context**: Practicing an entire 40-note piece repeatedly to fix a 4-note measure wastes time. Users requested interactive A–B looping both via toolbar buttons and tapping notes on the staff.
- **Decision**: Add `loopStartIndex: Int?` and `loopEndIndex: Int?` to `PracticeState`. When `isLoopActive` (`loopStartIndex != null && loopEndIndex != null`), advancing past `loopEndIndex` in `PracticeEngine` (via pitch hit, key tap, tempo timeout, or demo playback) wraps `currentNoteIndex` back to `loopStartIndex` and keeps `isCompleted = false`. Tapping a note on `StaffCanvas` or `WrappedStaffCanvas` invokes `onNoteTapped(index)` to set `A`, then `B`, or clear the loop.
- **Trade-offs & Risks**: If a user activates an A–B loop, the song never reaches `isCompleted = true` while the loop remains active. This is intentional for continuous loop drilling; clearing the loop immediately restores normal completion at the end of the song.
- **Rejected Alternatives**:
  - *Slicing `Song.notes` into a temporary sub-list*: Rejected because slicing alters note indices, breaks measure barline alignment in `WrappedStaffCanvas`, and hides surrounding musical context.
- **Traceability**: Addresses `REQ-007`.

---

## ADR-008: Coroutine Beat-Window Clock &Timeout Grading for `PracticeMode.TEMPO`
- **Context**: `PracticeMode.TEMPO` previously existed in the enum and `SettingsDialog`, but had no runtime clock—behaving identically to `WAIT_FOR_NOTE`. The user chose an auto-advancing beat clock with a toggleable metronome click.
- **Decision**: Add `onBeatTimeout(): NoteFeedbackStatus` to `PracticeEngine` and a managed `tempoJob` coroutine in `PracticeViewModel`. When `mode == PracticeMode.TEMPO` and tempo session is running, each note is allotted `noteDurationMs = ((60_000.0 / effectiveBpm) * note.durationBeats).toLong()`. If the user hits the target note during the window, `noteHitInCurrentWindow` is marked and the playhead advances; if the window expires without a hit, `engine.onBeatTimeout()` records a `MISMATCH`, resets streak, and advances to the next note (or wraps to `loopStartIndex`). A short percussive click is synthesized via `PianoAudioSynthesizer.playMetronomeClick()` when `metronomeEnabled` is true.
- **Trade-offs & Risks**: Synthesized metronome clicks playing over the device speaker could be picked up by the microphone. We mitigate this by generating a brief (`25ms`) high-frequency dampened noise/click transient whose periodicity does not sustain a pitched fundamental long enough for YIN/SPICE (`80ms+` window) to lock onto a musical MIDI note.
- **Rejected Alternatives**:
  - *Pausing microphone capture during metronome clicks*: Rejected because stopping and restarting `AudioRecord` every beat (`~500ms`) introduces hardware audio glitches and drops user note attacks.
- **Traceability**: Addresses `REQ-008`.

---

## ADR-009: Lightweight JSON/SharedPreferences Persistence for 3-Star Mastery & Custom MIDI Library
- **Context**: Users need their 1–3 star mastery ratings, best accuracy %, and imported `.mid`/`.midi` songs persisted across app restarts without adding heavy annotation-processing database dependencies (like Room/KSP) that slow down builds.
- **Decision**: Implement `SongProgressRepository` (backed by Android `SharedPreferences` + in-memory cache with injectable storage interface for JVM unit tests) and `CustomSongRepository` (storing imported `Song` metadata and parsed notes as JSON in `context.filesDir` / `SharedPreferences`, with an in-memory fallback for unit tests).
- **Trade-offs & Risks**: `SharedPreferences` / JSON file serialization is ideal for dozens to hundreds of songs, though not designed for tens of thousands of relational rows. Given a repertoire of ~30–200 melodies, JSON persistence is instantaneous (`<2ms`) and keeps zero external library overhead.
- **Rejected Alternatives**:
  - *Adding Room SQLite + KSP plugins to Gradle*: Rejected because it requires modifying build plugins and adds significant compile-time overhead for a key-value mastery map and flat list of imported songs.
- **Traceability**: Addresses `REQ-009`.

# Specification: Hand Fingering Indicator & Wrapped Sheet Music

## 1. Overview
This specification details the design and implementation of two core educational piano capabilities:
1. **Hand Fingering with Colored Finger Annotation**: Displaying a stylized hand icon on notes with the designated finger (1 to 5) highlighted in color.
2. **Full Wrapped Sheet Music in Landscape ("Horizon") View**: Rendering sheet music wrapped across multiple systems/lines instead of a single scrolling ribbon, allowing players to view the full piece like a real score while practicing.

---

## 2. Requirements & Acceptance Criteria

### `REQ-001`: Finger Hint Data & Heuristic Support
- **Description**: Ensure every note event in practice songs has an unambiguous finger hint (1 = Thumb, 2 = Index, 3 = Middle, 4 = Ring, 5 = Pinky).
- **Acceptance Criteria**:
  - `NoteEvent.fingerHint` (1..5) is respected if explicitly provided.
  - A fallback beginner 5-finger heuristic (Right Hand C-position: C4=1, D4=2, E4=3, F4=4, G4=5) provides valid finger hints for songs lacking explicit annotations.
  - Invalid finger values (<1 or >5) default to null or valid clamped range.

### `REQ-002`: Hand Icon with Colored Finger Annotation on Notes
- **Description**: Render a clear, stylized hand diagram/badge on notes (especially the active note) with the corresponding finger highlighted in a distinct color.
- **Acceptance Criteria**:
  - The hand graphic depicts 5 fingers (Thumb through Pinky).
  - The target finger for the note is filled/highlighted with a designated finger color (e.g., Thumb=Blue, Index=Green, Middle=Yellow, Ring=Orange, Pinky=Purple, or dynamic highlight).
  - The hand indicator is rendered clearly above or next to the note without obscuring staff lines or ledger lines.
  - Non-active notes may display a subtle finger number badge, while the active playing note displays the highlighted colored-finger hand icon.

### `REQ-003`: Multi-System Wrapped Sheet Music Layout
- **Description**: Provide a wrapped musical staff renderer that divides the song into multiple systems (lines of 5-line treble staff) on a single screen view.
- **Acceptance Criteria**:
  - Notes wrap naturally across horizontal systems based on available canvas width and measure divisions.
  - Each system draws standard 5 staff lines, starting treble clef indicator, and barlines.
  - Middle C ledger lines and accidentals render accurately on whichever system the note resides.

### `REQ-004`: Landscape ("Horizon") View Integration
- **Description**: In landscape orientation (`PracticeScreen`), always render the full wrapped sheet music score.
- **Acceptance Criteria**:
  - Landscape mode automatically renders the wrapped sheet layout.
  - As notes are struck or advanced (Wait-for-Note mode or Demo playback), the active playhead indicator moves along the wrapped systems from line to line.
  - The reference keyboard remains accessible at the bottom of the landscape screen.

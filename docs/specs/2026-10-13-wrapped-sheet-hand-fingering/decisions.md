# Architecture Decision Records (ADR)

## ADR-001: Dedicated Finger Color Mapping
- **Context**: The user specified: *"No need rainbow color, a hand with color finger on the note"*. Rather than pitch-based colors (C=Red, D=Orange...), each of the 5 piano fingers (1..5) receives a dedicated educational color code.
- **Decision**:
  - Finger 1 (Thumb): `#3B82F6` (Blue)
  - Finger 2 (Index): `#10B981` (Emerald Green)
  - Finger 3 (Middle): `#F59E0B` (Amber / Yellow)
  - Finger 4 (Ring): `#F97316` (Orange)
  - Finger 5 (Pinky): `#EC4899` (Rose / Pink)
- **Traceability**: Addresses `REQ-002`.

---

## ADR-002: Canvas Vector Path for Hand Graphic
- **Context**: A hand diagram is required on notes. External PNG or XML vector drawables can add asset overhead and make per-finger dynamic color highlighting cumbersome.
- **Decision**: Draw a stylized, clean 5-finger hand icon directly using Jetpack Compose `DrawScope` path / rounded rectangles / circles. This permits dynamic tinting of the individual active finger with exact pixel scaling.
- **Traceability**: Addresses `REQ-002`.

---

## ADR-003: Wrapped Sheet Music Component (`WrappedStaffCanvas`)
- **Context**: Portrait mode has narrow width (~360-400dp) where a scrolling ribbon is useful, while landscape ("horizon view") has wide aspect ratio (~800-900dp+) ideal for viewing the entire wrapped score.
- **Decision**: Create a dedicated `WrappedStaffCanvas` component for landscape orientation that lays out measures across systems/rows. When notes exceed a row's width, it wraps to the next 5-line staff row below.
- **Traceability**: Addresses `REQ-003`, `REQ-004`.

---

## ADR-004: 5-Finger Beginner Heuristic Engine
- **Context**: `NoteEvent.fingerHint` is optional and null for most songs in `SongRepository`.
- **Decision**: Implement a fallback heuristic utility `FingeringEngine.resolveFingerHint(note: NoteEvent): Int` that determines the most natural beginner C-position right-hand finger (C4..G4 -> 1..5) when no manual hint is set, guaranteeing that the hand indicator always displays a valid finger.
- **Traceability**: Addresses `REQ-001`.

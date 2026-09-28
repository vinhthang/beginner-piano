package com.beginnerpiano.data.repository

import com.beginnerpiano.data.models.ClefType
import com.beginnerpiano.data.models.DifficultyLevel
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.data.models.Song

object SongRepository {

    val songs: List<Song> = listOf(
        // Level 1: Starter (5-Finger Middle C Position, C4..G4)
        createHotCrossBuns(),
        createAuClairDeLaLune(),
        createMaryHadALittleLamb(),
        createOdeToJoy(),
        createJingleBells(),
        createWhenTheSaints(),
        createMorningMood(),
        createDvorakLargo(),
        createCanCan(),
        createLightlyRow(),
        createAlouette(),
        createBeyerOp101No12(),

        // Level 2: Elementary (1-Octave Melodies & Scale Drills)
        createTwinkleTwinkle(),
        createFrereJacques(),
        createBrahmsLullaby(),
        createCMajorScale(),

        // Level 3: Intermediate (Classical Beginner Repertoire)
        createBeyerOp101No8(),
        createCzernyOp599No1(),
        createBachMinuetInG(),
        createFurElise()
    )

    fun getSongById(id: String): Song? = songs.find { it.id == id }

    fun getSongsByDifficulty(level: DifficultyLevel): List<Song> = songs.filter { it.difficulty == level }

    // --- LEVEL 1: STARTER (5-Finger Middle C, C4..G4) ---

    private fun createHotCrossBuns(): Song {
        // E4 D4 C4, E4 D4 C4, C4 C4 C4 C4, D4 D4 D4 D4, E4 D4 C4
        val noteMidis = listOf(
            64, 62, 60,
            64, 62, 60,
            60, 60, 60, 60,
            62, 62, 62, 62,
            64, 62, 60
        )
        return Song(
            id = "hot_cross_buns",
            title = "Hot Cross Buns",
            composer = "English Nursery Song",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 60,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createAuClairDeLaLune(): Song {
        // C4 C4 C4 D4 E4 D4 C4 E4 D4 D4 C4
        val noteMidis = listOf(
            60, 60, 60, 62,
            64, 62,
            60, 64, 62, 62,
            60
        )
        return Song(
            id = "au_clair_de_la_lune",
            title = "Au Clair de la Lune",
            composer = "French Folk Song",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 55,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createMaryHadALittleLamb(): Song {
        // E4 D4 C4 D4 E4 E4 E4, D4 D4 D4, E4 G4 G4, E4 D4 C4 D4 E4 E4 E4 E4 D4 D4 E4 D4 C4
        val noteMidis = listOf(
            64, 62, 60, 62, 64, 64, 64,
            62, 62, 62,
            64, 67, 67,
            64, 62, 60, 62, 64, 64, 64, 64,
            62, 62, 64, 62, 60
        )
        return Song(
            id = "mary_had_a_little_lamb",
            title = "Mary Had a Little Lamb",
            composer = "Traditional",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 60,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createOdeToJoy(): Song {
        // E4 E4 F4 G4 G4 F4 E4 D4 C4 C4 D4 E4 E4 D4 D4 C4
        val noteMidis = listOf(
            64, 64, 65, 67, 67, 65, 64, 62,
            60, 60, 62, 64, 64, 62, 62, 60
        )
        return Song(
            id = "ode_to_joy",
            title = "Ode to Joy",
            composer = "Ludwig van Beethoven",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 50,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createJingleBells(): Song {
        // E4 E4 E4, E4 E4 E4, E4 G4 C4 D4 E4, F4 F4 F4 F4 F4 E4 E4 E4 E4 D4 D4 E4 D4 G4
        val noteMidis = listOf(
            64, 64, 64,
            64, 64, 64,
            64, 67, 60, 62, 64,
            65, 65, 65, 65, 65, 64, 64, 64, 64,
            62, 62, 64, 62, 67
        )
        return Song(
            id = "jingle_bells",
            title = "Jingle Bells (Chorus)",
            composer = "James Lord Pierpont",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 65,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createWhenTheSaints(): Song {
        // C4 E4 F4 G4, C4 E4 F4 G4, C4 E4 F4 G4 E4 C4 E4 D4, E4 E4 D4 C4 C4 E4 G4 G4 F4, E4 F4 G4 E4 C4 D4 C4
        val noteMidis = listOf(
            60, 64, 65, 67,
            60, 64, 65, 67,
            60, 64, 65, 67, 64, 60, 64, 62,
            64, 64, 62, 60, 60, 64, 67, 67, 65,
            64, 65, 67, 64, 60, 62, 60
        )
        return Song(
            id = "when_the_saints",
            title = "When the Saints Go Marching In",
            composer = "Traditional Gospel",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 65,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createMorningMood(): Song {
        // G4 E4 D4 C4 D4 E4 G4 E4 D4 C4 D4 E4 D4, G4 E4 G4 E4 D4 C4 D4 E4 C4
        val noteMidis = listOf(
            67, 64, 62, 60, 62, 64,
            67, 64, 62, 60, 62, 64, 62,
            67, 64, 67, 64, 62, 60, 62, 64, 60
        )
        return Song(
            id = "morning_mood",
            title = "Morning Mood (Peer Gynt)",
            composer = "Edvard Grieg",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 50,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createDvorakLargo(): Song {
        // E4 G4 G4 E4 D4 C4, D4 E4 G4 E4 D4, E4 G4 G4 E4 D4 C4, D4 E4 D4 C4 C4
        val noteMidis = listOf(
            64, 67, 67, 64, 62, 60,
            62, 64, 67, 64, 62,
            64, 67, 67, 64, 62, 60,
            62, 64, 62, 60, 60
        )
        return Song(
            id = "dvorak_largo",
            title = "Largo (Going Home)",
            composer = "Antonín Dvořák",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 50,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createCanCan(): Song {
        // E4 E4 F4 G4 E4, D4 D4 D4 F4 D4, C4 C4 D4 E4 C4, D4 D4 D4 E4 D4, E4 E4 F4 G4 E4, D4 D4 D4 F4 D4, C4 C4 D4 E4 D4 C4
        val noteMidis = listOf(
            64, 64, 65, 67, 64,
            62, 62, 62, 65, 62,
            60, 60, 62, 64, 60,
            62, 62, 62, 64, 62,
            64, 64, 65, 67, 64,
            62, 62, 62, 65, 62,
            60, 60, 62, 64, 62, 60
        )
        return Song(
            id = "can_can",
            title = "Can-Can",
            composer = "Jacques Offenbach",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 70,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createLightlyRow(): Song {
        // G4 E4 E4, F4 D4 D4, C4 D4 E4 F4 G4 G4 G4, G4 E4 E4, F4 D4 D4, C4 E4 G4 G4 C4
        val noteMidis = listOf(
            67, 64, 64,
            65, 62, 62,
            60, 62, 64, 65, 67, 67, 67,
            67, 64, 64,
            65, 62, 62,
            60, 64, 67, 67, 60
        )
        return Song(
            id = "lightly_row",
            title = "Lightly Row (Hänschen klein)",
            composer = "German Traditional",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 60,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createAlouette(): Song {
        // C4 D4 E4 E4 D4 C4 D4 E4 C4, C4 D4 E4 E4 D4 C4 D4 C4, G4 G4 E4 G4 G4 E4 D4 E4 F4 E4 D4 C4
        val noteMidis = listOf(
            60, 62, 64, 64, 62, 60, 62, 64, 60,
            60, 62, 64, 64, 62, 60, 62, 60,
            67, 67, 64,
            67, 67, 64,
            62, 64, 65, 64, 62, 60
        )
        return Song(
            id = "alouette",
            title = "Alouette",
            composer = "French-Canadian Folk",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 65,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createBeyerOp101No12(): Song {
        // Ferdinand Beyer Op. 101 No. 12 (5-Finger Running Melody)
        val noteMidis = listOf(
            60, 62, 64, 65, 67, 65, 64, 62,
            60, 64, 67, 64,
            60, 62, 64, 62, 60,
            62, 64, 65, 67, 64, 60, 62, 60
        )
        return Song(
            id = "beyer_op101_12",
            title = "Beyer Op. 101: Exercise No. 12",
            composer = "Ferdinand Beyer",
            difficulty = DifficultyLevel.BEGINNER,
            defaultBpm = 60,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    // --- LEVEL 2: ELEMENTARY (1-Octave & Scale Drills) ---

    private fun createTwinkleTwinkle(): Song {
        // C4 C4 G4 G4 A4 A4 G4, F4 F4 E4 E4 D4 D4 C4
        val noteMidis = listOf(
            60, 60, 67, 67, 69, 69, 67,
            65, 65, 64, 64, 62, 62, 60,
            67, 67, 65, 65, 64, 64, 62,
            67, 67, 65, 65, 64, 64, 62,
            60, 60, 67, 67, 69, 69, 67,
            65, 65, 64, 64, 62, 62, 60
        )
        return Song(
            id = "twinkle_twinkle",
            title = "Twinkle, Twinkle, Little Star",
            composer = "Traditional English",
            difficulty = DifficultyLevel.ELEMENTARY,
            defaultBpm = 55,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createFrereJacques(): Song {
        // C4 D4 E4 C4, C4 D4 E4 C4, E4 F4 G4, E4 F4 G4, G4 A4 G4 F4 E4 C4
        val noteMidis = listOf(
            60, 62, 64, 60,
            60, 62, 64, 60,
            64, 65, 67,
            64, 65, 67,
            67, 69, 67, 65, 64, 60,
            67, 69, 67, 65, 64, 60,
            60, 55, 60,
            60, 55, 60
        )
        return Song(
            id = "frere_jacques",
            title = "Frère Jacques",
            composer = "French Traditional",
            difficulty = DifficultyLevel.ELEMENTARY,
            defaultBpm = 60,
            notes = noteMidis.map { midi ->
                NoteEvent(
                    midiNote = midi,
                    durationBeats = 1.0,
                    clef = if (midi < 60) ClefType.BASS else ClefType.TREBLE
                )
            }
        )
    }

    private fun createBrahmsLullaby(): Song {
        // E4 E4 G4, E4 E4 G4, E4 G4 C5 B4 A4 A4 G4, D4 E4 F4 D4
        val noteMidis = listOf(
            64, 64, 67,
            64, 64, 67,
            64, 67, 72, 71, 69, 69, 67,
            62, 64, 65, 62,
            62, 64, 65, 62,
            62, 65, 71, 69, 67, 71, 72
        )
        return Song(
            id = "brahms_lullaby",
            title = "Brahms' Lullaby",
            composer = "Johannes Brahms",
            difficulty = DifficultyLevel.ELEMENTARY,
            defaultBpm = 50,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createCMajorScale(): Song {
        // C4 D4 E4 F4 G4 A4 B4 C5 B4 A4 G4 F4 E4 D4 C4
        val noteMidis = listOf(
            60, 62, 64, 65, 67, 69, 71, 72,
            71, 69, 67, 65, 64, 62, 60
        )
        return Song(
            id = "c_major_scale",
            title = "C Major Scale Drill",
            composer = "Technique Drill",
            difficulty = DifficultyLevel.ELEMENTARY,
            defaultBpm = 50,
            notes = noteMidis.mapIndexed { idx, midi ->
                NoteEvent(
                    midiNote = midi,
                    durationBeats = 1.0,
                    clef = ClefType.TREBLE,
                    fingerHint = if (idx <= 7) (idx % 5) + 1 else null
                )
            }
        )
    }

    // --- LEVEL 3: INTERMEDIATE (Classical Repertoire & Method Etudes) ---

    private fun createBeyerOp101No8(): Song {
        // Ferdinand Beyer Op. 101 No. 8 (Melodic Etude in C)
        val noteMidis = listOf(
            60, 64, 67, 64,
            62, 65, 67, 65,
            64, 67, 72, 67,
            65, 62, 60
        )
        return Song(
            id = "beyer_op101_8",
            title = "Beyer Op. 101: Exercise No. 8",
            composer = "Ferdinand Beyer",
            difficulty = DifficultyLevel.INTERMEDIATE,
            defaultBpm = 60,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createCzernyOp599No1(): Song {
        // Carl Czerny Op. 599 No. 1 (5-Finger Etude)
        val noteMidis = listOf(
            60, 62, 64, 65,
            67, 65, 64, 62,
            60, 64, 67, 64,
            60, 67, 72
        )
        return Song(
            id = "czerny_op599_1",
            title = "Czerny Op. 599: Etude No. 1",
            composer = "Carl Czerny",
            difficulty = DifficultyLevel.INTERMEDIATE,
            defaultBpm = 65,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createBachMinuetInG(): Song {
        // Christian Petzold: Minuet in G Major (from Anna Magdalena Bach notebook)
        val noteMidis = listOf(
            74, 67, 69, 71, 72,
            74, 67, 67,
            76, 72, 74, 76, 78,
            79, 67, 67
        )
        return Song(
            id = "bach_minuet_g",
            title = "Minuet in G Major",
            composer = "Christian Petzold / J.S. Bach Notebook",
            difficulty = DifficultyLevel.INTERMEDIATE,
            defaultBpm = 60,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }

    private fun createFurElise(): Song {
        // Beethoven: Für Elise (Main Theme simplified single-note melody)
        val noteMidis = listOf(
            76, 75, 76, 75, 76, 71, 74, 72, 69,
            60, 64, 69, 71,
            64, 68, 71, 72,
            64, 76, 75, 76, 75, 76, 71, 74, 72, 69
        )
        return Song(
            id = "fur_elise",
            title = "Für Elise (Theme)",
            composer = "Ludwig van Beethoven",
            difficulty = DifficultyLevel.INTERMEDIATE,
            defaultBpm = 60,
            notes = noteMidis.map { midi ->
                NoteEvent(midiNote = midi, durationBeats = 1.0, clef = ClefType.TREBLE)
            }
        )
    }
}

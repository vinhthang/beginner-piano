package com.beginnerpiano

import com.beginnerpiano.data.repository.SongRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SongRepositoryTest {

    @Test
    fun testCuratedSongsExist() {
        val songs = SongRepository.songs
        assertTrue("Expected curated songs", songs.isNotEmpty())
        assertEquals("Expected exactly 20 beginner curriculum pieces", 20, songs.size)
    }

    @Test
    fun testCurriculumHasAllDifficultyLevels() {
        val beginner = SongRepository.getSongsByDifficulty(com.beginnerpiano.data.models.DifficultyLevel.BEGINNER)
        val elementary = SongRepository.getSongsByDifficulty(com.beginnerpiano.data.models.DifficultyLevel.ELEMENTARY)
        val intermediate = SongRepository.getSongsByDifficulty(com.beginnerpiano.data.models.DifficultyLevel.INTERMEDIATE)

        assertEquals("Expected exactly 12 Level 1 five-finger beginner songs", 12, beginner.size)
        assertEquals("Expected exactly 4 Level 2 elementary songs", 4, elementary.size)
        assertEquals("Expected exactly 4 Level 3 classical songs", 4, intermediate.size)
    }

    @Test
    fun testLevel1SongsAreConstrainedToFiveFingerPosition() {
        val beginnerSongs = SongRepository.getSongsByDifficulty(com.beginnerpiano.data.models.DifficultyLevel.BEGINNER)
        for (song in beginnerSongs) {
            for (note in song.notes) {
                assertTrue(
                    "Song ${song.title} has note ${note.midiNote} outside 5-finger C4..G4 range",
                    note.midiNote in 60..67
                )
            }
        }
    }

    @Test
    fun testClassicPiecesExist() {
        val expectedIds = listOf(
            // Level 1: 12 Five-Finger Pieces
            "hot_cross_buns", "au_clair_de_la_lune", "mary_had_a_little_lamb", "ode_to_joy", "jingle_bells",
            "when_the_saints", "morning_mood", "dvorak_largo", "can_can", "lightly_row", "alouette", "beyer_op101_12",
            // Level 2: Elementary
            "twinkle_twinkle", "frere_jacques", "brahms_lullaby", "c_major_scale",
            // Level 3: Classical
            "beyer_op101_8", "czerny_op599_1", "bach_minuet_g", "fur_elise"
        )
        for (id in expectedIds) {
            assertNotNull("Missing expected song: $id", SongRepository.getSongById(id))
        }
    }

    @Test
    fun testSongNotesAreValid() {
        for (song in SongRepository.songs) {
            assertTrue("Song ${song.title} has no notes", song.notes.isNotEmpty())
            for (note in song.notes) {
                assertTrue(
                    "Invalid MIDI note ${note.midiNote} in ${song.title}",
                    note.midiNote in 21..108
                )
                assertNotNull(note.noteName)
            }
        }
    }

    @Test
    fun testGetSongById() {
        val ode = SongRepository.getSongById("ode_to_joy")
        assertNotNull(ode)
        assertEquals("Ode to Joy", ode?.title)
    }
}

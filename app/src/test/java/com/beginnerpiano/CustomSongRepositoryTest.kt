package com.beginnerpiano

import com.beginnerpiano.data.models.DifficultyLevel
import com.beginnerpiano.data.repository.CustomSongRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class CustomSongRepositoryTest {

    private fun createMidiBytes(trackName: String = "Test Melody", notes: List<Int> = listOf(60, 62, 64, 65)): ByteArray {
        val trackData = ByteArrayOutputStream().apply {
            if (trackName.isNotBlank()) {
                write(0x00)
                write(0xFF)
                write(0x03)
                write(trackName.length)
                write(trackName.toByteArray(Charsets.US_ASCII))
            }

            // Delta 0, Meta 0x51 (Set Tempo: 500,000 microseconds = 120 BPM)
            write(0x00)
            write(0xFF)
            write(0x51)
            write(0x03)
            write(0x07); write(0xA1); write(0x20)

            for (midi in notes) {
                write(0x00)
                write(0x90); write(midi); write(64)
                write(0x83); write(0x60) // 480 ticks
                write(0x80); write(midi); write(0)
            }

            write(0x00)
            write(0xFF); write(0x2F); write(0x00)
        }.toByteArray()

        val fullMidi = ByteArrayOutputStream()
        val out = DataOutputStream(fullMidi)
        out.writeBytes("MThd")
        out.writeInt(6)
        out.writeShort(0)
        out.writeShort(1)
        out.writeShort(480)
        out.writeBytes("MTrk")
        out.writeInt(trackData.size)
        out.write(trackData)
        out.flush()

        return fullMidi.toByteArray()
    }

    @Test
    fun testImportValidMidi() {
        val repo = CustomSongRepository() // In-memory
        val midiBytes = createMidiBytes(trackName = "My Custom Song", notes = listOf(60, 62, 64))

        val imported = repo.importMidi(ByteArrayInputStream(midiBytes), fileName = "my_song.mid")

        assertEquals("My Custom Song", imported.title)
        assertEquals(3, imported.notes.size)
        assertEquals(DifficultyLevel.BEGINNER, imported.difficulty) // pitch range 64-60 = 4 <= 7

        val customSongs = repo.getCustomSongs()
        assertEquals(1, customSongs.size)
        assertEquals(imported.id, customSongs[0].id)

        val allSongs = repo.getAllSongs()
        assertTrue(allSongs.any { it.id == imported.id })
    }

    @Test
    fun testImportMidiTitleFallback() {
        val repo = CustomSongRepository()
        // No track name in MIDI stream -> fallback to fileName without extension
        val midiBytes = createMidiBytes(trackName = "", notes = listOf(60, 62, 64))

        val imported = repo.importMidi(ByteArrayInputStream(midiBytes), fileName = "bach_prelude.mid")
        assertEquals("bach_prelude", imported.title)

        // Without fileName either -> fallback to "Custom Song"
        val imported2 = repo.importMidi(ByteArrayInputStream(midiBytes), fileName = null)
        assertEquals("Custom Song", imported2.title)
    }

    @Test
    fun testDifficultyClassification() {
        val repo = CustomSongRepository()

        // 5-finger range (60..64, diff=4) -> BEGINNER
        val beginnerMidi = createMidiBytes("Beginner", listOf(60, 62, 64))
        val s1 = repo.importMidi(ByteArrayInputStream(beginnerMidi))
        assertEquals(DifficultyLevel.BEGINNER, s1.difficulty)

        // 1-octave range (60..71, diff=11) -> ELEMENTARY
        val elementaryMidi = createMidiBytes("Elementary", listOf(60, 65, 71))
        val s2 = repo.importMidi(ByteArrayInputStream(elementaryMidi))
        assertEquals(DifficultyLevel.ELEMENTARY, s2.difficulty)

        // > 1-octave range (48..72, diff=24) -> INTERMEDIATE
        val intermediateMidi = createMidiBytes("Intermediate", listOf(48, 60, 72))
        val s3 = repo.importMidi(ByteArrayInputStream(intermediateMidi))
        assertEquals(DifficultyLevel.INTERMEDIATE, s3.difficulty)
    }

    @Test
    fun testGetSongByIdAndDeletion() {
        val repo = CustomSongRepository()
        val midiBytes = createMidiBytes("Delete Me", listOf(60, 62))
        val imported = repo.importMidi(ByteArrayInputStream(midiBytes))

        assertNotNull(repo.getSongById(imported.id))
        assertTrue(repo.deleteCustomSong(imported.id))
        assertEquals(null, repo.getSongById(imported.id))
        assertEquals(0, repo.getCustomSongs().size)
    }
}

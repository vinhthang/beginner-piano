package com.beginnerpiano

import com.beginnerpiano.data.parser.MidiFileParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class MidiFileParserTest {

    private fun createSampleMidiBytes(): ByteArray {
        val trackData = ByteArrayOutputStream().apply {
            // Delta 0, Meta 0x03 (Track Name = "Simple Melody")
            write(0x00) // delta
            write(0xFF) // meta prefix
            write(0x03) // track name type
            write(13)   // length
            write("Simple Melody".toByteArray(Charsets.US_ASCII))

            // Delta 0, Meta 0x51 (Set Tempo: 500,000 microseconds = 120 BPM)
            write(0x00) // delta
            write(0xFF)
            write(0x51)
            write(0x03)
            write(0x07); write(0xA1); write(0x20) // 500,000 in hex

            // Note 1: C4 (MIDI 60)
            write(0x00) // delta
            write(0x90); write(60); write(64) // Note On
            write(0x83); write(0x60) // Variable length delta: 480 ticks (0x83 0x60)
            write(0x80); write(60); write(0)  // Note Off

            // Note 2: D4 (MIDI 62)
            write(0x00) // delta
            write(0x90); write(62); write(64) // Note On
            write(0x83); write(0x60) // 480 ticks
            write(0x80); write(62); write(0)

            // Note 3: E4 (MIDI 64)
            write(0x00)
            write(0x90); write(64); write(64)
            write(0x83); write(0x60)
            write(0x80); write(64); write(0)

            // Note 4: F4 (MIDI 65)
            write(0x00)
            write(0x90); write(65); write(64)
            write(0x83); write(0x60)
            write(0x80); write(65); write(0)

            // End of Track
            write(0x00)
            write(0xFF); write(0x2F); write(0x00)
        }.toByteArray()

        val fullMidi = ByteArrayOutputStream()
        val out = DataOutputStream(fullMidi)

        // Header: MThd, length 6, format 0, 1 track, 480 ticks/quarter
        out.writeBytes("MThd")
        out.writeInt(6)
        out.writeShort(0)   // Format 0
        out.writeShort(1)   // 1 Track
        out.writeShort(480) // 480 division

        // Track: MTrk, length, data
        out.writeBytes("MTrk")
        out.writeInt(trackData.size)
        out.write(trackData)
        out.flush()

        return fullMidi.toByteArray()
    }

    @Test
    fun testParseSimpleMidi() {
        val midiBytes = createSampleMidiBytes()
        val result = MidiFileParser.parse(ByteArrayInputStream(midiBytes))

        assertEquals("Simple Melody", result.title)
        assertEquals(120, result.bpm)
        assertEquals(4, result.notes.size)
        assertEquals(listOf(60, 62, 64, 65), result.notes.map { it.midiNote })
        assertEquals(1.0, result.notes[0].durationBeats, 0.05)
    }

    @Test
    fun testMonophonicChordReductionExtractsTopNote() {
        // Construct track with a C-Major triad (C4=60, E4=64, G4=67) at tick 0
        val trackData = ByteArrayOutputStream().apply {
            write(0x00) // delta 0
            write(0x90); write(60); write(64) // Note On C4
            write(0x00) // delta 0 (simultaneous)
            write(0x90); write(64); write(64) // Note On E4
            write(0x00) // delta 0 (simultaneous)
            write(0x90); write(67); write(64) // Note On G4 (highest note)

            write(0x83); write(0x60) // delta 480 ticks
            write(0x80); write(60); write(0)
            write(0x00)
            write(0x80); write(64); write(0)
            write(0x00)
            write(0x80); write(67); write(0)

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

        val result = MidiFileParser.parse(ByteArrayInputStream(fullMidi.toByteArray()))

        // Should isolate single top note (G4 = 67)
        assertEquals(1, result.notes.size)
        assertEquals(67, result.notes[0].midiNote)
    }

    @Test
    fun testFormat1MultiTrackExtraction() {
        // Track 0: Tempo and name
        val track0Data = ByteArrayOutputStream().apply {
            write(0x00); write(0xFF); write(0x03); write(10); write("MultiTrack".toByteArray())
            write(0x00); write(0xFF); write(0x51); write(3); write(0x09); write(0x27); write(0xC0) // 600,000 micros = 100 BPM
            write(0x00); write(0xFF); write(0x2F); write(0x00)
        }.toByteArray()

        // Track 1: Notes
        val track1Data = ByteArrayOutputStream().apply {
            write(0x00); write(0x90); write(72); write(64) // C5
            write(0x83); write(0x60)
            write(0x80); write(72); write(0)
            write(0x00); write(0xFF); write(0x2F); write(0x00)
        }.toByteArray()

        val fullMidi = ByteArrayOutputStream()
        val out = DataOutputStream(fullMidi)
        out.writeBytes("MThd")
        out.writeInt(6)
        out.writeShort(1) // Format 1
        out.writeShort(2) // 2 tracks
        out.writeShort(480)

        out.writeBytes("MTrk"); out.writeInt(track0Data.size); out.write(track0Data)
        out.writeBytes("MTrk"); out.writeInt(track1Data.size); out.write(track1Data)
        out.flush()

        val result = MidiFileParser.parse(ByteArrayInputStream(fullMidi.toByteArray()))

        assertEquals("MultiTrack", result.title)
        assertEquals(100, result.bpm)
        assertEquals(1, result.notes.size)
        assertEquals(72, result.notes[0].midiNote) // C5
    }
}

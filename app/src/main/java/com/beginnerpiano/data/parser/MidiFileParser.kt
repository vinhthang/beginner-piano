package com.beginnerpiano.data.parser

import com.beginnerpiano.data.models.ClefType
import com.beginnerpiano.data.models.NoteEvent
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.InputStream

data class ParsedMidi(
    val title: String,
    val bpm: Int,
    val notes: List<NoteEvent>
)

object MidiFileParser {

    private data class RawNote(
        val midiNote: Int,
        val startTick: Long,
        var endTick: Long = 0L
    )

    fun parse(inputStream: InputStream): ParsedMidi {
        val bytes = inputStream.readBytes()
        val dataIn = DataInputStream(ByteArrayInputStream(bytes))

        // 1. Parse Header Chunk (MThd)
        val headerId = readString(dataIn, 4)
        if (headerId != "MThd") {
            throw IllegalArgumentException("Invalid MIDI header: expected MThd, got $headerId")
        }
        val headerLength = dataIn.readInt()
        val format = dataIn.readShort().toInt()
        val numTracks = dataIn.readShort().toInt()
        val division = dataIn.readShort().toInt()
        val ticksPerQuarter = if (division > 0) division else 480

        // Skip any extra header bytes if length > 6
        if (headerLength > 6) {
            dataIn.skipBytes(headerLength - 6)
        }

        var detectedTitle = ""
        var detectedBpm = 120
        val allRawNotes = mutableListOf<RawNote>()

        // 2. Parse Tracks
        for (t in 0 until numTracks) {
            if (dataIn.available() < 8) break
            val trackId = readString(dataIn, 4)
            val trackLength = dataIn.readInt()
            val trackBytes = ByteArray(trackLength)
            dataIn.readFully(trackBytes)

            if (trackId == "MTrk") {
                val trackStream = DataInputStream(ByteArrayInputStream(trackBytes))
                var currentTick = 0L
                var runningStatus = 0
                val activeNotes = mutableMapOf<Int, RawNote>()

                while (trackStream.available() > 0) {
                    val delta = readVariableLength(trackStream)
                    currentTick += delta

                    val nextByte = trackStream.readUnsignedByte()
                    val status: Int
                    val firstDataByte: Int

                    if (nextByte and 0x80 != 0) {
                        status = nextByte
                        runningStatus = status
                        firstDataByte = if (status < 0xF0) trackStream.readUnsignedByte() else 0
                    } else {
                        status = runningStatus
                        firstDataByte = nextByte
                    }

                    val messageType = status and 0xF0

                    when (messageType) {
                        0x90 -> { // Note On
                            val note = firstDataByte
                            val velocity = trackStream.readUnsignedByte()
                            if (velocity > 0) {
                                val raw = RawNote(midiNote = note, startTick = currentTick)
                                activeNotes[note] = raw
                                allRawNotes.add(raw)
                            } else {
                                // Velocity 0 is Note Off
                                activeNotes.remove(note)?.endTick = currentTick
                            }
                        }
                        0x80 -> { // Note Off
                            val note = firstDataByte
                            trackStream.readUnsignedByte() // velocity
                            activeNotes.remove(note)?.endTick = currentTick
                        }
                        0xA0, 0xB0, 0xE0 -> { // Polyphonic aftertouch, Control Change, Pitch Bend (2 data bytes)
                            trackStream.readUnsignedByte()
                        }
                        0xC0, 0xD0 -> { // Program change, Channel pressure (1 data byte already read)
                            // No extra byte needed
                        }
                        0xF0 -> { // Meta or System Exclusive
                            if (status == 0xFF) {
                                val metaType = trackStream.readUnsignedByte()
                                val metaLength = readVariableLength(trackStream).toInt()
                                val metaData = ByteArray(metaLength)
                                trackStream.readFully(metaData)

                                when (metaType) {
                                    0x03 -> { // Track Name
                                        if (detectedTitle.isEmpty()) {
                                            detectedTitle = String(metaData, Charsets.US_ASCII).trim()
                                        }
                                    }
                                    0x51 -> { // Set Tempo (3 bytes microseconds per quarter)
                                        if (metaLength == 3) {
                                            val tempoMicros = ((metaData[0].toInt() and 0xFF) shl 16) or
                                                    ((metaData[1].toInt() and 0xFF) shl 8) or
                                                    (metaData[2].toInt() and 0xFF)
                                            if (tempoMicros > 0) {
                                                detectedBpm = (60_000_000L / tempoMicros).toInt()
                                            }
                                        }
                                    }
                                    0x2F -> { // End of Track
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Close any unfinished notes
        allRawNotes.forEach {
            if (it.endTick <= it.startTick) {
                it.endTick = it.startTick + ticksPerQuarter
            }
        }

        // 3. Monophonic reduction: Sort by tick and group simultaneous notes (take highest pitch)
        val groupedByTick = allRawNotes.groupBy { it.startTick }.toSortedMap()
        val monophonicNotes = mutableListOf<NoteEvent>()

        for ((_, notesAtTick) in groupedByTick) {
            // Extract highest pitch melody note
            val topNote = notesAtTick.maxByOrNull { it.midiNote } ?: continue
            val durationBeats = maxOf(0.25, (topNote.endTick - topNote.startTick).toDouble() / ticksPerQuarter)
            val clef = if (topNote.midiNote >= 60) ClefType.TREBLE else ClefType.BASS

            monophonicNotes.add(
                NoteEvent(
                    midiNote = topNote.midiNote,
                    durationBeats = durationBeats,
                    clef = clef
                )
            )
        }

        return ParsedMidi(
            title = if (detectedTitle.isNotEmpty()) detectedTitle else "Imported Song",
            bpm = detectedBpm,
            notes = monophonicNotes
        )
    }

    private fun readString(dis: DataInputStream, length: Int): String {
        val bytes = ByteArray(length)
        dis.readFully(bytes)
        return String(bytes, Charsets.US_ASCII)
    }

    private fun readVariableLength(dis: DataInputStream): Long {
        var value = 0L
        do {
            val b = dis.readUnsignedByte()
            value = (value shl 7) or (b and 0x7F).toLong()
        } while (b and 0x80 != 0)
        return value
    }
}

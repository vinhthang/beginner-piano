package com.beginnerpiano.data.repository

import android.content.Context
import com.beginnerpiano.data.models.ClefType
import com.beginnerpiano.data.models.DifficultyLevel
import com.beginnerpiano.data.models.NoteEvent
import com.beginnerpiano.data.models.Song
import com.beginnerpiano.data.parser.MidiFileParser
import java.io.InputStream
import java.util.UUID

/**
 * Repository managing imported custom MIDI songs alongside standard catalog songs.
 * Operates purely in-memory when [context] is null (e.g. JVM unit tests).
 */
class CustomSongRepository(
    private val context: Context? = null
) {
    private val lock = Any()
    private val customSongs = mutableListOf<Song>()

    init {
        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.all.forEach { (key, value) ->
                    if (key.startsWith(PREFIX_SONG) && value is String) {
                        deserializeSong(value)?.let { song ->
                            customSongs.add(song)
                        }
                    }
                }
            } catch (e: Throwable) {
                // Graceful fallback
            }
        }
    }

    /**
     * Imports a MIDI file from [inputStream] and persists the parsed song.
     */
    fun importMidi(inputStream: InputStream, fileName: String? = null): Song {
        val parsed = MidiFileParser.parse(inputStream)

        val resolvedTitle = when {
            parsed.title.isNotBlank() && parsed.title != "Untitled" && parsed.title != "Imported Song" -> parsed.title
            fileName != null && fileName.isNotBlank() -> fileName.substringBeforeLast(".")
            else -> "Custom Song"
        }

        val difficulty = classifyDifficulty(parsed.notes)
        val song = Song(
            id = "custom_" + UUID.randomUUID().toString(),
            title = resolvedTitle,
            composer = "Imported MIDI",
            difficulty = difficulty,
            defaultBpm = parsed.bpm,
            notes = parsed.notes
        )

        synchronized(lock) {
            customSongs.add(song)
        }

        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putString("$PREFIX_SONG${song.id}", serializeSong(song)).apply()
            } catch (e: Throwable) {
                // Graceful fallback
            }
        }

        return song
    }

    /**
     * Returns all imported custom songs.
     */
    fun getCustomSongs(): List<Song> {
        synchronized(lock) {
            return customSongs.toList()
        }
    }

    /**
     * Returns union of standard built-in songs and imported custom songs.
     */
    fun getAllSongs(): List<Song> {
        synchronized(lock) {
            return SongRepository.songs + customSongs
        }
    }

    /**
     * Finds song by ID across custom songs and standard catalog songs.
     */
    fun getSongById(id: String): Song? {
        synchronized(lock) {
            return customSongs.find { it.id == id } ?: SongRepository.getSongById(id)
        }
    }

    /**
     * Deletes custom song with [id] if present.
     */
    fun deleteCustomSong(id: String): Boolean {
        val removed: Boolean
        synchronized(lock) {
            removed = customSongs.removeAll { it.id == id }
        }

        if (removed) {
            context?.let { ctx ->
                try {
                    val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().remove("$PREFIX_SONG$id").apply()
                } catch (e: Throwable) {
                    // Graceful fallback
                }
            }
        }
        return removed
    }

    companion object {
        private const val PREFS_NAME = "custom_songs_repo"
        private const val PREFIX_SONG = "song_"

        fun classifyDifficulty(notes: List<NoteEvent>): DifficultyLevel {
            if (notes.isEmpty()) return DifficultyLevel.BEGINNER
            val minMidi = notes.minOf { it.midiNote }
            val maxMidi = notes.maxOf { it.midiNote }
            val pitchRange = maxMidi - minMidi
            return when {
                pitchRange <= 7 -> DifficultyLevel.BEGINNER
                pitchRange <= 14 -> DifficultyLevel.ELEMENTARY
                else -> DifficultyLevel.INTERMEDIATE
            }
        }

        private fun serializeSong(song: Song): String {
            val notesString = song.notes.joinToString(";") { note ->
                "${note.midiNote}:${note.durationBeats}:${note.clef.name}:${note.fingerHint ?: ""}:${note.description.replace("|", "_")}"
            }
            return "${song.id}|${song.title.replace("|", "_")}|${song.composer.replace("|", "_")}|${song.difficulty.name}|${song.defaultBpm}|$notesString"
        }

        private fun deserializeSong(str: String): Song? {
            val parts = str.split("|")
            if (parts.size < 6) return null
            val id = parts[0]
            val title = parts[1]
            val composer = parts[2]
            val difficulty = try {
                DifficultyLevel.valueOf(parts[3])
            } catch (e: Throwable) {
                DifficultyLevel.BEGINNER
            }
            val bpm = parts[4].toIntOrNull() ?: 60
            val notesRaw = parts[5]

            val notes = if (notesRaw.isBlank()) emptyList() else {
                notesRaw.split(";").mapNotNull { noteStr ->
                    val noteParts = noteStr.split(":")
                    if (noteParts.size >= 3) {
                        val midi = noteParts[0].toIntOrNull() ?: return@mapNotNull null
                        val duration = noteParts[1].toDoubleOrNull() ?: 1.0
                        val clef = try {
                            ClefType.valueOf(noteParts[2])
                        } catch (e: Throwable) {
                            ClefType.TREBLE
                        }
                        val finger = if (noteParts.size > 3) noteParts[3].toIntOrNull() else null
                        val desc = if (noteParts.size > 4) noteParts[4] else ""
                        NoteEvent(
                            midiNote = midi,
                            durationBeats = duration,
                            clef = clef,
                            fingerHint = finger,
                            description = desc
                        )
                    } else null
                }
            }

            return Song(
                id = id,
                title = title,
                composer = composer,
                difficulty = difficulty,
                defaultBpm = bpm,
                notes = notes
            )
        }
    }
}

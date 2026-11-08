package com.beginnerpiano.data.repository

import android.content.Context
import com.beginnerpiano.data.models.SongMastery
import com.beginnerpiano.practice.PracticeState

/**
 * Repository persisting per-song completion metrics and 3-star ratings.
 * Operates purely in-memory when [context] is null (e.g. JVM unit tests).
 */
class SongProgressRepository(
    private val context: Context? = null
) {
    private val lock = Any()
    private val masteryMap = mutableMapOf<String, SongMastery>()

    init {
        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.all.forEach { (key, value) ->
                    if (key.startsWith(PREFIX_MASTERY) && value is String) {
                        deserialize(value)?.let { mastery ->
                            masteryMap[mastery.songId] = mastery
                        }
                    }
                }
            } catch (e: Throwable) {
                // Graceful fallback for test or restricted environments
            }
        }
    }

    /**
     * Records completion of a practice session for [songId].
     * Highest historical stars, accuracy, and streak are retained; playCount increments unconditionally.
     */
    fun recordCompletion(songId: String, accuracyPercent: Int, streak: Int): SongMastery {
        val earnedStars = PracticeState.calculateStars(accuracyPercent)

        val updated: SongMastery
        synchronized(lock) {
            val existing = masteryMap[songId] ?: SongMastery(songId = songId)
            updated = existing.copy(
                bestStars = maxOf(existing.bestStars, earnedStars),
                bestAccuracy = maxOf(existing.bestAccuracy, accuracyPercent),
                bestStreak = maxOf(existing.bestStreak, streak),
                playCount = existing.playCount + 1
            )
            masteryMap[songId] = updated
        }

        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putString("$PREFIX_MASTERY$songId", serialize(updated)).apply()
            } catch (e: Throwable) {
                // Graceful fallback
            }
        }

        return updated
    }

    /**
     * Returns mastery for [songId], or default 0-stats mastery if not yet played.
     */
    fun getMastery(songId: String): SongMastery {
        synchronized(lock) {
            return masteryMap[songId] ?: SongMastery(songId = songId)
        }
    }

    /**
     * Returns a snapshot of all recorded mastery stats keyed by songId.
     */
    fun getAllMastery(): Map<String, SongMastery> {
        synchronized(lock) {
            return masteryMap.toMap()
        }
    }

    /**
     * Returns total stars earned across all recorded songs.
     */
    fun getTotalStars(): Int {
        synchronized(lock) {
            return masteryMap.values.sumOf { it.bestStars }
        }
    }

    companion object {
        private const val PREFS_NAME = "song_progress_repo"
        private const val PREFIX_MASTERY = "mastery_"

        private fun serialize(mastery: SongMastery): String {
            return "${mastery.songId}|${mastery.bestStars}|${mastery.bestAccuracy}|${mastery.bestStreak}|${mastery.playCount}"
        }

        private fun deserialize(str: String): SongMastery? {
            val parts = str.split("|")
            if (parts.size != 5) return null
            return SongMastery(
                songId = parts[0],
                bestStars = parts[1].toIntOrNull() ?: 0,
                bestAccuracy = parts[2].toIntOrNull() ?: 0,
                bestStreak = parts[3].toIntOrNull() ?: 0,
                playCount = parts[4].toIntOrNull() ?: 0
            )
        }
    }
}

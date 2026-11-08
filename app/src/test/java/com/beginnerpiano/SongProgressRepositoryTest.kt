package com.beginnerpiano

import com.beginnerpiano.data.repository.SongProgressRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class SongProgressRepositoryTest {

    @Test
    fun testRecordCompletionInitial() {
        val repo = SongProgressRepository() // In-memory without Context
        val mastery = repo.recordCompletion(
            songId = "hot_cross_buns",
            accuracyPercent = 96,
            streak = 15
        )

        assertEquals("hot_cross_buns", mastery.songId)
        assertEquals(3, mastery.bestStars) // >= 95% -> 3 stars
        assertEquals(96, mastery.bestAccuracy)
        assertEquals(15, mastery.bestStreak)
        assertEquals(1, mastery.playCount)

        val retrieved = repo.getMastery("hot_cross_buns")
        assertEquals(3, retrieved.bestStars)
        assertEquals(96, retrieved.bestAccuracy)
        assertEquals(15, retrieved.bestStreak)
        assertEquals(1, retrieved.playCount)
    }

    @Test
    fun testLowerScoreDoesNotOverwriteHigherBestStats() {
        val repo = SongProgressRepository()
        // First run: high accuracy 95% (3 stars), streak 12
        repo.recordCompletion("song_1", accuracyPercent = 95, streak = 12)

        // Second run: lower accuracy 70% (1 star), lower streak 5
        val updated = repo.recordCompletion("song_1", accuracyPercent = 70, streak = 5)

        // Stars, bestAccuracy, bestStreak remain from the first run; playCount increments to 2
        assertEquals(3, updated.bestStars)
        assertEquals(95, updated.bestAccuracy)
        assertEquals(12, updated.bestStreak)
        assertEquals(2, updated.playCount)
    }

    @Test
    fun testHigherScoreUpdatesStats() {
        val repo = SongProgressRepository()
        // First run: 65% (1 star)
        repo.recordCompletion("song_2", accuracyPercent = 65, streak = 4)

        // Second run: 88% (2 stars), higher streak
        val updated = repo.recordCompletion("song_2", accuracyPercent = 88, streak = 10)

        assertEquals(2, updated.bestStars)
        assertEquals(88, updated.bestAccuracy)
        assertEquals(10, updated.bestStreak)
        assertEquals(2, updated.playCount)
    }

    @Test
    fun testTotalStarsAggregation() {
        val repo = SongProgressRepository()
        repo.recordCompletion("song_a", accuracyPercent = 96, streak = 10) // 3 stars
        repo.recordCompletion("song_b", accuracyPercent = 85, streak = 8)  // 2 stars
        repo.recordCompletion("song_c", accuracyPercent = 65, streak = 5)  // 1 star
        repo.recordCompletion("song_d", accuracyPercent = 40, streak = 2)  // 0 stars

        assertEquals(6, repo.getTotalStars()) // 3 + 2 + 1 + 0 = 6
    }
}

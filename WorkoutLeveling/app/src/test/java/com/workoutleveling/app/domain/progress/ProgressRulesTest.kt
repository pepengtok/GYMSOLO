package com.workoutleveling.app.domain.progress

import com.workoutleveling.app.data.local.db.WorkoutSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressRulesTest {
    @Test
    fun sessionXpGain_includesRecoveryBonus() {
        val xp = ProgressRules.sessionXpGain(
            durationMinutes = 30,
            streakAfterThisSession = 3,
            includedRecoveryMobility = true,
        )
        assertEquals(100, xp)
    }

    @Test
    fun rankFromRollingWindow_promotesWithBalancedMix() {
        val now = 1_000_000_000_000L
        val day = 24L * 60 * 60 * 1000
        val sessions = listOf(
            WorkoutSessionEntity(startedAtEpochMs = now - 2 * day, type = "A", includedRecoveryMobility = true),
            WorkoutSessionEntity(startedAtEpochMs = now - 4 * day, type = "B", includedRecoveryMobility = true),
            WorkoutSessionEntity(startedAtEpochMs = now - 6 * day, type = "A", includedRecoveryMobility = true),
            WorkoutSessionEntity(startedAtEpochMs = now - 8 * day, type = "B", includedRecoveryMobility = false),
            WorkoutSessionEntity(startedAtEpochMs = now - 10 * day, type = "Cardio"),
            WorkoutSessionEntity(startedAtEpochMs = now - 12 * day, type = "Cardio"),
            WorkoutSessionEntity(startedAtEpochMs = now - 14 * day, type = "A", includedRecoveryMobility = false),
            WorkoutSessionEntity(startedAtEpochMs = now - 16 * day, type = "B", includedRecoveryMobility = false),
            WorkoutSessionEntity(startedAtEpochMs = now - 18 * day, type = "Cardio"),
            WorkoutSessionEntity(startedAtEpochMs = now - 20 * day, type = "A", includedRecoveryMobility = true),
            WorkoutSessionEntity(startedAtEpochMs = now - 22 * day, type = "B", includedRecoveryMobility = true),
            WorkoutSessionEntity(startedAtEpochMs = now - 24 * day, type = "A", includedRecoveryMobility = false),
        )
        val rank = ProgressRules.rankFromRollingWindow(now, sessions)
        assertEquals("A", rank)
    }

    @Test
    fun rankFromRollingWindow_ignoresTooOldSessions() {
        val now = 1_000_000_000_000L
        val day = 24L * 60 * 60 * 1000
        val old = WorkoutSessionEntity(startedAtEpochMs = now - 35 * day, type = "A", includedRecoveryMobility = true)
        val recent = WorkoutSessionEntity(startedAtEpochMs = now - 2 * day, type = "A", includedRecoveryMobility = true)
        val rank = ProgressRules.rankFromRollingWindow(now, listOf(old, recent))
        assertTrue(rank == "E" || rank == "D")
    }
}

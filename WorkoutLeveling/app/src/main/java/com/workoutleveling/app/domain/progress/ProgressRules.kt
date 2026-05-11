package com.workoutleveling.app.domain.progress

import com.workoutleveling.app.data.local.db.WorkoutSessionEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object ProgressRules {
    const val XP_PER_LEVEL = 300
    const val BASE_SESSION_XP = 50
    const val MAX_DURATION_XP_MINUTES = 45
    const val STREAK_BONUS_XP = 10
    const val STREAK_BONUS_MIN_DAYS = 3
    const val RECOVERY_MOBILITY_BONUS_XP = 10

    private const val RANK_WINDOW_MS = 28L * 24 * 60 * 60 * 1000

    fun levelFromXp(xp: Int): Int = (xp / XP_PER_LEVEL) + 1

    fun xpIntoCurrentLevel(xp: Int): Int = xp % XP_PER_LEVEL

    /** XP yang masih dibutuhkan untuk naik level berikutnya (1..300). */
    fun xpRemainingToNextLevel(xp: Int): Int {
        val into = xpIntoCurrentLevel(xp)
        return if (into == 0 && xp > 0) XP_PER_LEVEL else XP_PER_LEVEL - into
    }

    /**
     * Rank dari aktivitas **rolling 28 hari** (≈4 minggu): frekuensi sesi, mix strength/cardio,
     * dan proporsi sesi strength yang menyertakan mobility/cooldown.
     */
    fun rankFromRollingWindow(nowEpochMs: Long, sessions: List<WorkoutSessionEntity>): String {
        val start = nowEpochMs - RANK_WINDOW_MS
        val inWindow = sessions.filter {
            it.startedAtEpochMs in start..nowEpochMs
        }
        if (inWindow.size <= 1) return "E"
        val perWeek = inWindow.size / 4f
        val strength = inWindow.count { it.type == "A" || it.type == "B" }
        val cardio = inWindow.count { it.type == "Cardio" }
        val strengthWithRecovery = inWindow.count {
            (it.type == "A" || it.type == "B") && it.includedRecoveryMobility
        }
        val recoveryRate = if (strength == 0) 0f else strengthWithRecovery.toFloat() / strength
        return when {
            perWeek >= 3f && strength >= 3 && cardio >= 2 && recoveryRate >= 0.4f -> "A"
            perWeek >= 2.5f && strength >= 2 && cardio >= 1 -> "B"
            perWeek >= 2f && strength >= 1 -> "C"
            else -> "D"
        }
    }

    fun nextStreak(
        lastSessionDate: LocalDate?,
        currentStreak: Int,
        today: LocalDate,
    ): Int {
        if (lastSessionDate == null) return 1
        if (lastSessionDate == today) return currentStreak
        val daysBetween = ChronoUnit.DAYS.between(lastSessionDate, today)
        return if (daysBetween == 1L) currentStreak + 1 else 1
    }

    fun sessionXpGain(
        durationMinutes: Int,
        streakAfterThisSession: Int,
        includedRecoveryMobility: Boolean,
    ): Int {
        val dur = durationMinutes.coerceIn(0, MAX_DURATION_XP_MINUTES)
        val streakBonus = if (streakAfterThisSession >= STREAK_BONUS_MIN_DAYS) STREAK_BONUS_XP else 0
        val recoveryBonus = if (includedRecoveryMobility) RECOVERY_MOBILITY_BONUS_XP else 0
        return BASE_SESSION_XP + dur + streakBonus + recoveryBonus
    }
}

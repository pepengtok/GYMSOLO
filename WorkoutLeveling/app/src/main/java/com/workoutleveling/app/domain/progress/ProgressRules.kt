package com.workoutleveling.app.domain.progress

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object ProgressRules {
    const val XP_PER_LEVEL = 300
    const val BASE_SESSION_XP = 50
    const val MAX_DURATION_XP_MINUTES = 45
    const val STREAK_BONUS_XP = 10
    const val STREAK_BONUS_MIN_DAYS = 3

    fun levelFromXp(xp: Int): Int = (xp / XP_PER_LEVEL) + 1

    fun xpIntoCurrentLevel(xp: Int): Int = xp % XP_PER_LEVEL

    /** XP yang masih dibutuhkan untuk naik level berikutnya (1..300). */
    fun xpRemainingToNextLevel(xp: Int): Int {
        val into = xpIntoCurrentLevel(xp)
        return if (into == 0 && xp > 0) XP_PER_LEVEL else XP_PER_LEVEL - into
    }

    fun rankFromLevel(level: Int): String = when {
        level >= 15 -> "A"
        level >= 10 -> "B"
        level >= 6 -> "C"
        level >= 3 -> "D"
        else -> "E"
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
    ): Int {
        val dur = durationMinutes.coerceIn(0, MAX_DURATION_XP_MINUTES)
        val streakBonus = if (streakAfterThisSession >= STREAK_BONUS_MIN_DAYS) STREAK_BONUS_XP else 0
        return BASE_SESSION_XP + dur + streakBonus
    }
}

package com.workoutleveling.app.domain.progress

import kotlin.math.roundToInt

object CalorieEstimator {
    data class Profile(
        val weightKg: Float,
        val heightCm: Int,
        val ageYears: Int,
        val sex: String,
    )

    fun estimateSessionCaloriesKcal(
        sessionType: String,
        durationMinutes: Int,
        setCount: Int,
        includesMobility: Boolean,
        profile: Profile,
    ): Int {
        val safeMin = durationMinutes.coerceAtLeast(1)
        val baseMet = when (sessionType) {
            "Cardio" -> 7.0f
            "A", "B" -> 5.2f
            else -> 4.5f
        }
        val ageAdjust = if (profile.ageYears >= 50) 0.96f else 1f
        val sexAdjust = when (profile.sex.lowercase()) {
            "male" -> 1.03f
            "female" -> 0.97f
            else -> 1f
        }
        val setsFactor = if (sessionType == "Cardio") 1f else (1f + (setCount.coerceAtMost(20) * 0.01f))
        val mobilityBonus = if (includesMobility) 1.05f else 1f
        val met = baseMet * ageAdjust * sexAdjust
        val kcal = met * 3.5f * profile.weightKg / 200f * safeMin * setsFactor * mobilityBonus
        return kcal.roundToInt().coerceAtLeast(0)
    }
}

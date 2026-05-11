package com.workoutleveling.app.domain.progress

import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieEstimatorTest {
    @Test
    fun cardioBurnsMoreThanStrength_sameDuration() {
        val profile = CalorieEstimator.Profile(weightKg = 75f, heightCm = 170, ageYears = 30, sex = "male")
        val cardio = CalorieEstimator.estimateSessionCaloriesKcal(
            sessionType = "Cardio",
            durationMinutes = 30,
            setCount = 0,
            includesMobility = false,
            profile = profile,
        )
        val strength = CalorieEstimator.estimateSessionCaloriesKcal(
            sessionType = "A",
            durationMinutes = 30,
            setCount = 12,
            includesMobility = false,
            profile = profile,
        )
        assertTrue(cardio > strength)
    }

    @Test
    fun mobilityAddsSmallBonus() {
        val profile = CalorieEstimator.Profile(weightKg = 80f, heightCm = 175, ageYears = 29, sex = "unspecified")
        val noMobility = CalorieEstimator.estimateSessionCaloriesKcal("B", 40, 14, false, profile)
        val withMobility = CalorieEstimator.estimateSessionCaloriesKcal("B", 40, 14, true, profile)
        assertTrue(withMobility > noMobility)
    }
}

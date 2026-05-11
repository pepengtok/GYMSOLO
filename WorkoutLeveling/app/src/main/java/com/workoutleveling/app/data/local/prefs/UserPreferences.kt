package com.workoutleveling.app.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    private val onboardingBaselineDone = booleanPreferencesKey("onboarding_baseline_done")
    private val homeTipsDismissed = booleanPreferencesKey("home_tips_dismissed")
    private val profileWeightKg = floatPreferencesKey("profile_weight_kg")
    private val profileHeightCm = intPreferencesKey("profile_height_cm")
    private val profileAgeYears = intPreferencesKey("profile_age_years")
    private val profileSex = stringPreferencesKey("profile_sex")
    private val calorieWeeklyTargetKey = intPreferencesKey("calorie_weekly_target")
    private val coachSoundEnabled = booleanPreferencesKey("coach_sound_enabled")

    val baselineDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[onboardingBaselineDone] ?: false
    }

    val isHomeTipsDismissed: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[homeTipsDismissed] ?: false
    }

    val calorieProfileWeightKg: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[profileWeightKg] ?: 75f
    }
    val calorieProfileHeightCm: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[profileHeightCm] ?: 170
    }
    val calorieProfileAgeYears: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[profileAgeYears] ?: 30
    }
    val calorieProfileSex: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[profileSex] ?: "unspecified"
    }
    val calorieWeeklyTarget: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[calorieWeeklyTargetKey] ?: 1800
    }
    val isCoachSoundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[coachSoundEnabled] ?: true
    }

    suspend fun setBaselineDone(done: Boolean) {
        context.dataStore.edit { it[onboardingBaselineDone] = done }
    }

    suspend fun setHomeTipsDismissed(done: Boolean) {
        context.dataStore.edit { it[homeTipsDismissed] = done }
    }

    suspend fun setCalorieProfile(weightKg: Float, heightCm: Int, ageYears: Int, sex: String) {
        context.dataStore.edit {
            it[profileWeightKg] = weightKg.coerceIn(35f, 250f)
            it[profileHeightCm] = heightCm.coerceIn(120, 230)
            it[profileAgeYears] = ageYears.coerceIn(13, 100)
            it[profileSex] = sex
        }
    }

    suspend fun setCalorieWeeklyTarget(kcal: Int) {
        context.dataStore.edit { it[calorieWeeklyTargetKey] = kcal.coerceIn(500, 10000) }
    }

    suspend fun setCoachSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[coachSoundEnabled] = enabled }
    }
}

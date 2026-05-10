package com.workoutleveling.app.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    private val onboardingBaselineDone = booleanPreferencesKey("onboarding_baseline_done")

    val baselineDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[onboardingBaselineDone] ?: false
    }

    suspend fun setBaselineDone(done: Boolean) {
        context.dataStore.edit { it[onboardingBaselineDone] = done }
    }
}

package com.workoutleveling.app.ui.baseline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.workoutleveling.app.WorkoutLevelingApp
import com.workoutleveling.app.data.local.db.BodyMetricsEntity
import kotlinx.coroutines.launch

class BaselineViewModel(application: Application) : AndroidViewModel(application) {
    private val bodyDao = (application as WorkoutLevelingApp).database.bodyMetricsDao()
    private val prefs = (application as WorkoutLevelingApp).userPreferences

    fun skip(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.setBaselineDone(true)
            onDone()
        }
    }

    fun saveBaseline(
        weightKg: String,
        waistCm: String,
        notes: String,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val w = weightKg.trim().toFloatOrNull()
            val waist = waistCm.trim().toFloatOrNull()
            bodyDao.insert(
                BodyMetricsEntity(
                    recordedAtEpochMs = System.currentTimeMillis(),
                    weightKg = w,
                    waistCm = waist,
                    notes = notes.trim().takeIf { it.isNotEmpty() },
                    isBaseline = true,
                ),
            )
            prefs.setBaselineDone(true)
            onDone()
        }
    }
}

package com.workoutleveling.app.ui.bodycheckin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.workoutleveling.app.WorkoutLevelingApp
import com.workoutleveling.app.data.local.db.BodyMetricsEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BodyCheckInViewModel(application: Application) : AndroidViewModel(application) {
    private val bodyDao = (application as WorkoutLevelingApp).database.bodyMetricsDao()

    val recentEntries: StateFlow<List<BodyMetricsEntity>> =
        bodyDao.observeRecent(12)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveCheckIn(
        weightKg: String,
        waistCm: String,
        notes: String,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            bodyDao.insert(
                BodyMetricsEntity(
                    recordedAtEpochMs = System.currentTimeMillis(),
                    weightKg = weightKg.trim().toFloatOrNull(),
                    waistCm = waistCm.trim().toFloatOrNull(),
                    notes = notes.trim().takeIf { it.isNotEmpty() },
                    isBaseline = false,
                ),
            )
            onDone()
        }
    }
}

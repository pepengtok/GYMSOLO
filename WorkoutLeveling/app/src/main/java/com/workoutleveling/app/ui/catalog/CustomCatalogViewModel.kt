package com.workoutleveling.app.ui.catalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.workoutleveling.app.WorkoutLevelingApp
import com.workoutleveling.app.data.local.db.ExerciseCatalogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomCatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as WorkoutLevelingApp).database.exerciseCatalogDao()

    val customEntries: StateFlow<List<ExerciseCatalogEntity>> =
        dao.observeCustomEntries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun renameEntry(id: Long, newName: String, isHg60: Boolean) {
        val n = newName.trim()
        if (n.isBlank()) return
        viewModelScope.launch {
            val tag = if (isHg60) "CUSTOM_HG60" else "CUSTOM"
            dao.updateCustomEntry(id, n, isHg60, tag)
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { dao.deleteCustomEntry(id) }
    }
}

package com.workoutleveling.app.data.local.db

import com.workoutleveling.app.domain.catalog.CatalogExercise
import com.workoutleveling.app.domain.catalog.WorkoutCatalog
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ExerciseCatalogSeed {
    private val mutex = Mutex()

    private fun CatalogExercise.equipmentTagForDb(): String = when {
        isHg60Station -> "HG60"
        displayName.contains("barbel", ignoreCase = true) -> "BARBELL"
        displayName.contains("dumbbell", ignoreCase = true) -> "DUMBBELL"
        displayName.contains("Jalan", ignoreCase = true) -> "CARDIO"
        else -> "BODYWEIGHT"
    }

    suspend fun ensureSeeded(catalogDao: ExerciseCatalogDao) {
        if (catalogDao.count() > 0) return
        mutex.withLock {
            if (catalogDao.count() > 0) return
            val rows = WorkoutCatalog.builtInExercisesOrdered().mapIndexed { i, e ->
                ExerciseCatalogEntity(
                    displayName = e.displayName,
                    equipmentTag = e.equipmentTagForDb(),
                    isHg60Station = e.isHg60Station,
                    sortOrder = i,
                )
            }
            catalogDao.insertAll(rows)
        }
    }
}

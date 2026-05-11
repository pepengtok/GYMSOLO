package com.workoutleveling.app.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_catalog",
    indices = [
        Index(value = ["displayName"], unique = true),
    ],
)
data class ExerciseCatalogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val displayName: String,
    /** Contoh: HG60, BARBELL, DUMBBELL, CARDIO, BODYWEIGHT. */
    val equipmentTag: String,
    val isHg60Station: Boolean,
    val sortOrder: Int,
)

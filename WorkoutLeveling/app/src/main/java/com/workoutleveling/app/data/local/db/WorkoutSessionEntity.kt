package com.workoutleveling.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long? = null,
    /** A, B, atau Cardio — lihat roadmap */
    val type: String,
    val notes: String? = null,
    val effortRpe: Int? = null,
)

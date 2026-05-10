package com.workoutleveling.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_metrics")
data class BodyMetricsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordedAtEpochMs: Long,
    val weightKg: Float?,
    val waistCm: Float?,
    val notes: String?,
    val isBaseline: Boolean,
)

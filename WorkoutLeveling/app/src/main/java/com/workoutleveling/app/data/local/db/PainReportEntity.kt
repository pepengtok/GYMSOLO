package com.workoutleveling.app.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pain_reports",
    indices = [Index("exerciseName"), Index("reportedAtEpochMs")],
)
data class PainReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseName: String,
    val reportedAtEpochMs: Long,
)

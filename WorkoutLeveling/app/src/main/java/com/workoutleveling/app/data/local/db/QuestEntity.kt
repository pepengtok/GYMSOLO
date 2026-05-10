package com.workoutleveling.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String,
    val title: String,
    /** "daily" | "weekly" */
    val questType: String,
    val targetValue: Int,
    val progressValue: Int,
    /** "active" | "completed" */
    val status: String,
    /** Untuk harian: yyyy-MM-dd (local). */
    val periodKey: String,
)

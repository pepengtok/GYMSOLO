package com.workoutleveling.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_state")
data class PlayerStateEntity(
    @PrimaryKey val id: Int = 1,
    val xpTotal: Int = 0,
    val streakDays: Int = 0,
    /** ISO-8601 local date (yyyy-MM-dd) terakhir ada sesi tercatat. */
    val lastSessionLocalDate: String? = null,
    val rankCode: String = "E",
)

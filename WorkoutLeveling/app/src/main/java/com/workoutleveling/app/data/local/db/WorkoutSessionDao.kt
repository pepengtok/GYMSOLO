package com.workoutleveling.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions ORDER BY startedAtEpochMs DESC")
    fun observeAll(): Flow<List<WorkoutSessionEntity>>

    @Insert
    suspend fun insert(session: WorkoutSessionEntity): Long
}

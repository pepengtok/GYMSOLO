package com.workoutleveling.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions ORDER BY startedAtEpochMs DESC")
    fun observeAll(): Flow<List<WorkoutSessionEntity>>

    @Query(
        "SELECT * FROM workout_sessions WHERE startedAtEpochMs >= :startMs AND startedAtEpochMs <= :endMs",
    )
    suspend fun listStartedBetween(startMs: Long, endMs: Long): List<WorkoutSessionEntity>

    @Query("SELECT type FROM workout_sessions ORDER BY startedAtEpochMs DESC LIMIT 1")
    suspend fun getLatestTypeOrNull(): String?

    @Insert
    suspend fun insert(session: WorkoutSessionEntity): Long
}

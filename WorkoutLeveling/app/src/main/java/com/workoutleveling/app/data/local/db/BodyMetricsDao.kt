package com.workoutleveling.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMetricsDao {
    @Insert
    suspend fun insert(entry: BodyMetricsEntity): Long

    @Query("SELECT * FROM body_metrics WHERE isBaseline = 1 ORDER BY recordedAtEpochMs ASC LIMIT 1")
    suspend fun getBaseline(): BodyMetricsEntity?

    @Query("SELECT * FROM body_metrics ORDER BY recordedAtEpochMs DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<BodyMetricsEntity>>
}

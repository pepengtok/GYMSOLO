package com.workoutleveling.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PainReportDao {
    @Insert
    suspend fun insertAll(items: List<PainReportEntity>)

    @Query(
        """
        SELECT COUNT(*) FROM pain_reports
        WHERE exerciseName = :exerciseName COLLATE NOCASE
        AND reportedAtEpochMs >= :sinceEpochMs
        """,
    )
    suspend fun countRecentForExercise(exerciseName: String, sinceEpochMs: Long): Int
}

package com.workoutleveling.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseSetDao {
    @Query("SELECT * FROM exercise_sets")
    fun observeAll(): Flow<List<ExerciseSetEntity>>

    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY exerciseName, setIndex")
    fun observeForSession(sessionId: Long): Flow<List<ExerciseSetEntity>>

    @Insert
    suspend fun insert(set: ExerciseSetEntity): Long

    @Insert
    suspend fun insertAll(sets: List<ExerciseSetEntity>)

    /**
     * Set terakhir (setIndex tertinggi) pada sesi paling baru yang memuat latihan ini.
     */
    @Query(
        """
        SELECT es.* FROM exercise_sets es
        WHERE es.sessionId = (
            SELECT es3.sessionId FROM exercise_sets es3
            INNER JOIN workout_sessions ws ON ws.id = es3.sessionId
            WHERE es3.exerciseName = :exerciseName COLLATE NOCASE
            ORDER BY ws.startedAtEpochMs DESC
            LIMIT 1
        )
        AND es.exerciseName = :exerciseName COLLATE NOCASE
        ORDER BY es.setIndex DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestSetForExerciseName(exerciseName: String): ExerciseSetEntity?

    @Query(
        """
        SELECT es.* FROM exercise_sets es
        INNER JOIN workout_sessions ws ON ws.id = es.sessionId
        WHERE es.exerciseName = :exerciseName COLLATE NOCASE
        ORDER BY ws.startedAtEpochMs DESC, es.setIndex DESC
        LIMIT 2
        """,
    )
    suspend fun getLatestTwoSetsForExerciseName(exerciseName: String): List<ExerciseSetEntity>
}

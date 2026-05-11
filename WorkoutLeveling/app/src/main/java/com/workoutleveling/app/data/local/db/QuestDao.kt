package com.workoutleveling.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests WHERE periodKey = :day ORDER BY questType ASC")
    fun observeForDay(day: String): Flow<List<QuestEntity>>

    @Query(
        """
        SELECT * FROM quests
        WHERE periodKey = :day OR periodKey = :weekKey
        ORDER BY questType ASC, id ASC
        """,
    )
    fun observeForDayAndWeek(day: String, weekKey: String): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun getById(id: String): QuestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(quest: QuestEntity)
}

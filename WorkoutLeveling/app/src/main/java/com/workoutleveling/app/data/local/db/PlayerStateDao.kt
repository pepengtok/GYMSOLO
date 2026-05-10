package com.workoutleveling.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerStateDao {
    @Query("SELECT * FROM player_state WHERE id = 1")
    fun observe(): Flow<PlayerStateEntity?>

    @Query("SELECT * FROM player_state WHERE id = 1")
    suspend fun getRow(): PlayerStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: PlayerStateEntity)
}

package com.workoutleveling.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseCatalogDao {
    @Query("SELECT COUNT(*) FROM exercise_catalog")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ExerciseCatalogEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ExerciseCatalogEntity): Long

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM exercise_catalog")
    suspend fun maxSortOrder(): Int

    @Query("SELECT COUNT(*) FROM exercise_catalog WHERE displayName = :name COLLATE NOCASE")
    suspend fun countByNameIgnoreCase(name: String): Int

    @Query(
        """
        SELECT * FROM exercise_catalog
        WHERE equipmentTag LIKE 'CUSTOM%'
        ORDER BY displayName COLLATE NOCASE ASC
        """,
    )
    fun observeCustomEntries(): Flow<List<ExerciseCatalogEntity>>

    @Query(
        """
        SELECT displayName FROM exercise_catalog
        WHERE (:hg60Only = 0 OR isHg60Station = 1)
        AND (:query = '' OR displayName LIKE '%' || :query || '%' COLLATE NOCASE)
        ORDER BY sortOrder ASC, displayName ASC
        LIMIT :limit
        """,
    )
    suspend fun searchCatalog(query: String, hg60Only: Boolean, limit: Int): List<String>

    @Query(
        """
        SELECT DISTINCT exerciseName FROM exercise_sets
        WHERE exerciseName != ''
        AND (:query = '' OR exerciseName LIKE '%' || :query || '%' COLLATE NOCASE)
        ORDER BY exerciseName ASC
        LIMIT :limit
        """,
    )
    suspend fun searchDistinctLoggedNames(query: String, limit: Int): List<String>

    @Query(
        """
        UPDATE exercise_catalog
        SET displayName = :newName,
            equipmentTag = :newTag,
            isHg60Station = :isHg60
        WHERE id = :id AND equipmentTag LIKE 'CUSTOM%'
        """,
    )
    suspend fun updateCustomEntry(
        id: Long,
        newName: String,
        isHg60: Boolean,
        newTag: String,
    ): Int

    @Query("DELETE FROM exercise_catalog WHERE id = :id AND equipmentTag LIKE 'CUSTOM%'")
    suspend fun deleteCustomEntry(id: Long): Int
}

package com.ai.jobfinder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSearchDao {
    @Query("SELECT * FROM saved_searches ORDER BY id DESC")
    fun observeAll(): Flow<List<SavedSearchEntity>>

    @Query("SELECT * FROM saved_searches WHERE enabled = 1")
    suspend fun getEnabled(): List<SavedSearchEntity>

    @Insert
    suspend fun insert(search: SavedSearchEntity)

    @Delete
    suspend fun delete(search: SavedSearchEntity)
}


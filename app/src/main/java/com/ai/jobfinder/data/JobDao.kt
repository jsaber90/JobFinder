package com.ai.jobfinder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY publishedAt DESC, title ASC")
    fun observeJobs(): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(jobs: List<JobEntity>): List<Long>

    @Query("UPDATE jobs SET notified = 1 WHERE id IN (:ids)")
    suspend fun markNotified(ids: List<String>)
}


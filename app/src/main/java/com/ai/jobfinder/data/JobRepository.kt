package com.ai.jobfinder.data

import kotlinx.coroutines.flow.Flow

class JobRepository(private val dao: JobDao) {
    fun observeJobs(): Flow<List<JobEntity>> = dao.observeJobs()

    suspend fun saveJobs(jobs: List<JobEntity>) {
        dao.insertAll(jobs)
    }
}


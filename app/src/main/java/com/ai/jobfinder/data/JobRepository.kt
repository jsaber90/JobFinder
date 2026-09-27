package com.ai.jobfinder.data

import kotlinx.coroutines.flow.Flow

class JobRepository(
    private val dao: JobDao,
    private val savedSearchDao: SavedSearchDao
) {
    fun observeJobs(): Flow<List<JobEntity>> = dao.observeJobs()
    fun observeSavedSearches(): Flow<List<SavedSearchEntity>> = savedSearchDao.observeAll()

    suspend fun saveSearch(search: SavedSearchEntity) = savedSearchDao.insert(search)
    suspend fun deleteSearch(search: SavedSearchEntity) = savedSearchDao.delete(search)

    suspend fun saveJobs(jobs: List<JobEntity>) {
        dao.insertAll(jobs)
    }

    suspend fun syncFromJooble(api: JoobleApi, apiKey: String, keyword: String) {
        if (apiKey.isBlank()) return
        val response = api.search(
            apiKey,
            JoobleSearchRequest(keywords = keyword, location = "Egypt")
        )
        saveJobs(response.jobs.mapNotNull { job ->
            val id = job.id?.toString() ?: job.link ?: return@mapNotNull null
            JobEntity(
                id = id,
                title = job.title.orEmpty(),
                company = job.company.orEmpty(),
                location = job.location.orEmpty(),
                description = job.snippet.orEmpty(),
                url = job.link.orEmpty(),
                source = job.source ?: "Jooble",
                publishedAt = job.updated
            )
        })
    }
}


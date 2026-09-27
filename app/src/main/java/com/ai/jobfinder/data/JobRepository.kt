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

    suspend fun saveJobs(jobs: List<JobEntity>): List<JobEntity> {
        if (jobs.isEmpty()) return emptyList()
        return dao.insertAll(jobs)
            .mapIndexedNotNull { index, rowId -> jobs[index].takeIf { rowId != -1L } }
    }

    suspend fun syncFromJooble(api: JoobleApi, apiKey: String, keyword: String): List<JobEntity> {
        if (apiKey.isBlank()) return emptyList()
        val response = api.search(
            apiKey,
            JoobleSearchRequest(keywords = keyword, location = "Egypt")
        )
        return saveJobs(response.jobs.mapNotNull { job ->
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


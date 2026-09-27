package com.ai.jobfinder.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class JobSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val database = AppDatabase.create(applicationContext)
        val repository = JobRepository(database.jobDao(), database.savedSearchDao())
        return runCatching {
            database.savedSearchDao().getEnabled().forEach { search ->
                repository.syncFromJooble(
                    api = JoobleClient.api,
                    apiKey = com.ai.jobfinder.BuildConfig.JOOBLE_API_KEY,
                    keyword = search.keyword
                )
                // Email delivery belongs in a protected backend/provider integration.
            }
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        private const val WORK_NAME = "jobfinder-periodic-sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<JobSyncWorker>(1, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}


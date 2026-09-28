package com.ai.jobfinder.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ai.jobfinder.R
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
                val newJobs = repository.syncFromJooble(
                    api = JoobleClient.api,
                    apiKey = com.ai.jobfinder.BuildConfig.JOOBLE_API_KEY,
                    keyword = search.keyword
                )
                newJobs.forEach { job -> notifyNewJob(search.keyword, job) }
            }
            Result.success()
        }.getOrElse { Result.retry() }
    }

    private fun notifyNewJob(keyword: String, job: JobEntity) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                android.app.NotificationChannel(
                    CHANNEL_ID,
                    applicationContext.getString(R.string.job_notifications),
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        val openJobIntent = Intent(Intent.ACTION_VIEW, Uri.parse(job.url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openJobPendingIntent = PendingIntent.getActivity(
            applicationContext,
            job.id.hashCode(),
            openJobIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(applicationContext.getString(R.string.new_job_found))
            .setContentText("$keyword: ${job.title}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("${job.title} · ${job.company}\n${job.location}"))
            .setContentIntent(openJobPendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(job.id.hashCode(), notification)
    }

    companion object {
        private const val WORK_NAME = "jobfinder-periodic-sync"
        private const val CHANNEL_ID = "job_notifications"

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


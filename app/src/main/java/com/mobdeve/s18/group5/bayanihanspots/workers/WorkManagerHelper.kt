package com.mobdeve.s18.group5.bayanihanspots.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Manager for scheduling and controlling WorkManager jobs.
 */
object WorkManagerHelper {
    private const val TAG = "WorkManagerHelper"
    private const val UPLOAD_SYNC_WORK = "upload_sync_work"
    private const val PERIODIC_SYNC_WORK = "periodic_sync_work"

    /**
     * Schedule an immediate one-time sync when network becomes available.
     * Call this after creating offline content.
     */
    fun scheduleImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadRequest = OneTimeWorkRequestBuilder<UploadSyncWorker>()
            .setConstraints(constraints)
            .addTag("upload_sync")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UPLOAD_SYNC_WORK,
            ExistingWorkPolicy.REPLACE,
            uploadRequest
        )

        Log.d(TAG, "Scheduled immediate sync work")
    }

    /**
     * Schedule periodic background sync (every 15 minutes when connected).
     * Call this once on app startup.
     */
    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<UploadSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("periodic_sync")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )

        Log.d(TAG, "Scheduled periodic sync work")
    }

    /**
     * Cancel all sync work.
     */
    fun cancelAllSync(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("upload_sync")
        WorkManager.getInstance(context).cancelAllWorkByTag("periodic_sync")
        Log.d(TAG, "Cancelled all sync work")
    }
}


package com.mobdeve.s18.group5.bayanihanspots.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.mobdeve.s18.group5.bayanihanspots.data.local.BayanihanDatabase
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.PendingUploadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that processes pending uploads when network is available.
 * Handles spots, reviews, and photos that were created offline.
 */
class PendingUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = BayanihanDatabase.getInstance(context)
    private val pendingUploadDao = database.pendingUploadDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val gson = Gson()

    companion object {
        private const val TAG = "PendingUploadWorker"
        private const val WORK_NAME = "pending_upload_work"
        private const val MAX_RETRY_COUNT = 3

        /**
         * Schedule periodic sync work
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<PendingUploadWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    1, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            Log.d(TAG, "Scheduled periodic upload work")
        }

        /**
         * Trigger immediate one-time sync
         */
        fun triggerNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<PendingUploadWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
            Log.d(TAG, "Triggered immediate upload work")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting pending upload processing")

        try {
            val pendingUploads = pendingUploadDao.getPendingUploads()

            if (pendingUploads.isEmpty()) {
                Log.d(TAG, "No pending uploads")
                return@withContext Result.success()
            }

            Log.d(TAG, "Processing ${pendingUploads.size} pending uploads")

            var successCount = 0
            var failureCount = 0

            for (upload in pendingUploads) {
                try {
                    when (upload.type) {
                        "SPOT" -> processSpotUpload(upload)
                        "REVIEW" -> processReviewUpload(upload)
                        "PHOTO" -> processPhotoUpload(upload)
                        else -> {
                            Log.w(TAG, "Unknown upload type: ${upload.type}")
                            pendingUploadDao.deleteById(upload.id)
                        }
                    }
                    // Success - delete from pending
                    pendingUploadDao.deleteById(upload.id)
                    successCount++
                    Log.d(TAG, "Successfully uploaded: ${upload.type} (id=${upload.id})")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload ${upload.type}: ${e.message}")
                    failureCount++

                    if (upload.retryCount >= MAX_RETRY_COUNT) {
                        pendingUploadDao.markFailed(upload.id, "FAILED", e.message)
                    } else {
                        pendingUploadDao.markFailed(upload.id, "PENDING", e.message)
                    }
                }
            }

            Log.d(TAG, "Upload complete: $successCount success, $failureCount failed")

            if (failureCount > 0 && failureCount == pendingUploads.size) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun processSpotUpload(upload: PendingUploadEntity) {
        val spotData = gson.fromJson(upload.payload, PendingSpotData::class.java)

        val spotMap = hashMapOf(
            "name" to spotData.name,
            "type" to spotData.type,
            "status" to spotData.status,
            "crowdLevel" to spotData.crowdLevel,
            "description" to spotData.description,
            "coordinates" to GeoPoint(spotData.latitude, spotData.longitude),
            "userID" to spotData.userID,
            "approvalStatus" to "PENDING",
            "modificationType" to "NEW",
            "imageList" to spotData.imageUrls
        )

        firestore.collection("spots")
            .add(spotMap)
            .await()
    }

    private suspend fun processReviewUpload(upload: PendingUploadEntity) {
        val reviewData = gson.fromJson(upload.payload, PendingReviewData::class.java)

        val reviewMap = hashMapOf(
            "spotID" to reviewData.spotID,
            "userID" to reviewData.userID,
            "userName" to reviewData.userName,
            "rating" to reviewData.rating,
            "comment" to reviewData.comment,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("spots")
            .document(reviewData.spotID)
            .collection("reviews")
            .add(reviewMap)
            .await()
    }

    private suspend fun processPhotoUpload(upload: PendingUploadEntity) {
        // Photo uploads require Firebase Storage - simplified for now
        val photoData = gson.fromJson(upload.payload, PendingPhotoData::class.java)
        // TODO: Implement photo upload to Firebase Storage
        Log.d(TAG, "Photo upload placeholder for spot: ${photoData.spotID}")
    }

    // Data classes for JSON serialization
    data class PendingSpotData(
        val name: String,
        val type: String,
        val status: String,
        val crowdLevel: String,
        val description: String,
        val latitude: Double,
        val longitude: Double,
        val userID: String,
        val imageUrls: List<String> = emptyList()
    )

    data class PendingReviewData(
        val spotID: String,
        val userID: String,
        val userName: String,
        val rating: Int,
        val comment: String
    )

    data class PendingPhotoData(
        val spotID: String,
        val localFilePath: String,
        val userID: String
    )
}


package com.mobdeve.s18.group5.bayanihanspots.data.sync

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.mobdeve.s18.group5.bayanihanspots.data.local.BayanihanDatabase
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.PendingUploadEntity
import kotlinx.coroutines.flow.Flow

/**
 * Manager for queuing offline data for later upload.
 * When the user creates spots, reviews, or photos while offline,
 * this class saves them to Room and triggers WorkManager sync.
 */
class PendingUploadManager(private val context: Context) {
    private val database = BayanihanDatabase.getInstance(context)
    private val pendingUploadDao = database.pendingUploadDao()
    private val gson = Gson()

    companion object {
        private const val TAG = "PendingUploadManager"

        @Volatile
        private var INSTANCE: PendingUploadManager? = null

        fun getInstance(context: Context): PendingUploadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PendingUploadManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    /**
     * Queue a spot for upload when online
     */
    suspend fun queueSpotUpload(
        name: String,
        type: String,
        status: String,
        crowdLevel: String,
        description: String,
        latitude: Double,
        longitude: Double,
        userID: String,
        imageUrls: List<String> = emptyList()
    ): Long {
        val spotData = PendingUploadWorker.PendingSpotData(
            name = name,
            type = type,
            status = status,
            crowdLevel = crowdLevel,
            description = description,
            latitude = latitude,
            longitude = longitude,
            userID = userID,
            imageUrls = imageUrls
        )

        val entity = PendingUploadEntity(
            type = "SPOT",
            payload = gson.toJson(spotData)
        )

        val id = pendingUploadDao.insert(entity)
        Log.d(TAG, "Queued spot upload: $name (id=$id)")

        // Trigger WorkManager to try upload
        PendingUploadWorker.triggerNow(context)

        return id
    }

    /**
     * Queue a review for upload when online
     */
    suspend fun queueReviewUpload(
        spotID: String,
        userID: String,
        userName: String,
        rating: Int,
        comment: String
    ): Long {
        val reviewData = PendingUploadWorker.PendingReviewData(
            spotID = spotID,
            userID = userID,
            userName = userName,
            rating = rating,
            comment = comment
        )

        val entity = PendingUploadEntity(
            type = "REVIEW",
            payload = gson.toJson(reviewData)
        )

        val id = pendingUploadDao.insert(entity)
        Log.d(TAG, "Queued review upload for spot: $spotID (id=$id)")

        // Trigger WorkManager to try upload
        PendingUploadWorker.triggerNow(context)

        return id
    }

    /**
     * Queue a photo for upload when online
     */
    suspend fun queuePhotoUpload(
        spotID: String,
        localFilePath: String,
        userID: String
    ): Long {
        val photoData = PendingUploadWorker.PendingPhotoData(
            spotID = spotID,
            localFilePath = localFilePath,
            userID = userID
        )

        val entity = PendingUploadEntity(
            type = "PHOTO",
            payload = gson.toJson(photoData)
        )

        val id = pendingUploadDao.insert(entity)
        Log.d(TAG, "Queued photo upload for spot: $spotID (id=$id)")

        // Trigger WorkManager to try upload
        PendingUploadWorker.triggerNow(context)

        return id
    }

    /**
     * Observe pending upload count (for UI indicators)
     */
    fun observePendingCount(): Flow<Int> {
        return pendingUploadDao.observePendingCount()
    }

    /**
     * Observe all pending uploads
     */
    fun observePendingUploads(): Flow<List<PendingUploadEntity>> {
        return pendingUploadDao.observePendingUploads()
    }

    /**
     * Cancel a pending upload
     */
    suspend fun cancelPendingUpload(id: Long) {
        pendingUploadDao.deleteById(id)
        Log.d(TAG, "Cancelled pending upload: $id")
    }

    /**
     * Initialize WorkManager scheduler
     */
    fun initializeWorker() {
        PendingUploadWorker.schedule(context)
    }
}


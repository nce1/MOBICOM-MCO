package com.mobdeve.s18.group5.bayanihanspots.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mobdeve.s18.group5.bayanihanspots.data.local.BayanihanDatabase
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.PendingUploadEntity
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Worker that uploads pending spots, reviews, and photos when network is available.
 * Implements the offline-first sync strategy.
 */
class UploadSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "UploadSyncWorker"
    }

    private val database = BayanihanDatabase.getInstance(applicationContext)
    private val pendingUploadDao = database.pendingUploadDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val gson = Gson()

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting upload sync...")

        val pendingUploads = pendingUploadDao.getPendingUploads()

        if (pendingUploads.isEmpty()) {
            Log.d(TAG, "No pending uploads")
            return Result.success()
        }

        Log.d(TAG, "Found ${pendingUploads.size} pending uploads")

        var successCount = 0
        var failCount = 0

        for (upload in pendingUploads) {
            try {
                pendingUploadDao.updateStatus(upload.id, "UPLOADING")

                when (upload.type) {
                    "SPOT" -> uploadSpot(upload)
                    "REVIEW" -> uploadReview(upload)
                    "PHOTO" -> uploadPhoto(upload)
                    else -> {
                        Log.w(TAG, "Unknown upload type: ${upload.type}")
                        pendingUploadDao.deleteById(upload.id)
                    }
                }

                // Success - remove from pending
                pendingUploadDao.deleteById(upload.id)
                successCount++
                Log.d(TAG, "Successfully uploaded ${upload.type} (id: ${upload.id})")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload ${upload.type}: ${e.message}")
                failCount++

                // Mark as failed with error
                pendingUploadDao.markFailed(upload.id, "FAILED", e.message)

                // If too many retries, skip
                if (upload.retryCount >= 3) {
                    Log.w(TAG, "Max retries reached for upload ${upload.id}, marking as permanently failed")
                }
            }
        }

        Log.d(TAG, "Upload sync complete: $successCount success, $failCount failed")

        return if (failCount > 0 && successCount == 0) {
            Result.retry()
        } else {
            Result.success()
        }
    }

    private suspend fun uploadSpot(upload: PendingUploadEntity) {
        val json = gson.fromJson(upload.payload, JsonObject::class.java)

        // First, upload images if any
        val localImageUris = json.getAsJsonArray("localImageUris")?.map { it.asString } ?: emptyList()
        val uploadedUrls = mutableListOf<String>()

        for (uriString in localImageUris) {
            val uri = Uri.parse(uriString)
            val url = uploadImageToStorage(uri)
            if (url != null) {
                uploadedUrls.add(url)
            }
        }

        // Create spot document
        val spotData = hashMapOf(
            "name" to json.get("name").asString,
            "type" to json.get("type").asString,
            "crowdLevel" to json.get("crowdLevel").asString,
            "description" to json.get("description").asString,
            "status" to "OPEN",
            "userID" to json.get("userID").asString,
            "coordinates" to GeoPoint(
                json.get("latitude").asDouble,
                json.get("longitude").asDouble
            ),
            "imageList" to uploadedUrls,
            "approvalStatus" to "PENDING",
            "modificationType" to "NEW"
        )

        firestore.collection("spots").add(spotData).await()
    }

    private suspend fun uploadReview(upload: PendingUploadEntity) {
        val json = gson.fromJson(upload.payload, JsonObject::class.java)

        val spotId = json.get("spotId").asString
        val reviewData = hashMapOf(
            "userID" to json.get("userID").asString,
            "rating" to json.get("rating").asInt,
            "comment" to json.get("comment").asString,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("spots").document(spotId)
            .collection("reviews")
            .add(reviewData)
            .await()
    }

    private suspend fun uploadPhoto(upload: PendingUploadEntity) {
        val json = gson.fromJson(upload.payload, JsonObject::class.java)

        val localUri = json.get("localUri").asString
        val spotId = json.get("spotId").asString

        val downloadUrl = uploadImageToStorage(Uri.parse(localUri))

        if (downloadUrl != null) {
            // Add photo URL to spot's image list
            firestore.collection("spots").document(spotId)
                .update("imageList", com.google.firebase.firestore.FieldValue.arrayUnion(downloadUrl))
                .await()
        }
    }

    private suspend fun uploadImageToStorage(uri: Uri): String? {
        return try {
            val ref = storage.reference.child("spot_images/${UUID.randomUUID()}.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload image: ${e.message}")
            null
        }
    }
}


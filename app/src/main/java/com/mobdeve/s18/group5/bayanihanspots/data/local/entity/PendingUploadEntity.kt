package com.mobdeve.s18.group5.bayanihanspots.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing pending uploads (spots, reviews, photos) that need to sync when online.
 * Used by WorkManager to queue items created while offline.
 */
@Entity(tableName = "pending_uploads")
data class PendingUploadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "SPOT", "REVIEW", "PHOTO"
    val payload: String, // JSON serialized data
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, UPLOADING, FAILED
    val retryCount: Int = 0,
    val lastError: String? = null
)


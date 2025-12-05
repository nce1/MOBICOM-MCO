package com.mobdeve.s18.group5.bayanihanspots.data.local.dao

import androidx.room.*
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.PendingUploadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingUploadDao {
    @Query("SELECT * FROM pending_uploads WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun observePendingUploads(): Flow<List<PendingUploadEntity>>

    @Query("SELECT * FROM pending_uploads WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingUploads(): List<PendingUploadEntity>

    @Query("SELECT * FROM pending_uploads WHERE type = :type AND status = 'PENDING'")
    suspend fun getPendingUploadsByType(type: String): List<PendingUploadEntity>

    @Query("SELECT COUNT(*) FROM pending_uploads WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pendingUpload: PendingUploadEntity): Long

    @Update
    suspend fun update(pendingUpload: PendingUploadEntity)

    @Delete
    suspend fun delete(pendingUpload: PendingUploadEntity)

    @Query("DELETE FROM pending_uploads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE pending_uploads SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE pending_uploads SET status = :status, retryCount = retryCount + 1, lastError = :error WHERE id = :id")
    suspend fun markFailed(id: Long, status: String = "FAILED", error: String?)

    @Query("DELETE FROM pending_uploads WHERE status = 'PENDING' AND id = :id")
    suspend fun deletePendingById(id: Long)
}


package com.mobdeve.s18.group5.bayanihanspots.data.local.dao

import androidx.room.*
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.FavoriteEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteEventDao {
    @Query("SELECT * FROM favorite_events WHERE userId = :userId ORDER BY addedAt DESC")
    fun observeFavoritesByUser(userId: String): Flow<List<FavoriteEventEntity>>

    @Query("SELECT eventId FROM favorite_events WHERE userId = :userId")
    fun observeFavoriteEventIds(userId: String): Flow<List<String>>

    @Query("SELECT * FROM favorite_events WHERE userId = :userId")
    suspend fun getFavoritesByUser(userId: String): List<FavoriteEventEntity>

    @Query("SELECT * FROM favorite_events WHERE id = :id")
    suspend fun getFavoriteById(id: String): FavoriteEventEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_events WHERE eventId = :eventId AND userId = :userId)")
    suspend fun isFavorite(eventId: String, userId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_events WHERE eventId = :eventId AND userId = :userId)")
    fun observeIsFavorite(eventId: String, userId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEventEntity)

    @Delete
    suspend fun delete(favorite: FavoriteEventEntity)

    @Query("DELETE FROM favorite_events WHERE eventId = :eventId AND userId = :userId")
    suspend fun deleteByEventAndUser(eventId: String, userId: String)

    @Query("DELETE FROM favorite_events WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}


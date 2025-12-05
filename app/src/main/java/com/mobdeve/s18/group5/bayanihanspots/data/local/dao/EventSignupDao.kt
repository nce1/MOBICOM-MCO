package com.mobdeve.s18.group5.bayanihanspots.data.local.dao

import androidx.room.*
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.EventSignupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventSignupDao {
    @Query("SELECT * FROM event_signups WHERE userId = :userId AND status = 'CONFIRMED' ORDER BY joinedAt DESC")
    fun observeSignupsByUser(userId: String): Flow<List<EventSignupEntity>>

    @Query("SELECT eventId FROM event_signups WHERE userId = :userId AND status = 'CONFIRMED'")
    fun observeJoinedEventIds(userId: String): Flow<List<String>>

    @Query("SELECT * FROM event_signups WHERE userId = :userId AND status = 'CONFIRMED'")
    suspend fun getSignupsByUser(userId: String): List<EventSignupEntity>

    @Query("SELECT * FROM event_signups WHERE id = :id")
    suspend fun getSignupById(id: String): EventSignupEntity?

    @Query("SELECT * FROM event_signups WHERE eventId = :eventId AND userId = :userId")
    suspend fun getSignupByEventAndUser(eventId: String, userId: String): EventSignupEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM event_signups WHERE eventId = :eventId AND userId = :userId AND status = 'CONFIRMED')")
    suspend fun hasJoined(eventId: String, userId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM event_signups WHERE eventId = :eventId AND userId = :userId AND status = 'CONFIRMED')")
    fun observeHasJoined(eventId: String, userId: String): Flow<Boolean>

    @Query("SELECT * FROM event_signups WHERE status = 'CONFIRMED' AND notificationScheduled = 0")
    suspend fun getSignupsNeedingNotifications(): List<EventSignupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(signup: EventSignupEntity)

    @Update
    suspend fun update(signup: EventSignupEntity)

    @Query("UPDATE event_signups SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE event_signups SET notificationScheduled = 1 WHERE id = :id")
    suspend fun markNotificationScheduled(id: String)

    @Delete
    suspend fun delete(signup: EventSignupEntity)

    @Query("DELETE FROM event_signups WHERE eventId = :eventId AND userId = :userId")
    suspend fun deleteByEventAndUser(eventId: String, userId: String)

    @Query("DELETE FROM event_signups WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}


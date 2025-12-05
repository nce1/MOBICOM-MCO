package com.mobdeve.s18.group5.bayanihanspots.data.local.dao

import androidx.room.*
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.EventEntity
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.SpotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE approvalStatus = 'APPROVED' ORDER BY schedule ASC")
    fun observeApprovedEvents(): Flow<List<EventEntity>>
    @Query("SELECT * FROM events ORDER BY schedule ASC")
    fun observeAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun observeEventById(eventId: String): Flow<EventEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()

    @Transaction
    suspend fun replaceAllEvents(events: List<EventEntity>) {
        deleteAllEvents()
        insertEvents(events)
    }
}


package com.mobdeve.s18.group5.bayanihanspots.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing user's favorite events locally.
 */
@Entity(tableName = "favorite_events")
data class FavoriteEventEntity(
    @PrimaryKey
    val id: String, // Composite key: eventId_userId
    val eventId: String,
    val userId: String,
    val eventTitle: String,
    val eventSchedule: String,
    val addedAt: Long = System.currentTimeMillis()
)


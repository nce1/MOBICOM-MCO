package com.mobdeve.s18.group5.bayanihanspots.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing user's RSVP/joined events locally.
 */
@Entity(tableName = "event_signups")
data class EventSignupEntity(
    @PrimaryKey
    val id: String,
    val eventId: String,
    val userId: String,
    val userEmail: String,
    val eventTitle: String,
    val eventSchedule: String,
    val eventScheduleMillis: Long?,
    val status: String = "CONFIRMED",
    val joinedAt: Long = System.currentTimeMillis(),
    val notificationScheduled: Boolean = false
)



package com.mobdeve.s18.group5.bayanihanspots.data.local.entity

import android.health.connect.datatypes.units.Volume
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.google.firebase.firestore.GeoPoint

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val schedule: String,
    val locationLabel: String,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val host: String?,
    val maxVolunteers: Int?,
    val reminderOffsetsDays: String, // comma-separated
    val scheduleUtcMillis: Long?,
    val creatorId: String,
    val approvalStatus: String,
    val modificationType: String,
    val currentVolunteers: Int?
) {
    fun toEvent(): Event {
        return Event(
            id = id,
            title = title,
            schedule = schedule,
            locationLabel = locationLabel,
            description = description,
            coordinates = if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null,
            host = host,
            maxVolunteers = maxVolunteers,
            reminderOffsetsDays = parseReminderOffsets(reminderOffsetsDays),
            scheduleUtcMillis = scheduleUtcMillis,
            creatorId = creatorId,
            approvalStatus = approvalStatus,
            modificationType = modificationType,
            currentVolunteers = currentVolunteers
        )
    }

    companion object {
        fun fromEvent(event: Event): EventEntity {
            return EventEntity(
                id = event.id,
                title = event.title,
                schedule = event.schedule,
                locationLabel = event.locationLabel,
                description = event.description,
                latitude = event.coordinates?.latitude,
                longitude = event.coordinates?.longitude,
                host = event.host,
                maxVolunteers = event.maxVolunteers,
                reminderOffsetsDays = event.reminderOffsetsDays.joinToString(","),
                scheduleUtcMillis = event.scheduleUtcMillis,
                creatorId = event.creatorId,
                approvalStatus = event.approvalStatus,
                modificationType = event.modificationType,
                currentVolunteers = event.currentVolunteers
            )
        }

        private fun parseReminderOffsets(offsetsStr: String): List<Int> {
            return if (offsetsStr.isBlank()) listOf(3, 1)
            else offsetsStr.split(",").mapNotNull { it.trim().toIntOrNull() }
        }
    }
}


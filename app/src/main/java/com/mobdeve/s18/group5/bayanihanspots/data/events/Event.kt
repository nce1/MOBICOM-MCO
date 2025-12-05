package com.mobdeve.s18.group5.bayanihanspots.data.events

import com.google.firebase.firestore.GeoPoint

// Represents both a readable label and optional coordinates for event locations
// so the UI can show text today and map logic can use precise points later.
data class Event(
    val id: String,
    val title: String,
    val schedule: String,
    val locationLabel: String,
    val description: String,
    val coordinates: GeoPoint? = null,
    val host: String? = null,
    val maxVolunteers: Int? = null,
    val reminderOffsetsDays: List<Int> = listOf(3, 1),
    val scheduleUtcMillis: Long? = null,

    var creatorId: String,
    var approvalStatus: String = "PENDING",
    var modificationType: String = "NEW",
    val currentVolunteers: Int = 0
)

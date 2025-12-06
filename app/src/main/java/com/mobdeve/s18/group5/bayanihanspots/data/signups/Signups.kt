package com.mobdeve.s18.group5.bayanihanspots.data.signups

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Signups(
    val signupId: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val status: String = "CONFIRMED",
    @ServerTimestamp
    val timestamp: Date? = null,
    val checkedInAt: Timestamp? = null,
    val distanceMeters: Double? = null
)
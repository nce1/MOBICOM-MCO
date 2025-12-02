package com.mobdeve.s18.group5.bayanihanspots.data.review

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Review(
    @DocumentId
    val reviewID: String = "",
    val spotID: String = "",
    val userID: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val timestamp: Date = Date()
)
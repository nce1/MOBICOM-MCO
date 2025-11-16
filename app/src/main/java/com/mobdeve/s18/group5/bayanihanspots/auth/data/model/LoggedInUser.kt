package com.mobdeve.s18.group5.bayanihanspots.auth.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val email: String,
    val displayName: String,
    val username: String,
    val imageUri: String
)
package com.mobdeve.s18.group5.bayanihanspots.auth.ui.login

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val displayName: String,
    val username: String,
    val email: String,
    val imageUri: String
)
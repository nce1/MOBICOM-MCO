package com.mobdeve.s18.group5.bayanihanspots.auth.ui.login

data class LoggedInUserView(
    val displayName: String,
    val username: String,
    val email: String,
    val imageUri: String
)
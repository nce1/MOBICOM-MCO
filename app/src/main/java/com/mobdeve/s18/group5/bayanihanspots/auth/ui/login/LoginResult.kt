package com.mobdeve.s18.group5.bayanihanspots.auth.ui.login

data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null,
    val errorString: String? = null
)
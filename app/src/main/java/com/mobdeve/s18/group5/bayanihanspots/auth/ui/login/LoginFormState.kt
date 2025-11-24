package com.mobdeve.s18.group5.bayanihanspots.auth.ui.login

data class LoginFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false,
    val usernameTouched: Boolean = false,
    val passwordTouched: Boolean = false
)
package com.mobdeve.s18.group5.bayanihanspots.auth.ui.signup

data class SignUpFormState(
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val confirmPasswordError: Int? = null,
    val firstnameError: Int? = null,
    val lastnameError: Int? = null,
    val usernameError: Int? = null,
    val isDataValid: Boolean = false
)
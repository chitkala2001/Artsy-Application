package com.example.artistsearchapp.model

data class RegisterState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null
)
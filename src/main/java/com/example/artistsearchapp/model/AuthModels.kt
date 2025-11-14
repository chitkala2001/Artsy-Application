package com.example.artistsearchapp.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val profile: UserProfile
)

data class RegisterResponse(
    val message: String,
    val profile: UserProfile
)

data class UserProfile(
    val name: String,
    val email: String,
    val profileImageUrl: String
)

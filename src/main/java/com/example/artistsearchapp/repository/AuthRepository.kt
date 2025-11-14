package com.example.artistsearchapp.repository

import com.example.artistsearchapp.model.LoginRequest
import com.example.artistsearchapp.model.LoginResponse
import com.example.artistsearchapp.model.RegisterRequest
import com.example.artistsearchapp.model.RegisterResponse
import com.example.artistsearchapp.network.ApiService
import retrofit2.Response

class AuthRepository(private val api: ApiService) {

    suspend fun loginUser(request: LoginRequest): Response<LoginResponse> {
        return api.login(request)
    }

    suspend fun registerUser(request: RegisterRequest): Response<RegisterResponse> {
        return api.register(request)
    }

    suspend fun logoutUser(): Response<Void> {
        return api.logout()
    }

    suspend fun deleteUser(): Response<Void> {
        return api.deleteAccount()
    }

}

package com.example.artistsearchapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.artistsearchapp.model.UserProfile
import com.example.artistsearchapp.network.ApiService
import kotlinx.coroutines.launch

class SessionViewModel(
    private val api: ApiService,
    application: Application
) : AndroidViewModel(application) {

    private val _user = mutableStateOf<UserProfile?>(null)
    val user: State<UserProfile?> = _user

    private val _checkingSession = mutableStateOf(true)
    val checkingSession: State<Boolean> = _checkingSession

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            try {
                val response = api.getCurrentUser()
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    _user.value = profile
                    saveToPrefs(profile)
                }
            } catch (e: Exception) {
                // Not logged in or error, treat as logged out
            } finally {
                _checkingSession.value = false
            }
        }
    }

    private fun saveToPrefs(profile: UserProfile) {
        val prefs = getApplication<Application>()
            .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("user_name", profile.name)
            .putString("user_email", profile.email)
            .putString("avatar", profile.profileImageUrl)
            .apply()
    }
}

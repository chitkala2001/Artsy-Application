package com.example.artistsearchapp.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.artistsearchapp.model.LoginRequest
import com.example.artistsearchapp.model.LoginUiState
import com.example.artistsearchapp.network.ApiService
import kotlinx.coroutines.launch

class LoginViewModel(
    private val api: ApiService,
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    var uiState = mutableStateOf(LoginUiState())
        private set

    fun onEmailChanged(newEmail: String) {
        uiState.value = uiState.value.copy(email = newEmail, emailError = null, generalError = null)
    }

    fun onPasswordChanged(newPassword: String) {
        uiState.value = uiState.value.copy(password = newPassword, passwordError = null, generalError = null)
    }

    fun onEmailFieldFocused() {
        if (uiState.value.email.isBlank()) {
            uiState.value = uiState.value.copy(emailError = "Email cannot be empty")
        }
    }

    fun onPasswordFieldFocused() {
        if (uiState.value.password.isBlank()) {
            uiState.value = uiState.value.copy(passwordError = "Password cannot be empty")
        }
    }


    fun onLoginClicked(onSuccess: (name: String, email: String) -> Unit) {
        val email = uiState.value.email.trim()
        val password = uiState.value.password.trim()

        var hasError = false
        if (email.isEmpty()) {
            uiState.value = uiState.value.copy(emailError = "Email cannot be empty")
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uiState.value = uiState.value.copy(emailError = "Invalid email format")
            hasError = true
        }

        if (password.isEmpty()) {
            uiState.value = uiState.value.copy(passwordError = "Password cannot be empty")
            hasError = true
        }

        if (hasError) return

        uiState.value = uiState.value.copy(isLoading = true, generalError = null)

        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(email, password))

                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!.profile
                    Log.d("AVATAR", "Received avatar: ${profile.profileImageUrl}")

                    val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("user_name", profile.name)
                        .putString("user_email", profile.email)
                        .putString("avatar", profile.profileImageUrl)
                        .apply()

                    val savedAvatar = prefs.getString("avatar", "")
                    Log.d("AVATAR", "Saved avatar in prefs: $savedAvatar")

                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        successMessage = "Logged in successfully"
                    )

                    onSuccess(profile.name, profile.email)

                } else {
                    // ✅ Always show the same message (no need for error code)
                    uiState.value = uiState.value.copy(
                        isLoading = false,
                        generalError = "Username or password is incorrect"
                    )
                }

            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    generalError = "Login error: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun clearSuccessMessage() {
        uiState.value = uiState.value.copy(successMessage = null)
    }
}

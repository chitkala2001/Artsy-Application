package com.example.artistsearchapp.viewmodel

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artistsearchapp.model.RegisterState
import com.example.artistsearchapp.model.RegisterRequest
import com.example.artistsearchapp.repository.AuthRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class RegisterViewModel(private val repository: AuthRepository ) : ViewModel() {
    private val _uiState = mutableStateOf(RegisterState())
    val uiState: State<RegisterState> = _uiState

    fun onFullNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name, fullNameError = null, generalError = null)
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = null, generalError = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, passwordError = null, generalError = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun onFullNameFieldFocused() {
        if (_uiState.value.fullName.isBlank()) {
            _uiState.value = _uiState.value.copy(fullNameError = "Full name cannot be empty")
        }
    }

    fun onEmailFieldFocused() {
        if (_uiState.value.email.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email cannot be empty")
        }
    }

    fun onPasswordFieldFocused() {
        if (_uiState.value.password.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Password cannot be empty")
        }
    }


    fun onRegisterClicked(context: Context, onSuccess: (String) -> Unit) {
        val state = _uiState.value
        var hasError = false

        var fullNameError: String? = null
        var emailError: String? = null
        var passwordError: String? = null

        if (state.fullName.isBlank()) {
            fullNameError = "Full name cannot be empty"
            hasError = true
        }

        if (state.email.isBlank()) {
            emailError = "Email cannot be empty"
            hasError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            emailError = "Invalid email format"
            hasError = true
        }

        if (state.password.isBlank()) {
            passwordError = "Password cannot be empty"
            hasError = true
        }

        _uiState.value = state.copy(
            fullNameError = fullNameError,
            emailError = emailError,
            passwordError = passwordError,
            generalError = null
        )

        if (hasError) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val response = repository.registerUser(
                    RegisterRequest(
                        name = state.fullName,
                        email = state.email,
                        password = state.password
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!.profile

                    // ✅ Save avatar and user info in SharedPreferences
                    val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("user_name", profile.name)
                        .putString("user_email", profile.email)
                        .putString("avatar", profile.profileImageUrl)
                        .apply()

                    // ✅ Log to verify
                    Log.d("AVATAR", "Saved avatar after register: ${profile.profileImageUrl}")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Registered successfully"
                    )

                    onSuccess(profile.name)
                }

                else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    if ("Email is already registered" in errorBody) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            emailError = "Email already exists"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            generalError = "Registration failed"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = e.message ?: "Unexpected error occurred"
                )
            }
        }
    }

}

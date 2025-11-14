package com.example.artistsearchapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.artistsearchapp.network.ApiService

class LoginViewModelFactory(
    private val api: ApiService,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(api, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

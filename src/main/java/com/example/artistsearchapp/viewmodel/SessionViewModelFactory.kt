package com.example.artistsearchapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.artistsearchapp.network.ApiService

class SessionViewModelFactory(
    private val api: ApiService,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            return SessionViewModel(api, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
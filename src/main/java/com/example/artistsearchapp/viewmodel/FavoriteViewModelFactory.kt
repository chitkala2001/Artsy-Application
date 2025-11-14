package com.example.artistsearchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.artistsearchapp.network.ApiService

class FavoriteViewModelFactory(
    private val api: ApiService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoriteViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

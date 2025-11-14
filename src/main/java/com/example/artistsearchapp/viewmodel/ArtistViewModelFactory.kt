package com.example.artistsearchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.artistsearchapp.network.ApiService

class ArtistDetailViewModelFactory(
    private val api: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtistDetailViewModel::class.java)) {
            return ArtistDetailViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
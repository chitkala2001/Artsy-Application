package com.example.artistsearchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.artistsearchapp.network.ApiService
import com.example.artistsearchapp.ui.viewmodel.HomeViewModel

class HomeViewModelFactory(
    private val api: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

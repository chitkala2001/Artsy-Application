package com.example.artistsearchapp.viewmodel

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artistsearchapp.model.ArtistResult
import com.example.artistsearchapp.network.ApiService
import com.example.artistsearchapp.network.RetrofitInstance
import kotlinx.coroutines.launch

class SearchViewModel(
    private val api: ApiService
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    var searchResults = mutableStateListOf<ArtistResult>()
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun updateSearchQuery(newQuery: String) {
        searchQuery = newQuery
        if (newQuery.length >= 3) {
            performSearch(newQuery)
        } else {
            searchResults.clear()
        }
    }


    private fun performSearch(query: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = api.searchArtists(query)
                searchResults.clear()
                searchResults.addAll(response.results)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "API error: ${e.localizedMessage}")
                searchResults.clear()
            } finally {
                isLoading = false
            }
        }
    }

    fun clearSearch() {
        searchQuery = ""
        searchResults.clear()
    }
}

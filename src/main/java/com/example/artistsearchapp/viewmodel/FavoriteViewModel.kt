package com.example.artistsearchapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artistsearchapp.model.ArtistResult
import com.example.artistsearchapp.model.FavoriteArtist
import com.example.artistsearchapp.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class FavoriteViewModel(
    private val api: ApiService
) : ViewModel() {

    private val _favoriteArtists = MutableStateFlow<List<FavoriteArtist>>(emptyList())
    val favoriteArtists: StateFlow<List<FavoriteArtist>> = _favoriteArtists.asStateFlow()

    init {
        loadFavorites()
    }

    fun isFavorite(artistId: String): Boolean {
        return _favoriteArtists.value.any { it.artistId == artistId }
    }

    fun toggleFavorite(
        artist: ArtistResult,
        add: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val success = if (add) {
                    val response = api.addToFavorites(artist)
                    response.isSuccessful.also {
                        if (!it) Log.e("FavoriteViewModel", "Add failed: ${response.code()}")
                    }
                } else {
                    val response = api.removeFromFavorites(artist.id)
                    response.isSuccessful.also {
                        if (!it) Log.e("FavoriteViewModel", "Remove failed: ${response.code()}")
                    }
                }

                if (success) {
                    loadFavorites()
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Toggle failed: ${e.localizedMessage}")
            }
        }
    }




    fun loadFavorites() {
        Log.d("FavoriteViewModel", "loadFavorites CALLED")
        viewModelScope.launch {
            try {
                val response = api.getFavorites()
                response.forEach {
                    Log.d("FAV_DEBUG", "Received: ${it.name}, addedAt=${it.addedAt}")
                }
                Log.d("FavoriteViewModel", "Favorites loaded: ${response.size}")
                _favoriteArtists.value = response
            } catch (e: Exception) {
                Log.e("FavoriteViewModel", "Failed to load favorites", e)
            }
        }
    }

}

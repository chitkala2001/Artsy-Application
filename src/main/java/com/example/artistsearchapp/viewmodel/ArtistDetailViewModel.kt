package com.example.artistsearchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artistsearchapp.model.ArtistDetail
import com.example.artistsearchapp.model.ArtistResult
import com.example.artistsearchapp.model.Artwork
import com.example.artistsearchapp.model.Category
import com.example.artistsearchapp.network.ApiService
import com.example.artistsearchapp.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArtistDetailViewModel (
    private val api: ApiService
) : ViewModel() {

    private val _artistDetail = MutableStateFlow<ArtistDetail?>(null)
    val artistDetail: StateFlow<ArtistDetail?> = _artistDetail

    private val _artworks = MutableStateFlow<List<Artwork>>(emptyList())
    val artworks: StateFlow<List<Artwork>> = _artworks

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _loadingCategories = MutableStateFlow(false)
    val loadingCategories: StateFlow<Boolean> = _loadingCategories

    private val _selectedIndex = MutableStateFlow(0)
    val selectedIndex: StateFlow<Int> = _selectedIndex

    private val _currentArtworkId = MutableStateFlow<String?>(null)
    val currentArtworkId: StateFlow<String?> = _currentArtworkId

    private val _similarArtists = MutableStateFlow<List<ArtistResult>>(emptyList())
    val similarArtists: StateFlow<List<ArtistResult>> = _similarArtists

    fun loadArtistDetails(artistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val detail = api.getArtistDetails(artistId)
                val artworkResults = api.getArtworks(artistId).results
                _artistDetail.value = detail
                _artworks.value = artworkResults
            } catch (e: Exception) {
                _artworks.value = emptyList()
                _artistDetail.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSimilarArtists(artistId: String) {
        viewModelScope.launch {
            try {
                val response = api.getSimilarArtists(artistId)
                _similarArtists.value = response.results
            } catch (e: Exception) {
                _similarArtists.value = emptyList()
            }
        }
    }

    fun openCategoryDialog(artworkId: String) {
        _showDialog.value = true
        _currentArtworkId.value = artworkId
        _loadingCategories.value = true
        _selectedIndex.value = 0

        viewModelScope.launch {
            try {
                val result = api.getCategories(artworkId)
                _categories.value = result
            } catch (e: Exception) {
                _categories.value = emptyList()
            } finally {
                _loadingCategories.value = false
            }
        }
    }

    fun closeDialog() {
        _showDialog.value = false
    }

    fun nextCategory() {
        _selectedIndex.value = (_selectedIndex.value + 1) % _categories.value.size
    }

    fun prevCategory() {
        _selectedIndex.value =
            (_selectedIndex.value - 1 + _categories.value.size) % _categories.value.size
    }
}

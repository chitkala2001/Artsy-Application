package com.example.artistsearchapp.model

data class ArtistResult(
    val id: String,
    val name: String,
    val birthday: String?,
    val deathday: String? = null,
    val nationality: String?,
    val imageUrl: String?
)

package com.example.artistsearchapp.model

data class Artwork(
    val id: String,
    val title: String,
    val date: String,
    val image: String?
)

data class ArtworksResponse(
    val results: List<Artwork>
)

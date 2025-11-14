package com.example.artistsearchapp.model

import com.google.gson.annotations.SerializedName

data class FavoriteArtist(

    val artistId: String,
    val name: String,
    val nationality: String?,
    val birthday: String?,
    val imageUrl: String?,
    val addedAt: String
)
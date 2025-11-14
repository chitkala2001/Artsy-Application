package com.example.artistsearchapp.network

import android.content.Context
import com.example.artistsearchapp.model.ArtistDetail
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import com.example.artistsearchapp.model.ArtistResult
import com.example.artistsearchapp.model.ArtworksResponse
import com.example.artistsearchapp.model.Category
import com.example.artistsearchapp.model.FavoriteArtist
import com.example.artistsearchapp.model.SearchResponse
import retrofit2.http.POST
import retrofit2.http.Body
import com.example.artistsearchapp.model.RegisterRequest
import com.example.artistsearchapp.model.LoginRequest
import com.example.artistsearchapp.model.LoginResponse
import com.example.artistsearchapp.model.RegisterResponse
import com.example.artistsearchapp.model.SimilarArtistsResponse
import com.example.artistsearchapp.model.UserProfile
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.HTTP


interface ApiService {
    @GET("search/{query}")
    suspend fun searchArtists(@Path("query") query: String): SearchResponse

    @GET("artists/{id}")
    suspend fun getArtistDetails(@Path("id") artistId: String): ArtistDetail

    @GET("artworks/{artist_id}")
    suspend fun getArtworks(@Path("artist_id") artistId: String): ArtworksResponse

    @GET("genes/{artwork_id}")
    suspend fun getCategories(@Path("artwork_id") artworkId: String): List<Category>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<Void>

    @GET("me")
    suspend fun getCurrentUser(): Response<UserProfile>

    @DELETE("delete")
    suspend fun deleteAccount(): Response<Void>

    @GET("favorites")
    suspend fun getFavorites(): List<FavoriteArtist>

    @POST("favorites")
    suspend fun addToFavorites(@Body artist: ArtistResult): Response<Unit>

    @DELETE("favorites/{id}")
    suspend fun removeFromFavorites(@Path("id") artistId: String): Response<Unit>

    @GET("/artists/{id}/similar")
    suspend fun getSimilarArtists(@Path("id") artistId: String): SimilarArtistsResponse




}

object RetrofitInstance {
    fun create(context: Context): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://backend-459221.wl.r.appspot.com/")
            .client(CookieProvider.provideClient(context.applicationContext))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
package com.example.artistsearchapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.compose.rememberNavController
import com.example.artistsearchapp.Navigation.AppNavGraph
import com.example.artistsearchapp.network.RetrofitInstance // ✅ Add this import
import com.example.artistsearchapp.ui.theme.ArtistSearchAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()


        val api = RetrofitInstance.create(applicationContext)

        setContent {
            ArtistSearchAppTheme(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false
            ) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController, api=api)
            }
        }
    }
}

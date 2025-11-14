package com.example.artistsearchapp.Navigation

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.artistsearchapp.model.ArtistResult
import com.example.artistsearchapp.model.FavoriteArtist
import com.example.artistsearchapp.network.ApiService
import com.example.artistsearchapp.ui.screen.*
import com.example.artistsearchapp.ui.viewmodel.HomeViewModel
import com.example.artistsearchapp.viewmodel.*

@Composable
fun AppNavGraph(navController: NavHostController, api: ApiService) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val snackbarHostState = remember { SnackbarHostState() }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(api)
    )

    // 🔐 Session check
    val sessionViewModel: SessionViewModel = viewModel(
        factory = SessionViewModelFactory(api, application)
    )
    val user = sessionViewModel.user.value
    val checkingSession = sessionViewModel.checkingSession.value

    // ✅ Shared FavoriteViewModel
    val favoriteViewModel: FavoriteViewModel = viewModel(
        factory = FavoriteViewModelFactory(api)
    )

    if (checkingSession) {
        LoadingScreen()
    } else {
        Log.d("DEBUG", "Session check complete. User = ${user?.email}")
        if (user != null) {
            LaunchedEffect(Unit) {
                Log.d("NAV_DEBUG", "Calling loadFavorites() after session restored")
                favoriteViewModel.loadFavorites()
            }
        }

        val startDestination = if (user != null) "home2" else "home"

        NavHost(navController = navController, startDestination = startDestination) {

            composable("login") {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(api, application)
                )
                LoginScreen(navController = navController, viewModel = loginViewModel,
                    favoriteViewModel = favoriteViewModel)
            }

            composable("register") {
                val registerViewModel: RegisterViewModel = viewModel(
                    factory = RegisterViewModelFactory(api)
                )
                RegisterScreen(navController = navController, viewModel = registerViewModel,
                    favoriteViewModel = favoriteViewModel)
            }

            composable("home") {
                HomeScreen(
                    isLoggedIn = false,
                    onLoginClick = { navController.navigate("login") },
                    onSearchClick = { navController.navigate("search") },
                    onProfileClick = { navController.navigate("login") }
                )
            }

            composable("search") {
                val searchViewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(api))
                SearchScreen(
                    onBackClick = { navController.popBackStack() },
                    onArtistClick = { artist ->
                        navController.navigate("details/${artist.id}/${artist.name}")
                    },
                    viewModel = searchViewModel
                )
            }

            composable("searchUser") {
                val searchViewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(api))
                SearchScreen2(
                    onBackClick = { navController.popBackStack() },
                    onArtistClick = { artist ->
                        navController.navigate("details2/${artist.id}/${artist.name}")
                    },
                    viewModel = searchViewModel,
                    favoriteViewModel = favoriteViewModel // ✅ Shared instance
                )
            }

            composable("details/{artistId}/{artistName}") { backStackEntry ->
                val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                val artistName = backStackEntry.arguments?.getString("artistName") ?: ""

                ArtistDetailScreen(
                    artistId = artistId,
                    artistName = artistName,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    api = api
                )
            }

            composable("details2/{artistId}/{artistName}") { backStackEntry ->
                val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                val artistName = backStackEntry.arguments?.getString("artistName") ?: ""

                ArtistDetailsScreen2(
                    artistId = artistId,
                    artistName = artistName,
                    api = api,
                    onBackClick = { navController.popBackStack() },
                    onArtistClick = { newArtist ->
                        navController.navigate("details2/${newArtist.id}/${newArtist.name}")
                    },
                    favoriteViewModel = favoriteViewModel
                )
            }


            composable("home2") {
                val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val avatarUrl = prefs.getString("avatar", null)
                    ?: "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=identicon"

                Log.d("AVATAR", "Loaded avatar in NavGraph: $avatarUrl")
                LaunchedEffect(Unit) {
                    Log.d("HOME2", "Re-entered home2 screen, refreshing favorites")
                    favoriteViewModel.loadFavorites()
                }


                val favorites by favoriteViewModel.favoriteArtists.collectAsState()

                HomeScreen2(
                    isLoggedIn = true,
                    favorites = favorites,
                    avatarUrl = avatarUrl,
                    onSearchClick = { navController.navigate("searchUser") },
                    onArtistClick = { artist ->
                        navController.navigate("details2/${artist.artistId}/${artist.name}")
                    },
                    snackbarHostState = snackbarHostState,
                    navController = navController,
                    viewModel = homeViewModel
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

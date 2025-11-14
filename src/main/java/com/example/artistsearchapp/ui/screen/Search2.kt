package com.example.artistsearchapp.ui.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.artistsearchapp.R
import com.example.artistsearchapp.model.ArtistResult
import com.example.artistsearchapp.viewmodel.FavoriteViewModel
import com.example.artistsearchapp.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen2(
    onBackClick: () -> Unit,
    onArtistClick: (ArtistResult) -> Unit,
    viewModel: SearchViewModel,
    favoriteViewModel: FavoriteViewModel
) {
    val searchQuery = viewModel.searchQuery
    val searchResults = viewModel.searchResults
    val isLoading = viewModel.isLoading
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val favoriteIds by favoriteViewModel.favoriteArtists.collectAsState()
    val isReady = favoriteIds.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            modifier = Modifier.size(24.dp),
                            tint = if (isDark) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search artists...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                cursorColor = if (isDark) Color.White else Color.Black,
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                disabledTextColor = Color.Gray,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            trailingIcon = {
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Back",
                                        tint = if (isDark) Color.White else Color.Black
                                    )
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                searchResults.isEmpty() && searchQuery.length >= 3 -> {
                    Text(
                        text = "No results found",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { artist ->
                            val isFavorite = favoriteIds.any { it.artistId == artist.id }
                            ArtistCard(
                                artist = artist,
                                onClick = { onArtistClick(artist) },
                                isFavorite = isFavorite,
                                onToggleFavorite = { selectedArtist, add ->
                                    favoriteViewModel.toggleFavorite(
                                        artist = selectedArtist,
                                        add = add,
                                        onSuccess = {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (add) "Added to Favorites" else "Removed from Favorites"
                                                )
                                            }
                                        }
                                    )
                                },
                                snackbarHostState = snackbarHostState,
                                isReady = isReady
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistCard(
    artist: ArtistResult,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: (ArtistResult, Boolean) -> Unit,
    snackbarHostState: SnackbarHostState,
    isReady: Boolean
) {
    val isDark = isSystemInDarkTheme()
    val contentColor = if (isDark) Color.White else Color.Black
    val isFallback = artist.imageUrl.isNullOrBlank() || artist.imageUrl.contains("/assets/shared/missing_image.png")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isFallback) {
                Image(
                    painter = painterResource(id = R.drawable.artsy_logo),
                    contentDescription = "Artsy Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                )
            } else {
                Image(
                    painter = rememberAsyncImagePainter(artist.imageUrl),
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            IconButton(
                onClick = {
                    if (isReady) {
                        onToggleFavorite(artist, !isFavorite)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp)
                    .background(
                        color = if (isDark) Color(0xFF385D9B) else Color(0xFFACCBFF),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                    tint = Color.Black
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = artist.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = contentColor,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Go to details",
                    tint = contentColor
                )
            }
        }
    }
}

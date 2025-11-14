package com.example.artistsearchapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.artistsearchapp.model.ArtistResult
import com.example.artistsearchapp.viewmodel.SearchViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.artistsearchapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarTopApp(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
            ) {

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        modifier = Modifier.size(24.dp),
                        tint = if (isDark) Color.White else Color.Black
                    )


                Spacer(modifier = Modifier.width(0.dp))

                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            "Search artists...",
                            color = if (isDark) Color.White else Color.Black,
                            style = LocalTextStyle.current.copy(fontSize = 18.sp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = if (isDark) Color.White else Color.Black,
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black
                    ),
                    trailingIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "back to home",
                                tint = if (isDark) Color.White else Color.Black
                            )
                        }
                    }
                )
            }
        },
        navigationIcon = {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            titleContentColor = if (isDark) Color.White else Color.Black,
            navigationIconContentColor = if (isDark) Color.White else Color.Black,
            actionIconContentColor = if (isDark) Color.White else Color.Black
        )
    )
}

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onArtistClick: (ArtistResult) -> Unit,
    viewModel: SearchViewModel,

) {
    val searchQuery = viewModel.searchQuery
    val searchResults = viewModel.searchResults
    val isLoading = viewModel.isLoading

    Scaffold(
        topBar = {
            SearchBarTopApp(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onClearClick = viewModel::clearSearch,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                searchResults.isEmpty() && searchQuery.length >= 3 -> {
                    Text(
                        "No results found",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults) { artist ->
                            ArtistCard(artist = artist, onClick = { onArtistClick(artist) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistCard(artist: ArtistResult, onClick: () -> Unit) {
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
                        .padding(0.dp)
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


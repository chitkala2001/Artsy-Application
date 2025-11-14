package com.example.artistsearchapp.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.artistsearchapp.R
import com.example.artistsearchapp.model.ArtistDetail
import com.example.artistsearchapp.model.ArtistResult
import com.example.artistsearchapp.model.Artwork
import com.example.artistsearchapp.model.Category
import com.example.artistsearchapp.network.ApiService
import com.example.artistsearchapp.viewmodel.ArtistDetailViewModel
import com.example.artistsearchapp.viewmodel.ArtistDetailViewModelFactory
import com.example.artistsearchapp.viewmodel.FavoriteViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailsScreen2(
    artistId: String,
    artistName: String,
    api: ApiService,
    onBackClick: () -> Unit,
    onArtistClick: (ArtistResult) -> Unit,
    favoriteViewModel: FavoriteViewModel = viewModel(),
    viewModel: ArtistDetailViewModel = viewModel(factory = ArtistDetailViewModelFactory(api))
) {
    val tabTitles = listOf("Details", "Artworks", "Similar")
    val tabIcons = listOf(
        Icons.Outlined.Info,            // for Details
        Icons.Outlined.AccountBox,      // for Artworks
        Icons.Outlined.PersonSearch     // for Similar Artists
    )
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val favorites by favoriteViewModel.favoriteArtists.collectAsState()
    val isFavorite = favorites.any { it.artistId == artistId }
    val isDark = isSystemInDarkTheme()
    val topBarBgColor = if (isDark) Color(0xFF2A3D66) else Color(0xFFACCBFF)
    val topBarContentColor = if (isDark) Color.White else Color.Black
    val tabColor = if (isDark) Color(0xFFACCBFF) else Color(0xFF003366)

    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(artistId) {
        viewModel.loadArtistDetails(artistId)

        viewModel.loadSimilarArtists(artistId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(artistName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = topBarContentColor)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val detail = viewModel.artistDetail.value
                        if (detail != null) {
                            val artistResult = ArtistResult(
                                id = artistId,
                                name = detail.name ?: "Unknown",
                                imageUrl = "",
                                birthday = detail.birthday,
                                nationality = detail.nationality
                            )
                            favoriteViewModel.toggleFavorite(artistResult, add = !isFavorite){
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = if (isFavorite) "Removed from favorites" else "Added to favorites",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }

                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isDark) Color.White else Color.Black
                        )

                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarBgColor,
                    titleContentColor = topBarContentColor,
                    navigationIconContentColor = topBarContentColor
                )
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab, contentColor = tabColor) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(tabIcons[index], contentDescription = title, tint = tabColor)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(title, color = tabColor, fontWeight = FontWeight.SemiBold)
                            }
                        },
                        selectedContentColor = tabColor,
                        unselectedContentColor = tabColor
                    )
                }
            }

            when (selectedTab) {
                0 -> DetailsTabLoggedIn(viewModel)
                1 -> ArtworksTabLoggedIn(viewModel)
                2 -> {

                    val favoriteArtistResults = favorites.map {
                        ArtistResult(
                            id = it.artistId,
                            name = it.name,
                            imageUrl = it.imageUrl ?: "",
                            birthday = it.birthday,
                            nationality = it.nationality
                        )
                    }

                    SimilarArtistsTab(
                        artists = viewModel.similarArtists.collectAsState().value,
                        favorites = favoriteArtistResults,
                        favoriteViewModel = favoriteViewModel,
                        onArtistClick = onArtistClick,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

@Composable
fun DetailsTabLoggedIn(viewModel: ArtistDetailViewModel) {
    val artistDetail by viewModel.artistDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val textColor = if (isSystemInDarkTheme()) Color.White else Color.Black

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading...", fontSize = 16.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            artistDetail?.let { detail ->
                item {
                    // Name (bold & centered)
                    detail.name?.let {
                        Text(
                            text = it,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center,
                            color = textColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Nationality + Birth–Death with en dash
                    val nationality = detail.nationality?.trim().orEmpty()
                    val birthday = detail.birthday?.trim().orEmpty()
                    val deathday = detail.deathday?.trim().orEmpty()
                    val lifeLine = buildString {
                        if (nationality.isNotBlank()) append(nationality)
                        if (birthday.isNotBlank() || deathday.isNotBlank()) {
                            if (nationality.isNotBlank()) append(", ")
                            append(birthday)
                            if (deathday.isNotBlank()) append(" – $deathday")
                        }
                    }

                    if (lifeLine.isNotBlank()) {
                        Text(
                            text = lifeLine,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 16.dp)
                        )
                    }

                    // Biography
                    detail.biography?.let { bio ->
                        val cleanedBio = fixHyphenation2(bio)
                        val paragraphs = cleanedBio.trim().split(Regex("\\n\\n+"))
                        paragraphs.forEach { paragraph ->
                            Text(
                                text = paragraph.trim(),
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Start,
                                color = textColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            } ?: item {
                Text("No details available.", color = textColor)
            }
        }
    }
}

@Composable
fun ArtworksTabLoggedIn(viewModel: ArtistDetailViewModel) {
    val artworks by viewModel.artworks.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()
    val loadingCategories by viewModel.loadingCategories.collectAsState()

    val isDark = isSystemInDarkTheme()
    val cardBgColor = if (isDark) Color(0xFF2C2C2C) else Color.White
    val cardTextColor = if (isDark) Color.White else Color.Black
    val buttonBgColor = if (isDark) Color(0xFFACCBFF) else Color(0xFF385D9B)
    val buttonTextColor = if (isDark) Color.Black else Color.White
    val dialogTextColor = if (isDark) Color.White else Color.Black
    val dialogBgColor = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF4F4F8)
    val closeButtonBg = if (isDark) Color(0xFFACCBFF) else Color(0xFF385D9B)
    val closeButtonTextColor = if (isDark) Color.Black else Color.White
    val iconColor = if (isDark) Color.White else Color.Black
    val alertDialogContainerColor = if (isDark) Color(0xFF2A2A2A) else Color.White

    if (artworks.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFACCBFF),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("No Artworks", fontSize = 16.sp, color = Color.Black)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(artworks) { artwork ->
                Card(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 250.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = artwork.image,
                            contentDescription = artwork.title,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${artwork.title}, ${artwork.date}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = cardTextColor,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    artwork.id?.let { viewModel.openCategoryDialog(it) }
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = buttonBgColor)
                            ) {
                                Text("View categories", color = buttonTextColor)
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                confirmButton = {
                    Button(
                        onClick = { viewModel.closeDialog() },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = closeButtonBg)
                    ) {
                        Text("Close", color = closeButtonTextColor)
                    }
                },
                title = {
                    Text("Categories", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = dialogTextColor)
                },
                text = {
                    if (loadingCategories) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Loading...")
                            }
                        }
                    } else if (categories.isNotEmpty()) {
                        val category = categories[selectedIndex]

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(420.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(280.dp)
                                    .height(400.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(containerColor = dialogBgColor)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    category.image?.let { url ->
                                        val context = LocalContext.current
                                        val painter = rememberAsyncImagePainter(
                                            model = ImageRequest.Builder(context)
                                                .data(url)
                                                .crossfade(true)
                                                .build()
                                        )
                                        Image(
                                            painter = painter,
                                            contentDescription = category.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = category.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center,
                                        color = dialogTextColor
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .background(dialogBgColor)
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        val scroll = rememberScrollState()
                                        Column(modifier = Modifier.verticalScroll(scroll)) {
                                            Text(
                                                text = category.description ?: "No description available.",
                                                fontSize = 14.sp,
                                                color = dialogTextColor
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.prevCategory() },
                                modifier = Modifier.align(Alignment.CenterStart).offset(x = (-35).dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = iconColor)
                            }

                            IconButton(
                                onClick = { viewModel.nextCategory() },
                                modifier = Modifier.align(Alignment.CenterEnd).offset(x = (35).dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = iconColor)
                            }
                        }
                    } else {
                        Text(
                            text = "No categories available",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                containerColor = alertDialogContainerColor
            )
        }
    }
}

@Composable
fun ArtistCard2(
    artist: ArtistResult,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: (ArtistResult, Boolean) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val isDark = isSystemInDarkTheme()
    val contentColor = if (isDark) Color.White else Color.Black
    val scope = rememberCoroutineScope()

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


            IconButton(
                onClick = {

                    onToggleFavorite(artist, !isFavorite)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp)
                    .background(color = if (isSystemInDarkTheme()) Color(0xFF385D9B) else Color(0xFFACCBFF), shape = CircleShape)

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

@Composable
fun SimilarArtistsTab(
    artists: List<ArtistResult>,
    favorites: List<ArtistResult>,
    favoriteViewModel: FavoriteViewModel,
    onArtistClick: (ArtistResult) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(artists) { artist ->
            val isFavorite = favorites.any { it.id == artist.id }

            ArtistCard2(
                artist = artist,
                onClick = { onArtistClick(artist) },
                isFavorite = isFavorite,
                onToggleFavorite = { a, add ->
                    favoriteViewModel.toggleFavorite(a, add) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (add) "Added to favorites" else "Removed from favorites",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                snackbarHostState = snackbarHostState
            )
        }
    }
}
fun fixHyphenation2(text: String): String {
    val hyphenFixRegex = Regex("\\b(\\w+)-\\s+(\\w+)\\b")
    return hyphenFixRegex.replace(text) { matchResult ->
        "${matchResult.groupValues[1]}${matchResult.groupValues[2]}"
    }
}
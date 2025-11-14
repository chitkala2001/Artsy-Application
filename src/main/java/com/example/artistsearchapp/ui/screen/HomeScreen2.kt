package com.example.artistsearchapp.ui.screen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import coil.compose.AsyncImage
import com.example.artistsearchapp.model.FavoriteArtist
import com.example.artistsearchapp.ui.theme.LightBlueHeader
import com.example.artistsearchapp.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.TimeZone
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen2(
    isLoggedIn: Boolean,
    favorites: List<FavoriteArtist>,
    avatarUrl: String,
    onSearchClick: () -> Unit,
    onArtistClick: (FavoriteArtist) -> Unit,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {

    Log.d("AVATAR", "avatarUrl = $avatarUrl")
    val context = LocalContext.current
    val currentDate = remember {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    var menuExpanded by remember { mutableStateOf(false) }

    val logoutEvent by viewModel.logoutEvent.collectAsState()
    val deleteEvent by viewModel.deleteEvent.collectAsState()

    LaunchedEffect(logoutEvent) {
        if (logoutEvent) {
            snackbarHostState.showSnackbar("Logged out successfully")
            navController.navigate("home") {
                popUpTo("home2") { inclusive = true }
            }
            viewModel.clearEvents()
        }
    }

    LaunchedEffect(deleteEvent) {
        if (deleteEvent) {
            snackbarHostState.showSnackbar("Deleted user successfully")
            navController.navigate("home") {
                popUpTo("home2") { inclusive = true }
            }
            viewModel.clearEvents()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artist Search", fontWeight = FontWeight.Normal) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary),
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search2")
                    }
                    if (isLoggedIn) {
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(onClick = { menuExpanded = true }) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "User Avatar",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(MaterialTheme.shapes.small)
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Log Out",
                                        color = Color(0xFF003366),
                                        fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.logout(context)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete Account",
                                        color = Color.Red,
                                        fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.deleteAccount(context)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = currentDate,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = 14.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(Color(0xFFF0F0F0))
                ) {
                    Text(
                        text = "Favorites",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (favorites.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text("No favorites", fontSize = 16.sp, color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Powered by Artsy",
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.artsy.net/"))
                                    context.startActivity(urlIntent)
                                },
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        items(favorites) { artist ->
                            FavoriteArtistListItem(artist = artist) {
                                onArtistClick(artist)
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(30.dp))

                            Text(
                                text = "Powered by Artsy",
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.artsy.net/"))
                                        context.startActivity(urlIntent)
                                    },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteArtistListItem(artist: FavoriteArtist, onClick: () -> Unit) {
    Log.d("FAV_ITEM", "Name=${artist.name}, Nationality='${artist.nationality}', Birthday='${artist.birthday}'")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = artist.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            val nationality = artist.nationality?.trim().orEmpty()
            val birthday = artist.birthday?.trim().orEmpty()

            val subtitle = when {
                nationality.isNotBlank() && birthday.isNotBlank() -> "$nationality, $birthday"
                nationality.isNotBlank() -> nationality
                birthday.isNotBlank() -> birthday
                else -> ","
            }

            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }



        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = getLiveTimeAgoText(artist.addedAt),
                fontSize = 12.sp,
                color = Color.Gray
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Go",
                tint = Color.Gray
            )
        }

    }
}

@Composable
fun getLiveTimeAgoText(addedAt: String): String {
    val sdf = remember {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC") // interpret input as UTC
        }
    }

    val addedMillis = remember(addedAt) {
        try {
            sdf.parse(addedAt)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val diff = currentTime - addedMillis
    val seconds = (diff / 1000) % 60
    val minutes = (diff / (1000 * 60)) % 60
    val hours = (diff / (1000 * 60 * 60)) % 24
    val days = (diff / (1000 * 60 * 60 * 24))

    return when {
        diff < 0 -> "just now"
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        else -> "$seconds second${if (seconds != 1L) "s" else ""} ago"
    }
}

package com.example.artistsearchapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.artistsearchapp.R
import com.example.artistsearchapp.ui.theme.LightBlueHeader
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentDate = remember {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    }
    val isDark = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artist Search", fontWeight = FontWeight.Normal) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary),
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(painter = painterResource(id = R.drawable.ic_account_outline),
                            contentDescription = "Profile",
                            tint = if (isDark) Color.White else Color.Black)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(12.dp)) {

            Text(
                text = " $currentDate",
                modifier = Modifier.padding(bottom = 8.dp),
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding( vertical = 12.dp)
                    .background(Color(0xFFF0F0F0))

            ) {
                Text(
                    text = "Favorites",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = onLoginClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )) {
                        Text("Log in to see favorites")
                    }
                }
            }


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


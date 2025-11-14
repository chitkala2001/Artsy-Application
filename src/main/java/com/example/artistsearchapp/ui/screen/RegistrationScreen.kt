package com.example.artistsearchapp.ui.screen

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.artistsearchapp.utils.UserSessionManager
import com.example.artistsearchapp.viewmodel.RegisterViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.foundation.layout.imePadding
import com.example.artistsearchapp.viewmodel.FavoriteViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    favoriteViewModel: FavoriteViewModel,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::onFullNameChanged,
                    label = { Text("Enter full name") },
                    isError = uiState.fullNameError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusEvent { event ->
                            if (event.isFocused) {
                                viewModel.onFullNameFieldFocused()
                            }
                        }
                )
                uiState.fullNameError?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text("Enter email") },
                    isError = uiState.emailError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusEvent { event ->
                            if (event.isFocused) {
                                viewModel.onEmailFieldFocused()
                            }
                        }
                )
                uiState.emailError?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Password") },
                    isError = uiState.passwordError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusEvent { event ->
                            if (event.isFocused) {
                                viewModel.onPasswordFieldFocused()
                            }
                        },
                    visualTransformation = PasswordVisualTransformation()
                )
                uiState.passwordError?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.onRegisterClicked(context) { name ->
                            UserSessionManager.loginUser(context, name, uiState.email)
                            scope.launch {
                                delay(500) // ⏳ give time for cookie jar to store and attach
                                favoriteViewModel.loadFavorites()
                            }
                            scope.launch {
                                snackbarHostState.showSnackbar("Registered successfully")
                            }
                            navController.navigate("home2") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Register")
                    }
                }

                uiState.generalError?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isDark = isSystemInDarkTheme()
                val bodyTextColor = if (isDark) Color.White else Color.Black

                val annotatedText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = bodyTextColor)) {
                        append("Already have an account? ")
                    }

                    pushStringAnnotation(tag = "LOGIN", annotation = "login")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append("Login")
                    }
                    pop()
                }

                ClickableText(
                    text = annotatedText,
                    onClick = { offset ->
                        annotatedText.getStringAnnotations("LOGIN", offset, offset)
                            .firstOrNull()?.let {
                                navController.navigate("login")
                            }
                    }
                )
            }
        }
    }
}

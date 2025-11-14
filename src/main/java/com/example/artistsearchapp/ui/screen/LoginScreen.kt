package com.example.artistsearchapp.ui.screen

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.artistsearchapp.ui.theme.LightBlueHeader
import com.example.artistsearchapp.utils.UserSessionManager
import com.example.artistsearchapp.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.foundation.layout.imePadding
import com.example.artistsearchapp.viewmodel.FavoriteViewModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController,
                favoriteViewModel: FavoriteViewModel,
                viewModel: LoginViewModel = viewModel()) {
    val uiState = viewModel.uiState.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Show snackbar on login success
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
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
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text("Email") },
                    isError = uiState.emailError != null,
                    modifier = Modifier.fillMaxWidth()
                        .onFocusEvent { event ->
                            if (event.isFocused) {
                                viewModel.onEmailFieldFocused()
                            }
                        }
                )
                uiState.emailError?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        it,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Password") },
                    isError = uiState.passwordError != null,
                    modifier = Modifier.fillMaxWidth()
                        .onFocusEvent { event ->
                            if (event.isFocused) {
                                viewModel.onPasswordFieldFocused()
                            }
                        },
                    visualTransformation = PasswordVisualTransformation()
                )
                uiState.passwordError?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        it,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.onLoginClicked { name, email ->
                            UserSessionManager.loginUser(context, name, email)
                            scope.launch {
                                delay(500) // ⏳ give time for cookie jar to store and attach
                                favoriteViewModel.loadFavorites()
                            }
                            scope.launch { snackbarHostState.showSnackbar("Logged in successfully") }
                            navController.navigate("home2") {
                                popUpTo("login") { inclusive = true }
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
                        Text("Login")
                    }
                }

                uiState.generalError?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        it,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                val isDark = isSystemInDarkTheme()
                val bodyTextColor = if (isDark) Color.White else Color.Black

                val annotatedText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = bodyTextColor)) {
                        append("Don't have an account yet? ")
                    }

                    pushStringAnnotation(tag = "REGISTER", annotation = "register")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold

                        )
                    ) {
                        append("Register")
                    }
                    pop()
                }


                ClickableText(
                    text = annotatedText,
                    onClick = { offset ->
                        annotatedText.getStringAnnotations(
                            tag = "REGISTER", start = offset, end = offset
                        ).firstOrNull()?.let {
                            navController.navigate("register")
                        }
                    }
                )
            }
        }
    }
}

package com.example.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(authViewModel: AuthViewModel) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Google Sign-In launcher
    val gso = remember {
        val webClientId = try {
            context.getString(context.resources.getIdentifier("default_web_client_id", "string", context.packageName))
        } catch (_: Exception) { "" }
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .apply { if (webClientId.isNotEmpty()) requestIdToken(webClientId) }
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { token ->
                    authViewModel.loginWithGoogle(token)
                }
            } catch (e: ApiException) {
                authViewModel.clearError()
            }
        }
    }

    // Show error as Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Error) {
            snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
            authViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IndustrialPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                // Logo / App name
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(IndustrialTertiaryFixed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Finder Parts",
                        tint = IndustrialTertiaryFixed,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "FINDER PARTS",
                    color = IndustrialTertiaryFixed,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isLoginMode) "Acesse sua conta" else "Crie sua conta",
                    color = IndustrialTertiaryFixed.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Name field (only for registration)
                AnimatedVisibility(visible = !isLoginMode) {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nome completo") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndustrialTertiaryFixed,
                                unfocusedBorderColor = IndustrialTertiaryFixed.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = IndustrialTertiaryFixed,
                                unfocusedLabelColor = IndustrialTertiaryFixed.copy(alpha = 0.6f),
                                cursorColor = IndustrialTertiaryFixed,
                                focusedLeadingIconColor = IndustrialTertiaryFixed,
                                unfocusedLeadingIconColor = IndustrialTertiaryFixed.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndustrialTertiaryFixed,
                        unfocusedBorderColor = IndustrialTertiaryFixed.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = IndustrialTertiaryFixed,
                        unfocusedLabelColor = IndustrialTertiaryFixed.copy(alpha = 0.6f),
                        cursorColor = IndustrialTertiaryFixed,
                        focusedLeadingIconColor = IndustrialTertiaryFixed,
                        unfocusedLeadingIconColor = IndustrialTertiaryFixed.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password",
                                tint = IndustrialTertiaryFixed.copy(alpha = 0.6f)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndustrialTertiaryFixed,
                        unfocusedBorderColor = IndustrialTertiaryFixed.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = IndustrialTertiaryFixed,
                        unfocusedLabelColor = IndustrialTertiaryFixed.copy(alpha = 0.6f),
                        cursorColor = IndustrialTertiaryFixed,
                        focusedLeadingIconColor = IndustrialTertiaryFixed,
                        unfocusedLeadingIconColor = IndustrialTertiaryFixed.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Login / Register button
                Button(
                    onClick = {
                        if (isLoginMode) {
                            authViewModel.loginWithEmail(email, password)
                        } else {
                            authViewModel.registerWithEmail(email, password, name)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = uiState !is AuthUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialTertiaryFixed),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            color = IndustrialOnPrimaryFixed,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isLoginMode) "ENTRAR" else "CRIAR CONTA",
                            color = IndustrialOnPrimaryFixed,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Divider "OU"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = IndustrialTertiaryFixed.copy(alpha = 0.2f)
                    )
                    Text(
                        text = "  OU  ",
                        color = IndustrialTertiaryFixed.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = IndustrialTertiaryFixed.copy(alpha = 0.2f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Google Sign-In button
                OutlinedButton(
                    onClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        launcher.launch(signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = uiState !is AuthUiState.Loading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = IndustrialTertiaryFixed
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(IndustrialTertiaryFixed.copy(alpha = 0.5f), IndustrialTertiaryFixed.copy(alpha = 0.3f))
                        )
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = IndustrialTertiaryFixed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "CONTINUAR COM GOOGLE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Toggle login/register mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isLoginMode) "Não tem conta? " else "Já tem conta? ",
                        color = IndustrialTertiaryFixed.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = if (isLoginMode) "Criar conta" else "Fazer login",
                        color = IndustrialTertiaryFixed,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable {
                            isLoginMode = !isLoginMode
                            authViewModel.clearError()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Footer
                Text(
                    text = "SAGACITAS SaaS",
                    color = IndustrialTertiaryFixed.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

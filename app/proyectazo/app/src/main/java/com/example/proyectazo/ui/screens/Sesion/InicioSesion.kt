package com.example.proyectazo.ui.screens.Sesion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.R
import com.example.proyectazo.ui.viewmodel.Sesion.LoginUiState
import com.example.proyectazo.ui.viewmodel.Sesion.LoginViewModel
import com.example.proyectazo.ui.viewmodel.Sesion.LoginViewModelFactory

/**
 * Login screen — the app entry point for returning users.
 * Credentials are held in local state and only sent to the ViewModel on button tap.
 * Navigates automatically once the ViewModel reports a successful login.
 */
@Composable
fun PantallaIncioSesion(
    onLoginExitoso: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(context)
    )
    // collectAsStateWithLifecycle stops collecting when the screen is in the background
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Credentials are local UI state — not stored in the ViewModel to avoid leaking them
    var nombre   by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Navigate and reset state once login succeeds — reset prevents re-triggering on recomposition
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginExitoso()
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)  // Primary color fills the background behind the card
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)  // Card takes 90% of screen width, centered
                .wrapContentHeight()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logosinfondo),
                    contentDescription = "Logo SmartFit",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Header row — welcome text on the left, register link on the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // AnnotatedString applies a different color to "SmartFit" within the same Text
                    Text(
                        text = buildAnnotatedString {
                            append("Bienvenido a\n")
                            withStyle(SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )) { append("SmartFit") }
                        },
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text("¿Sin cuenta?", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = onRegisterClick,
                            contentPadding = PaddingValues(0.dp)) {
                            Text("Regístrate", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Iniciar sesión", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)

                Spacer(modifier = Modifier.height(20.dp))

                Text("Correo electrónico", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Contraseña", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    // PasswordVisualTransformation replaces characters with dots — password never shown in plain text
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary),
                    trailingIcon = {
                        // Clear button lets the user reset the password field without backspacing
                        IconButton(onClick = { password = "" }) {
                            Icon(imageVector = Icons.Default.Close,
                                contentDescription = "Borrar contraseña",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )

                // Error message shown inline — intentionally vague to match the API (401 doesn't reveal which field is wrong)
                if (uiState is LoginUiState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (uiState as LoginUiState.Error).mensaje,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Button disabled during the API call to prevent duplicate login requests
                Button(
                    onClick = { viewModel.login(nombre, password) },
                    enabled = uiState !is LoginUiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState is LoginUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
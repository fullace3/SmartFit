package com.example.proyectazo.ui.screens.Sesion

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
import com.example.proyectazo.ui.viewmodel.Sesion.RegisterUiState
import com.example.proyectazo.ui.viewmodel.Sesion.RegisterViewModel
import com.example.proyectazo.ui.viewmodel.Sesion.RegisterViewModelFactory

/**
 * Registration screen for new users.
 * All four fields are validated locally before the register button becomes enabled —
 * the API is only called when the form is complete and the passwords match.
 * Navigates back to Login automatically on successful registration.
 */
@Composable
fun PantallaRegistro(
    onRegistroExitoso: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory()
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // All form fields held as local state — not stored in the ViewModel to avoid leaking credentials
    var nombre          by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Navigate to Login and reset state once registration succeeds
    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            onRegistroExitoso()
            viewModel.resetState()  // Prevents re-triggering if the screen recomposes
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 60.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row — app name on the left, login link on the right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                                append("Bienvenido a ")
                            }
                            withStyle(SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )) { append("SmartFit") }
                        },
                        fontSize = 14.sp
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text("¿Ya tienes cuenta?", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = onLoginClick, contentPadding = PaddingValues(0.dp)) {
                            Text("Inicia sesión", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Regístrate", fontSize = 32.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)

                Spacer(modifier = Modifier.height(24.dp))

                Text("Nombre de usuario", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = nombre, onValueChange = { nombre = it },
                    placeholder = { Text("Nombre de usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), singleLine = true)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Correo electrónico", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = email, onValueChange = { email = it },
                    placeholder = { Text("correo@ejemplo.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), singleLine = true,
                    // Email keyboard shows @ and .com shortcuts on most devices
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))

                Spacer(modifier = Modifier.height(16.dp))

                Text("Contraseña", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = password, onValueChange = { password = it },
                    placeholder = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),  // Characters replaced with dots
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { password = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    })

                Spacer(modifier = Modifier.height(16.dp))

                Text("Repite tu contraseña", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    // isError turns the border red in real time as the user types the second password
                    isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                    trailingIcon = {
                        IconButton(onClick = { confirmPassword = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    })

                // Inline mismatch warning — shown as soon as the second field has content and differs
                if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Las contraseñas no coinciden", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth())
                }

                // API-level error (e.g. email already registered) shown below the fields
                if (uiState is RegisterUiState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = (uiState as RegisterUiState.Error).mensaje,
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Button only enabled when all fields are filled and passwords match —
                // client-side validation prevents unnecessary API calls
                Button(
                    onClick = { viewModel.registrar(nombre, email, password) },
                    enabled = nombre.isNotEmpty()
                            && email.isNotEmpty()
                            && password.isNotEmpty()
                            && password == confirmPassword       // Final guard before enabling
                            && uiState !is RegisterUiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState is RegisterUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
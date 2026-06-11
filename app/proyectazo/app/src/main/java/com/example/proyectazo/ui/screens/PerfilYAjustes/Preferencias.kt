package com.example.proyectazo.ui.screens.PerfilYAjustes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.viewmodel.PerfilYAjustes.PreferenciasViewModel

/**
 * Settings screen with notification preferences, legal links and account deletion.
 * The current workout time is read directly from SharedPreferences to display
 * as a subtitle without making an API call.
 */
@Composable
fun PreferenciasScreen(
    onBack: () -> Unit,
    onTerminos: () -> Unit,
    onHoraEntrenamiento: () -> Unit,
    onEliminarCuenta: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PreferenciasViewModel = viewModel(
        factory = PreferenciasViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Read the saved workout time to display as a subtitle under the notification row
    val prefs = remember {
        context.getSharedPreferences("smartfit_config", android.content.Context.MODE_PRIVATE)
    }
    val horaGuardada = prefs.getString("hora_entrenamiento", "20:00") ?: "20:00"

    // Controls the account deletion confirmation dialog
    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Eliminar cuenta") },
            // Warning text makes clear the action is permanent and irreversible
            text = { Text("¿Estás seguro? Esta acción no se puede deshacer y perderás todos tus datos.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    viewModel.eliminarCuenta(onEliminarCuenta) // Deletes user data then navigates to Login
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartFitTopBar(titulo = "Preferencias", onBack = onBack)

        // Modifier.weight(1f) makes this column take all available space,
        // pushing the delete button and version label to the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Sección Notificaciones
            Text(
                "Notificaciones",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Surface(onClick = onHoraEntrenamiento, color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hora del entrenamiento", fontSize = 15.sp)
                            // Shows the currently saved time so the user knows what is set
                            Text(
                                horaGuardada,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))


            Text(
                "Legal",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Surface(onClick = onTerminos, color = MaterialTheme.colorScheme.surfaceContainer) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Description,
                            null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Text("Términos y condiciones", fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Error message from the ViewModel — only visible if account deletion fails
            uiState.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Delete account button — disabled while the request is in progress
        // Uses error color to signal this is a destructive and irreversible action
        Button(
            onClick = { mostrarDialogo = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onError, strokeWidth = 2.dp)
            } else {
                Text("Eliminar cuenta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        // App version displayed at the bottom for support reference
        Text(
            "SmartFit v1.0.0 - 2026",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}
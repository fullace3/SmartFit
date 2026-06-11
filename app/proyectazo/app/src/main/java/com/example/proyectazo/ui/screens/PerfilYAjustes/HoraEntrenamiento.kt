package com.example.proyectazo.ui.screens.PerfilYAjustes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectazo.notificaciones.NotificationScheduler
import com.example.proyectazo.ui.components.SmartFitTopBar

/**
 * Lets the user pick their daily workout time using a 24-hour clock.
 * Saving the time persists it to SharedPreferences and reschedules
 * the WorkManager notification for 30 minutes before the new time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoraEntrenamientoScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    // Read from smartfit_config — separate from smartfit_session so it survives logout
    val prefs = remember {
        context.getSharedPreferences("smartfit_config", android.content.Context.MODE_PRIVATE)
    }

    // Load the previously saved time, falling back to 20:00 if none exists
    val horaGuardada = prefs.getString("hora_entrenamiento", "20:00") ?: "20:00"
    val partes = horaGuardada.split(":")
    val horaInicial = partes.getOrNull(0)?.toIntOrNull() ?: 20
    val minutoInicial = partes.getOrNull(1)?.toIntOrNull() ?: 0

    // Initialize the time picker with the saved values so the user sees their current setting
    val timePickerState = rememberTimePickerState(
        initialHour = horaInicial,
        initialMinute = minutoInicial,
        is24Hour = true
    )

    // Tracks whether the user has saved in this session — shows a confirmation message
    var guardado by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { SmartFitTopBar(titulo = "Hora del entrenamiento", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                "Hora de entrenamiento",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            TimeInput(state = timePickerState)

            Spacer(Modifier.height(8.dp))

            Text(
                "Recibirás una notificación 30 minutos antes",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Confirmation message — only visible after the user saves
            if (guardado) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "✓ Guardado — notificación programada",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Pushes the button to the bottom of the screen
            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    // Format as "HH:MM" with leading zeros (e.g. "08:05" not "8:5")
                    val horaStr = "%02d:%02d".format(
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    // Persist the time so it survives app restarts and logout
                    prefs.edit()
                        .putString("hora_entrenamiento", horaStr)
                        .apply()

                    // Cancel any existing reminder and schedule a new one with the updated time
                    NotificationScheduler.programar(context, horaStr)

                    guardado = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Guardar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
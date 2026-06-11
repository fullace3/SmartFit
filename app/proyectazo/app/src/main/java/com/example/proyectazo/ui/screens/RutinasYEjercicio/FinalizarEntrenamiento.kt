package com.example.proyectazo.ui.screens.RutinasYEjercicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.FinalizarEntrenamientoViewModel
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.GuardarUiState

/**
 * Summary screen shown after the user finishes a workout.
 * Calculates session stats from the in-memory ResultadoEntrenamiento object
 * and lets the user save or discard the session.
 */
@Composable
fun FinalizarEntrenamientoScreen(
    resultado: ResultadoEntrenamiento,  // Built in EntrenarScreen and passed via NavGraph memory
    onGuardado: () -> Unit,
    onEliminar: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: FinalizarEntrenamientoViewModel = viewModel(
        factory = FinalizarEntrenamientoViewModel.Factory(context)
    )
    val state by viewModel.state.collectAsState()

    // Navigate to Home once the API confirms the workout was saved successfully
    LaunchedEffect(state) {
        if (state is GuardarUiState.Guardado) onGuardado()
    }

    val tiempoMin = resultado.tiempoSegundos / 60

    // Volume = sum of (weight × reps) for every completed set across all exercises
    val volumenTotal = resultado.ejercicios.sumOf { ej ->
        ej.series.filter { it.completada }.sumOf { s ->
            val p = s.peso.toFloatOrNull() ?: 0f
            val r = s.reps.toIntOrNull() ?: 0
            (p * r).toDouble()
        }
    }

    // An exercise counts as completed if at least one of its sets was marked done
    val ejerciciosCompletados = resultado.ejercicios.count { ej ->
        ej.series.any { it.completada }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Text("Entrenamiento terminado",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary)

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.tertiary, thickness = 1.dp)

        Spacer(Modifier.height(8.dp))

        Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null,
            modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(32.dp))

        // Three summary stats — each reuses the same StatRow composable
        StatRow(Icons.Default.Timer,       "Tiempo total:",   "$tiempoMin min")
        Spacer(Modifier.height(12.dp))
        StatRow(Icons.Default.FitnessCenter, "Volumen total:", "${"%.1f".format(volumenTotal)} kg")
        Spacer(Modifier.height(12.dp))
        StatRow(Icons.Default.CheckCircle, "Ejercicios:",     "$ejerciciosCompletados completados")

        Spacer(Modifier.height(40.dp))

        // Error message shown inline if the save API call fails
        if (state is GuardarUiState.Error) {
            Text((state as GuardarUiState.Error).msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp))
        }

        // Save button disabled while the request is in progress to prevent duplicate submissions
        Button(
            onClick = { viewModel.guardarEntrenamiento(resultado) },
            enabled = state !is GuardarUiState.Guardando,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (state is GuardarUiState.Guardando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Guardar entrenamiento", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        // TextButton instead of Button — lower visual weight signals this is a secondary action
        TextButton(onClick = onEliminar) {
            Text("Eliminar entrenamiento", color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium)
        }
    }
}

// Stat card with icon, label and value — reused for time, volume and exercise count
@Composable
private fun StatRow(icon: ImageVector, label: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        Text(valor, fontWeight = FontWeight.Bold, fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary)
    }
}
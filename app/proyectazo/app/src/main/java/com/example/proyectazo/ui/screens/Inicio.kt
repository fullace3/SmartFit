package com.example.proyectazo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.ui.viewmodel.DietaDelDia
import com.example.proyectazo.ui.viewmodel.EntrenoDelDia
import com.example.proyectazo.ui.viewmodel.InicioViewModel
import java.time.Instant
import java.time.ZoneId

/**
 * Home screen — the first screen the user sees after logging in.
 * Shows a date picker to browse days, the workout logged for the selected day,
 * and the active diet with its progress bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(usuario: String) {
    val context = LocalContext.current
    val viewModel: InicioViewModel = viewModel(
        factory = InicioViewModel.Factory(context)
    )

    val diaSeleccionado by viewModel.diaSeleccionado.collectAsStateWithLifecycle()
    val entrenoDelDia   by viewModel.entrenoDelDia.collectAsStateWithLifecycle()
    val dietaDelDia     by viewModel.dietaDelDia.collectAsStateWithLifecycle()

    // Initialize the date picker to today so the user sees today's data by default
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    // Convert the picker's epoch millis to a LocalDate and pass it to the ViewModel
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val nuevoDia = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            viewModel.seleccionarDia(nuevoDia)
        }
    }

    // LazyColumn used so the calendar and cards scroll together as one unit
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Bienvenido, $usuario",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        // Inline date picker — showModeToggle and headline hidden for a cleaner look
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,  // Hides the calendar/input toggle to keep the UI simple
                    title = null,
                    headline = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { TarjetaEntreno(dia = diaSeleccionado.dayOfMonth, estado = entrenoDelDia) }
        item { TarjetaDieta(estado = dietaDelDia) }
    }
}

/**
 * Card showing the workout logged for the selected day.
 * Uses a sealed state (Cargando/SinEntreno/ConEntreno/Error) to render
 * the appropriate content for each loading phase.
 */
@Composable
private fun TarjetaEntreno(dia: Int, estado: EntrenoDelDia) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            Text("Entreno del día $dia", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))

            when (estado) {
                is EntrenoDelDia.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                        strokeWidth = 2.dp)
                }
                is EntrenoDelDia.SinEntreno -> {
                    Text("Sin entreno registrado", fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is EntrenoDelDia.ConEntreno -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = estado.rutinaNombre ?: "Entrenamiento completado",
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // distinctBy avoids counting the same exercise multiple times if logged in different sets
                    val numEjercicios = estado.ejercicios.distinctBy { it.nombre_ejercicio }.size
                    val horas   = estado.duracionEstimadaMin / 60
                    val minutos = estado.duracionEstimadaMin % 60
                    val durText = buildString {
                        if (horas > 0) append("${horas}h ")
                        if (minutos > 0) append("${minutos}min")
                    }.trim()

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Exercise count chip
                        Surface(shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.FitnessCenter, null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("$numEjercicios ejercicios", fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                        // Duration chip — only shown when duration is available
                        if (durText.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Timer, null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Text(durText, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }
                }
                is EntrenoDelDia.Error -> {
                    Text(estado.mensaje, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/**
 * Card showing the active diet and its calorie progress bar.
 * Progress is read from SharedPreferences (smartfit_session) where DietaActivaScreen writes it —
 * this avoids an extra API call from the Home screen.
 */
@Composable
private fun TarjetaDieta(estado: DietaDelDia) {
    val context = LocalContext.current
    // Read the progress value written by DietaActivaScreen — no API call needed
    val prefs = remember { context.getSharedPreferences("smartfit_session", android.content.Context.MODE_PRIVATE) }
    val dietaProgreso = prefs.getFloat("dieta_progreso", 0f)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            when (estado) {
                is DietaDelDia.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                        strokeWidth = 2.dp)
                }
                is DietaDelDia.SinDieta -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restaurant, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Sin dieta activa", fontSize = 15.sp, fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                is DietaDelDia.ConDieta -> {
                    val dieta = estado.dieta
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restaurant, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(dieta.nombre, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("${dieta.objetivo_calorico} kcal objetivo", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    // Progress bar shows calories consumed vs target — value comes from SharedPreferences
                    // (always 0 until the TODO in DietaActivaScreen is implemented)
                    LinearProgressIndicator(
                        progress = { dietaProgreso },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
                is DietaDelDia.Error -> {
                    Text(estado.mensaje, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
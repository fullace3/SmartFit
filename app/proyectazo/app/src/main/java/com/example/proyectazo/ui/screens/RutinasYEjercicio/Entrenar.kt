package com.example.proyectazo.ui.screens.RutinasYEjercicio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.EntrenarViewModel
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.RutinaConEjercicios
import kotlinx.coroutines.delay

/**
 * Live workout screen — the most complex screen in the app.
 * Manages exercise navigation, per-exercise series tracking, rest timer and session duration.
 * All state is held in memory; it is collected into ResultadoEntrenamiento when the user finishes.
 */

// Data classes that represent workout state — defined here because they are only used in this flow

/** A single set within an exercise, tracking weight, reps, completion and the previous session's value. */
data class SerieEntrenamiento(
    val numero: Int,
    val previa: String = "-",   // Previous session's result — shown as reference, e.g. "80 x 10"
    val peso: String = "",
    val reps: String = "",
    val completada: Boolean = false
)

/** All sets logged for one exercise during the current session. */
data class EjercicioResultado(
    val id: Int,
    val nombre: String,
    val series: List<SerieEntrenamiento>
)

/** The complete workout result passed to FinalizarEntrenamientoScreen after the session ends. */
data class ResultadoEntrenamiento(
    val rutinaNombre: String,
    val rutinaId: Int,
    val tiempoSegundos: Int,
    val ejercicios: List<EjercicioResultado>
)

@Composable
fun EntrenarScreen(
    rutinaConEjercicios: RutinaConEjercicios,
    onTerminar: (ResultadoEntrenamiento) -> Unit
) {
    val context = LocalContext.current
    val viewModel: EntrenarViewModel = viewModel(
        factory = EntrenarViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    val ejercicios = rutinaConEjercicios.ejercicios
    var ejercicioActualIndex by remember { mutableStateOf(0) }
    val ejercicioActual = ejercicios.getOrNull(ejercicioActualIndex)

    // One mutableStateList per exercise — each list starts with a single empty set
    val seriesPorEjercicio = remember {
        List(ejercicios.size) { mutableStateListOf(SerieEntrenamiento(1)) }
    }
    val seriesActuales = seriesPorEjercicio[ejercicioActualIndex]

    // Load previous session results for all exercises in one call to avoid N separate requests
    LaunchedEffect(Unit) {
        viewModel.cargarPrevias(ejercicios.map { it.id_ejercicio })
    }

    // Once previous results arrive, populate the first set of each exercise as a reference
    LaunchedEffect(uiState.previasPorEjercicio) {
        if (uiState.previasPorEjercicio.isNotEmpty()) {
            ejercicios.forEachIndexed { i, ej ->
                val previa = uiState.previasPorEjercicio[ej.id_ejercicio]
                val previaText = if (previa != null && previa.peso != "-")
                    "${previa.peso} x ${previa.reps}" else "-"
                // Only update if the user hasn't started entering data yet
                if (seriesPorEjercicio[i].size == 1 && seriesPorEjercicio[i][0].peso.isEmpty()) {
                    seriesPorEjercicio[i][0] = seriesPorEjercicio[i][0].copy(previa = previaText)
                }
            }
        }
    }

    // Session duration counter — increments every second in the background
    var tiempoEntrenamiento by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) { delay(1000); tiempoEntrenamiento++ }
    }

    // Rest timer — starts automatically when a set is marked as completed
    var timerActivo  by remember { mutableStateOf(false) }
    var timerPausado by remember { mutableStateOf(false) }
    var segundos     by remember { mutableStateOf(90) }  // Default rest: 90 seconds

    // LaunchedEffect re-runs when timerActivo or timerPausado change
    LaunchedEffect(timerActivo, timerPausado) {
        if (timerActivo && !timerPausado) {
            while (segundos > 0 && !timerPausado) { delay(1000); segundos-- }
            if (segundos == 0) timerActivo = false  // Hide the timer bar when it reaches zero
        }
    }

    var mostrarDialogoTerminar by remember { mutableStateOf(false) }

    if (mostrarDialogoTerminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoTerminar = false },
            title = { Text("¿Terminar entrenamiento?") },
            text = { Text("Se guardará el progreso de las series completadas.") },
            confirmButton = {
                Button(onClick = {
                    mostrarDialogoTerminar = false
                    // Build the final result from all in-memory series data
                    val resultado = ResultadoEntrenamiento(
                        rutinaNombre  = rutinaConEjercicios.rutina.nombre,
                        rutinaId      = rutinaConEjercicios.rutina.id_rutina,
                        tiempoSegundos = tiempoEntrenamiento,
                        ejercicios = ejercicios.mapIndexed { i, ej ->
                            EjercicioResultado(
                                id     = ej.id_ejercicio,
                                nombre = ej.nombre,
                                series = seriesPorEjercicio[i].toList()
                            )
                        }
                    )
                    onTerminar(resultado)
                }) { Text("Terminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoTerminar = false }) { Text("Cancelar") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = rutinaConEjercicios.rutina.nombre,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { mostrarDialogoTerminar = true },
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Terminar entrenamiento", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.tertiary, thickness = 1.dp)

        // Horizontal exercise navigator — tapping a thumbnail jumps to that exercise
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(ejercicios) { index, ejercicio ->
                val isActual = index == ejercicioActualIndex
                AsyncImage(
                    model = ejercicio.imagen,
                    contentDescription = ejercicio.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        // Active exercise gets a colored border to indicate current position
                        .border(
                            if (isActual) 2.dp else 0.dp,
                            if (isActual) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { ejercicioActualIndex = index }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.tertiary, thickness = 1.dp)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = ejercicioActual?.nombre ?: "",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            item {
                // ContentScale.Fit here (not Crop) — shows the full exercise image for form reference
                AsyncImage(
                    model = ejercicioActual?.imagen,
                    contentDescription = ejercicioActual?.nombre,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.height(16.dp))
            }
            item {
                // Table header — weight(1f/1.5f) distributes columns proportionally
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Spacer(Modifier.width(28.dp))
                    Text("Serie",  fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f),   textAlign = TextAlign.Center)
                    Text("Previa", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Peso",   fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                    Text("Rep.",   fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                    Spacer(Modifier.weight(1f))
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            itemsIndexed(seriesActuales) { index, serie ->
                // remember keyed on index AND ejercicioActualIndex so fields reset when switching exercises
                var peso by remember(index, ejercicioActualIndex) { mutableStateOf(serie.peso) }
                var reps by remember(index, ejercicioActualIndex) { mutableStateOf(serie.reps) }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                        .background(
                            // Completed sets get a tinted background for visual feedback
                            if (serie.completada) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete button — hidden (transparent) when only one set remains
                    IconButton(onClick = {
                        if (seriesActuales.size > 1) {
                            seriesActuales.removeAt(index)
                            seriesActuales.forEachIndexed { i, s ->
                                seriesActuales[i] = s.copy(numero = i + 1)  // Renumber after deletion
                            }
                        }
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Eliminar",
                            tint = if (seriesActuales.size > 1) MaterialTheme.colorScheme.error
                            else Color.Transparent,
                            modifier = Modifier.size(16.dp))
                    }

                    Text("${serie.numero}", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)

                    // Previous session reference — read-only, shown for comparison
                    Text(serie.previa, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)

                    OutlinedTextField(
                        value = peso,
                        onValueChange = { peso = it; seriesActuales[index] = serie.copy(peso = it) },
                        modifier = Modifier.weight(1.5f).height(56.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 13.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it; seriesActuales[index] = serie.copy(reps = it) },
                        modifier = Modifier.weight(1.5f).height(56.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 13.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(Modifier.width(4.dp))

                    // Check circle — marks the set as done and starts the rest timer automatically
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                            .background(
                                if (serie.completada) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                val completada = !serie.completada
                                seriesActuales[index] = serie.copy(completada = completada, peso = peso, reps = reps)
                                if (completada) {
                                    // Reset and start the 90-second rest timer
                                    segundos = 90
                                    timerPausado = false
                                    timerActivo  = false
                                    timerActivo  = true  // Toggle forces LaunchedEffect to re-run
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Completar",
                            tint = if (serie.completada) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { seriesActuales.add(SerieEntrenamiento(seriesActuales.size + 1)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ), border = null
                ) { Text("Añadir serie", fontWeight = FontWeight.Medium) }
            }
        }

        // Rest timer bar — only visible while a rest period is active
        if (timerActivo) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play/pause toggle
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .clickable { timerPausado = !timerPausado },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (timerPausado) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Format seconds as M:SS
                val min = segundos / 60
                val seg = segundos % 60
                Text("%d:%02d".format(min, seg),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiary)

                // +15 / -15 adjustment buttons — capped at 0 and 599 seconds (9:59)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { segundos = minOf(segundos + 15, 599) },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                    ) { Text("+15", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    Button(onClick = { segundos = maxOf(0, segundos - 15) },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                    ) { Text("-15", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
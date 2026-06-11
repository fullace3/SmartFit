package com.example.proyectazo.ui.screens.ProgresoYRegistros

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.network.SessionManager
import com.example.proyectazo.ui.viewmodel.ProgresoYRegistro.ProgresoViewModel

/**
 * Progress screen showing weight stats, training volume chart and body measurements table.
 * Reloads data every time the screen returns to the foreground — this ensures fresh data
 * after the user adds a new measurement in AñadirRegistroScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProgreso(onAñadirRegistro: () -> Unit) {
    val context = LocalContext.current
    val userId = remember { SessionManager(context).getUserId() }
    val viewModel: ProgresoViewModel = viewModel(
        factory = ProgresoViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // repeatOnLifecycle(RESUMED) re-runs the block every time the screen comes to the foreground.
    // This triggers a reload when the user returns from AñadirRegistroScreen without
    // needing savedStateHandle or manual refresh calls.
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.recargar()
        }
    }

    // rememberSaveable persists the selected chart filter across device rotation
    var filtroSeleccionado by rememberSaveable { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Thin loading bar at the top — less intrusive than a full-screen spinner
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Weight stats row — four equal cards via Modifier.weight(1f)
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PesoCard("Peso actual:", "${uiState.pesoActual} Kg", Modifier.weight(1f))
                PesoCard("Peso inicial:", "${uiState.pesoInicial} Kg", Modifier.weight(1f))
                PesoCard("Diferencia", "${uiState.diferenciaPeso} Kg", Modifier.weight(1f))
                PesoCard("IMC", "${uiState.imc}", Modifier.weight(1f))
            }
        }

        // Training volume chart card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Volumen de entrenamiento", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Custom segmented control — no ripple effect to feel like a toggle, not a button
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Semana", "Mes", "Año").forEachIndexed { i, label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (filtroSeleccionado == i) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickableNoRipple { filtroSeleccionado = i }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (filtroSeleccionado == i) FontWeight.Bold else FontWeight.Normal,
                                color = if (filtroSeleccionado == i) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Switch the dataset based on the selected time filter
                val datos = when (filtroSeleccionado) {
                    0 -> uiState.volumenSemana
                    1 -> uiState.volumenMes
                    else -> uiState.volumenAnio
                }

                if (datos.isEmpty() || datos.all { it.second == 0.0 }) {
                    Box(
                        Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sin datos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    GraficaBarras(datos = datos, filtro = filtroSeleccionado)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Body measurements table — shows the 6 most recent entries
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Medidas corporales", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                // Table header — each column takes equal space via weight(1f)
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Fecha", "Brazo", "Pecho", "Pierna", "Altura").forEach { col ->
                        Text(
                            col, fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                if (uiState.medidas.isEmpty()) {
                    Text(
                        "Sin medidas registradas",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Limited to 6 rows to keep the card compact — not a scrollable table
                    uiState.medidas.take(6).forEach { medida ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            // Reformat ISO date "2025-08-17" → "17/08" for compact display
                            val fecha = medida.fecha.take(10).let {
                                val parts = it.split("-")
                                if (parts.size == 3) "${parts[2]}/${parts[1]}" else it
                            }
                            Text(fecha, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("${medida.brazo_cm ?: "-"}", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("${medida.pecho_cm ?: "-"}", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("${medida.pierna_cm ?: "-"}", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text(
                                medida.altura_cm?.let { "${it.toInt()}" } ?: "-",
                                fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onAñadirRegistro,
                    modifier = Modifier.align(Alignment.CenterHorizontally).height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Añadir registro")
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// Small stat card used in the weight row — label above value, centered
@Composable
private fun PesoCard(label: String, valor: String, modifier: Modifier) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(valor, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

/**
 * Custom bar chart drawn with Compose layout primitives (no external chart library).
 * Bar heights are proportional to the maximum value in the dataset.
 * A minimum fraction of 0.02f ensures zero-value bars remain visible as a thin line.
 */
@Composable
private fun GraficaBarras(datos: List<Pair<String, Double>>, filtro: Int) {
    val maxValor = datos.maxOfOrNull { it.second } ?: 1.0
    val alturaGrafica = 160.dp

    Column {
        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(alturaGrafica),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            datos.forEach { (_, valor) ->
                val fraccion = if (maxValor > 0) (valor / maxValor).toFloat() else 0f
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight(fraccion.coerceAtLeast(0.02f)) // Never fully invisible
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        HorizontalDivider()
        Spacer(Modifier.height(4.dp))

        // X-axis labels aligned below each bar using matching weight(1f)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            datos.forEach { (label, _) ->
                Text(
                    label, fontSize = 10.sp, modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Custom clickable modifier that suppresses the ripple effect —
// used on the segmented control tabs where a ripple would look out of place
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.then(
        Modifier.clickable(indication = null, interactionSource = interactionSource, onClick = onClick)
    )
}
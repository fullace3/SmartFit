package com.example.proyectazo.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyectazo.R
import com.example.proyectazo.network.EjercicioResponse
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.AñadirEjercicioUiState

/**
 * Searchable exercise list with muscle group and equipment filters.
 * Filters open a bottom sheet panel — tapping outside or selecting a value closes it.
 * panelVisible state uses rememberSaveable to survive device rotation.
 */
enum class FiltroTipo { MUSCULO, EQUIPAMIENTO }

// Maps muscle group names to their local drawable resources for the filter grid
private val musculoImagenes: Map<String, Int> = mapOf(
    "Abdominales"   to R.drawable.img_003_ms_bajo,
    "Trapecio"      to R.drawable.img_003_atrs_1,
    "Bíceps"        to R.drawable.img_002_bceps,
    "Espalda"       to R.drawable.img_002_dorsal,
    "Cuádriceps"    to R.drawable.img_002_frente,
    "Isquiosurales" to R.drawable.img_002_atrs,
    "Gemelos"       to R.drawable.img_001_msculos,
    "Glúteos"       to R.drawable.img_001_gluteo,
    "Hombros"       to R.drawable.img_003_hombro,
    "Antebrazo"     to R.drawable.img_003_culturismo,
    "Pecho"         to R.drawable.img_001_gimnasia,
    "Tríceps"       to R.drawable.img_001_triceps,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaEjerciciosScreen(
    uiState: AñadirEjercicioUiState,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFiltroTipoChange: (FiltroTipo) -> Unit,
    onFiltroValorChange: (String?) -> Unit,
    onEjercicioClick: (EjercicioResponse) -> Unit
) {
    // rememberSaveable keeps the panel open across rotation
    var panelVisible by rememberSaveable { mutableStateOf(false) }

    // Close the filter panel automatically once a filter value has been selected
    LaunchedEffect(uiState.filtroValor) {
        if (uiState.filtroValor != null) panelVisible = false
    }

    Scaffold(
        topBar = { SmartFitTopBar(titulo = "Añadir ejercicio", onBack = onBack) }
    ) { innerPadding ->
        // Box allows the filter panel to overlay the list content
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Buscar ejercicio") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                // Two filter chips — selecting one opens the bottom sheet panel
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroTipo.entries.forEach { tipo ->
                        val selected = uiState.filtroValor != null && uiState.filtroTipo == tipo
                        FilterChip(
                            selected = selected,
                            onClick = {
                                onFiltroTipoChange(tipo)
                                onFiltroValorChange(null)  // Clear previous value when switching filter type
                                panelVisible = true
                            },
                            label = {
                                Text(if (tipo == FiltroTipo.MUSCULO) "Músculo" else "Equipamiento",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            },
                            modifier = Modifier.weight(1f),  // Equal width chips via weight
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))

                // Render the correct content based on current state
                when {
                    uiState.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = uiState.error, color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                        }
                    }
                    uiState.ejerciciosFiltrados.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron ejercicios",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    else -> {
                        // Stable key prevents unnecessary recompositions when the list changes
                        LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                            items(items = uiState.ejerciciosFiltrados, key = { it.id_ejercicio }) { ejercicio ->
                                EjercicioItem(ejercicio = ejercicio, onClick = { onEjercicioClick(ejercicio) })
                                HorizontalDivider(modifier = Modifier.padding(start = 80.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // Bottom sheet filter panel — overlays the list with a semi-transparent scrim
            if (panelVisible) {
                // Semi-transparent scrim — tapping it closes the panel
                Box(modifier = Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { panelVisible = false })

                // Panel slides up from the bottom covering 75% of the screen height
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(top = 8.dp)
                ) {
                    // Drag handle — visual cue that the panel can be dismissed
                    Box(modifier = Modifier.width(40.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .align(Alignment.CenterHorizontally))

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = if (uiState.filtroTipo == FiltroTipo.MUSCULO) "Grupo muscular" else "Equipamiento",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(horizontal = 16.dp))

                    Spacer(Modifier.height(8.dp))

                    // "Clear filter" button — only shown when a filter is already active
                    if (uiState.filtroValor != null) {
                        TextButton(onClick = { onFiltroValorChange(null); panelVisible = false },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            Text("✕  Quitar filtro", color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium)
                        }
                    }

                    if (uiState.filtroTipo == FiltroTipo.MUSCULO) {
                        // 3-column grid of muscle groups with local drawable images
                        LazyVerticalGrid(columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()) {
                            items(uiState.filtrosDisponibles) { grupo ->
                                GrupoMusculoItem(
                                    nombre = grupo,
                                    selected = uiState.filtroValor == grupo,
                                    onClick = {
                                        // Tapping the active filter clears it; tapping another selects it
                                        onFiltroValorChange(if (uiState.filtroValor == grupo) null else grupo)
                                        panelVisible = false
                                    })
                            }
                        }
                    } else {
                        // Equipment grid — uses the first exercise image for that equipment as a preview
                        LazyVerticalGrid(columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()) {
                            items(uiState.filtrosDisponibles) { equip ->
                                val imagen = uiState.ejercicios
                                    .firstOrNull { it.equipamiento == equip }?.imagen
                                GrupoEquipamientoItem(
                                    nombre = equip,
                                    imagenUrl = imagen,
                                    selected = uiState.filtroValor == equip,
                                    onClick = {
                                        onFiltroValorChange(if (uiState.filtroValor == equip) null else equip)
                                        panelVisible = false
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}

// Muscle group grid item — uses a local drawable image mapped by name
@Composable
private fun GrupoMusculoItem(nombre: String, selected: Boolean, onClick: () -> Unit) {
    val drawable = musculoImagenes[nombre]
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (drawable != null) {
            // ContentScale.Fit — shows the full muscle diagram without cropping
            Image(painter = painterResource(id = drawable), contentDescription = nombre,
                modifier = Modifier.size(64.dp).padding(4.dp), contentScale = ContentScale.Fit)
        } else {
            // Fallback for muscle groups without a mapped drawable
            Box(modifier = Modifier.size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center) {
                Text(nombre.take(1), style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = nombre,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center, maxLines = 1)
    }
}

// Equipment grid item — uses a network image from an exercise that uses that equipment
@Composable
private fun GrupoEquipamientoItem(nombre: String, imagenUrl: String?, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center) {
            if (imagenUrl != null) {
                AsyncImage(model = imagenUrl, contentDescription = nombre,
                    contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            } else {
                Text(nombre.take(1), style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = nombre,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center, maxLines = 1)
    }
}

// Single exercise row — thumbnail, name and muscle/equipment subtitle
@Composable
private fun EjercicioItem(ejercicio: EjercicioResponse, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center) {
            if (ejercicio.imagen != null) {
                AsyncImage(model = ejercicio.imagen, contentDescription = ejercicio.nombre,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                // Fallback initial letter when no image is available
                Text(ejercicio.nombre.take(1).uppercase(), style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = ejercicio.nombre,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface)
            // Combines muscle group and equipment as subtitle, falls back to default sets/reps
            val subtitulo = listOfNotNull(ejercicio.grupo_muscular, ejercicio.equipamiento)
                .joinToString(" · ").ifBlank { "3 series x 10 repeticiones" }
            Text(text = subtitulo, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
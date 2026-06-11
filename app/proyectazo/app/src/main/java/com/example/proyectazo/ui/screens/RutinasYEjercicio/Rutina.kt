package com.example.proyectazo.ui.screens.RutinasYEjercicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.RutinaConEjercicios
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.RutinasUiState
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.RutinasViewModel

// Maximum number of exercise thumbnails shown in the routine card preview
private const val MAX_PREVIEW_IMAGES = 4

/**
 * Lists all workout routines for the current user.
 * Uses a sealed state (Cargando/Error/Vacio/Exito) to render the correct UI
 * for each loading phase without nested if/else chains.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRutinas(
    onCrearRutina: () -> Unit,
    onVerDetalles: (RutinaConEjercicios) -> Unit = {},
    onEliminarRutina: ((rutinaId: Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel: RutinasViewModel = viewModel(
        factory = RutinasViewModel.Factory(context)
    )
    // collectAsStateWithLifecycle stops collecting when the screen is in the background,
    // saving resources compared to the standard collectAsState()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargarRutinas()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Todas las rutinas", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrearRutina,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear nueva rutina")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is RutinasUiState.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RutinasUiState.Error -> {
                    // Error state includes a retry button — the user is not stuck
                    Column(modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(state.mensaje, color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
                        OutlinedButton(onClick = { viewModel.cargarRutinas() }) { Text("Reintentar") }
                    }
                }
                is RutinasUiState.Vacio -> {
                    // Empty state guides the user to create their first routine
                    Column(modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Text("No tienes rutinas todavía", fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Pulsa + para crear tu primera rutina", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
                is RutinasUiState.Exito -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Stable key prevents unnecessary recompositions when the list updates
                        items(items = state.rutinas, key = { it.rutina.id_rutina }) { rutinaConEj ->
                            TarjetaRutina(
                                rutinaConEj = rutinaConEj,
                                onClick = { onVerDetalles(rutinaConEj) },
                                onEliminar = { viewModel.eliminarRutina(rutinaConEj.rutina.id_rutina) }
                            )
                        }
                        // Extra bottom padding so the FAB does not overlap the last card
                        item { Spacer(Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }
}

/**
 * Routine card showing the name, a thumbnail preview of up to 4 exercises,
 * and a "+N" badge if there are more exercises than the preview limit.
 * Delete is behind a three-dot menu and a confirmation dialog to prevent accidents.
 */
@Composable
private fun TarjetaRutina(
    rutinaConEj: RutinaConEjercicios,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    var menuVisible         by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }

    val ejercicios  = rutinaConEj.ejercicios
    val preview     = ejercicios.take(MAX_PREVIEW_IMAGES)
    val extrasCount = ejercicios.size - preview.size  // Number of exercises beyond the preview limit

    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("Eliminar rutina") },
            text  = { Text("¿Seguro que quieres eliminar \"${rutinaConEj.rutina.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false; onEliminar() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // weight(1f) lets the name take all space, pushing the menu icon to the right
                Text(
                    text = rutinaConEj.rutina.nombre,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Three-dot menu — keeps the card clean while still allowing deletion
                Box {
                    IconButton(onClick = { menuVisible = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = menuVisible, onDismissRequest = { menuVisible = false }) {
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null,
                                tint = MaterialTheme.colorScheme.error) },
                            onClick = { menuVisible = false; mostrarDialogoEliminar = true }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                preview.forEach { ejercicio ->
                    AsyncImage(
                        model = ejercicio.imagen,
                        contentDescription = ejercicio.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                // "+N" badge shown when the routine has more exercises than the preview limit
                if (extrasCount > 0) {
                    Box(modifier = Modifier.size(68.dp).clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center) {
                        Text("+$extrasCount", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}
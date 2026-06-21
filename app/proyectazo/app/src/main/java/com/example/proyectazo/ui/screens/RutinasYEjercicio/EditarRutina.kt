package com.example.proyectazo.ui.screens.RutinasYEjercicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.proyectazo.ui.util.rememberDrawableId
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.util.rememberDrawableId
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.EjercicioRutina
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.EditarRutinaViewModel

/**
 * Screen for editing an existing routine — rename it, add or remove exercises.
 * Reloads the routine every time the screen returns to the foreground so that
 * exercises added in AñadirEjercicioScreen appear immediately without a manual refresh.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarRutinaScreen(
    rutinaId: Int,
    onNavigateBack: () -> Unit,
    onAnadirEjercicio: (rutinaId: Int) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: EditarRutinaViewModel = viewModel(
        factory = EditarRutinaViewModel.Factory(rutinaId, context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // DisposableEffect used instead of LaunchedEffect because we need to both
    // add and remove the observer — LaunchedEffect has no cleanup mechanism
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.cargarRutina()  // Reloads when returning from AñadirEjercicioScreen
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }  // Prevent memory leaks
    }

    // rememberSaveable persists the edit mode across device rotation
    var editandoNombre by rememberSaveable { mutableStateOf(false) }

    // Navigate back once the ViewModel confirms changes were saved successfully
    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) {
            viewModel.onGuardadoConsumed()  // Reset the flag to avoid re-triggering
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = { SmartFitTopBar(titulo = "Editar rutina", onBack = onNavigateBack) }
    ) { innerPadding ->
        // Early return while loading — avoids rendering an empty layout
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Inline name editor — same pattern as CrearRutinaScreen
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (editandoNombre) {
                    BasicTextField(
                        value = uiState.nombre,
                        onValueChange = viewModel::onNombreChange,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = uiState.nombre.ifEmpty { "Sin nombre" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.nombre.isEmpty())
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = { editandoNombre = !editandoNombre }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar nombre",
                        tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                }
            }

            // Inline error message from the ViewModel
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(8.dp))

            if (uiState.ejercicios.isEmpty()) {
                // Empty state centered in the remaining space via weight(1f)
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))
                    Spacer(Modifier.height(12.dp))
                    Text("Sin ejercicios", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else {
                // Exercise list takes all remaining space, buttons stay fixed at the bottom
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.ejercicios, key = { it.id }) { ejercicio ->
                        EjercicioEditItem(
                            ejercicio = ejercicio,
                            onEliminar = { viewModel.eliminarEjercicio(ejercicio.id) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onAnadirEjercicio(rutinaId) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Añadir ejercicio", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.height(12.dp))

            // Save disabled if the routine name is empty — prevents saving an unnamed routine
            Button(
                onClick = viewModel::guardarCambios,
                enabled = uiState.nombre.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Guardar cambios", style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Single exercise row in the edit list.
 * Delete is hidden behind a three-dot menu and a confirmation dialog
 * to prevent accidental removal.
 */
@Composable
private fun EjercicioEditItem(ejercicio: EjercicioRutina, onEliminar: () -> Unit) {
    var menuVisible    by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Eliminar ejercicio") },
            text = { Text("¿Quitar \"${ejercicio.nombre}\" de la rutina?") },
            confirmButton = {
                TextButton(onClick = { mostrarDialogo = false; onEliminar() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val resId = rememberDrawableId(ejercicio.imagenUrl)
        if (resId != null) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = ejercicio.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(ejercicio.nombre, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium)
            Text("${ejercicio.series} series × ${ejercicio.repeticiones} repeticiones",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        // Three-dot menu keeps the row clean — delete only appears on explicit interaction
        Box {
            IconButton(onClick = { menuVisible = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Opciones",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DropdownMenu(expanded = menuVisible, onDismissRequest = { menuVisible = false }) {
                DropdownMenuItem(
                    text = { Text("Eliminar de rutina", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    onClick = { menuVisible = false; mostrarDialogo = true }
                )
            }
        }
    }
}
package com.example.proyectazo.ui.screens.RutinasYEjercicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.EjercicioRutina
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.CrearRutinaUiState
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.RutinaViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.proyectazo.ui.util.rememberDrawableId

/**
 * Screen for creating a new workout routine.
 * The flow is: enter a name → create the routine via API → navigate to exercise picker.
 * The routine is created in the backend before adding exercises so it has an ID to link them to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearRutinaScreen(
    userId: Int,
    ejercicios: List<EjercicioRutina> = emptyList(),
    onNavigateBack: () -> Unit = {},
    onAnadirEjercicio: (rutinaId: Int) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: RutinaViewModel = viewModel(
        factory = RutinaViewModel.Factory(context = context)
    )
    val uiState by viewModel.uiState.collectAsState()
    // rememberSaveable keeps the name and edit mode across device rotation
    var nombreRutina by rememberSaveable { mutableStateOf("") }
    var editandoNombre by rememberSaveable { mutableStateOf(false) }

    // When the API returns the new routine ID, reset the state and navigate to the exercise picker
    LaunchedEffect(uiState) {
        if (uiState is CrearRutinaUiState.RutinaCreada) {
            val rutinaId = (uiState as CrearRutinaUiState.RutinaCreada).rutinaId
            viewModel.resetState()
            onAnadirEjercicio(rutinaId) // Pass the real ID so exercises can be linked to it
        }
    }

    Scaffold(
        topBar = {
            SmartFitTopBar(titulo = "Crear rutina", onBack = onNavigateBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Inline name editor — toggles between read and edit mode on the pencil icon
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()) {
                if (editandoNombre) {
                    OutlinedTextField(value = nombreRutina, onValueChange = { nombreRutina = it },
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f), singleLine = true)
                } else {
                    // Dimmed placeholder text when no name has been entered yet
                    Text(text = nombreRutina.ifEmpty { "Añade un nombre" },
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                        color = if (nombreRutina.isEmpty())
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = { editandoNombre = !editandoNombre }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar nombre",
                        tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                }
            }

            // Inline error message from the ViewModel — shown below the name field
            if (uiState is CrearRutinaUiState.Error) {
                Text(text = (uiState as CrearRutinaUiState.Error).mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(8.dp))

            // Empty state guides the user toward the next action
            if (ejercicios.isEmpty()) {
                Column(modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))
                    Spacer(Modifier.height(12.dp))
                    Text("Sin ejercicios aún", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Spacer(Modifier.height(4.dp))
                    Text(text = "Pulsa \"Añadir ejercicio\" para\nconstruir tu rutina",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center)
                }
            } else {
                // LazyColumn takes remaining space via weight(1f) — exercises scroll independently
                LazyColumn(modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(ejercicios, key = { it.id }) { ejercicio ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            val resId = rememberDrawableId(ejercicio.imagenUrl)
                            if (resId != null) {
                                Image(painter = painterResource(id = resId),
                                    contentDescription = ejercicio.nombre,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant))
                            } else {
                                Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ejercicio.nombre, style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium)
                                Text("${ejercicio.series} series × ${ejercicio.repeticiones} rep",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Creates the routine via API if not yet created, then navigates to exercise picker
            OutlinedButton(
                onClick = { viewModel.crearONavegar(nombreRutina) },
                enabled = uiState !is CrearRutinaUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                if (uiState is CrearRutinaUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Añadir ejercicio", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Save button only enabled when the routine has a name and at least one exercise
            Button(
                onClick = onNavigateBack,
                enabled = uiState is CrearRutinaUiState.RutinaCreada && nombreRutina.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Guardar entrenamiento", style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
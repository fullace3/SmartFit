package com.example.proyectazo.ui.screens.PerfilYAjustes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.viewmodel.PerfilYAjustes.EditarGuardarEstado
import com.example.proyectazo.ui.viewmodel.PerfilYAjustes.EditarPerfilViewModel

/**
 * Screen that allows the user to edit their physical data and fitness goal.
 * Account fields (username, email) are displayed as read-only — they cannot be changed here.
 * Shows a Snackbar on success or error after saving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(onBack: () -> Unit, onGuardadoExitoso: () -> Unit) {
    val context = LocalContext.current
    val viewModel: EditarPerfilViewModel = viewModel(
        factory = EditarPerfilViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val opcionesSexo = listOf("Hombre", "Mujer", "Otro", "Prefiero no decirlo")
    val opcionesObjetivo = listOf(
        "Volumen limpio", "Definición", "Pérdida de peso",
        "Mantenimiento", "Fuerza", "Resistencia"
    )

    // Reacts to state changes after the save attempt — shows feedback and navigates back on success
    LaunchedEffect(uiState.guardarEstado) {
        when (val estado = uiState.guardarEstado) {
            is EditarGuardarEstado.Exito -> {
                snackbarHostState.showSnackbar("Perfil actualizado correctamente")
                viewModel.resetEstado()
                onGuardadoExitoso() // Triggers perfilViewModel.recargar() in the NavGraph
            }
            is EditarGuardarEstado.Error -> {
                snackbarHostState.showSnackbar(estado.mensaje)
                viewModel.resetEstado()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { SmartFitTopBar(titulo = "Editar Perfil", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Avatar
            // no image upload implemented, icon used as fallback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Datos de cuenta
            // Read-only — username and email cannot be changed from this screen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Información de la cuenta",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                CampoInformativo(label = "Nombre de usuario", valor = uiState.nombre)
                CampoInformativo(label = "Correo electrónico", valor = uiState.email)
            }
            Spacer(Modifier.height(24.dp))

            // ── Datos físicos
            // Editable physical data — used for progress tracking and calorie calculations
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Datos físicos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                CampoEdicion(
                    label = "Altura (cm)",
                    valor = uiState.alturaCm,
                    placeholder = "Ej: 175",
                    keyboardType = KeyboardType.Decimal,
                    onValorChange = viewModel::onAlturaChange
                )
                CampoEdicion(
                    label = "Peso (kg)",
                    valor = uiState.pesoKg,
                    placeholder = "Ej: 70",
                    keyboardType = KeyboardType.Decimal,
                    onValorChange = viewModel::onPesoChange
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Objetivo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Adjust,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Mi objetivo:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    DropdownCampo(
                        valor = uiState.objetivo,
                        placeholder = "Seleccionar Objetivo",
                        opciones = opcionesObjetivo,
                        onSeleccion = viewModel::onObjetivoChange
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Botón guardar
            // Save button is disabled while a request is in progress to prevent duplicate submissions
            Button(
                onClick = { viewModel.guardar() },
                enabled = !uiState.isLoading &&
                        uiState.guardarEstado !is EditarGuardarEstado.Cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                // Show a spinner inside the button while saving — replaces the label
                if (uiState.guardarEstado is EditarGuardarEstado.Cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── TextField genérico
// Generic labeled text field with configurable keyboard type
@Composable
private fun CampoEdicion(
    label: String,
    valor: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValorChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
// ── Dropdown genérico
// Generic dropdown using ExposedDropdownMenuBox — readOnly prevents keyboard from opening
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownCampo(
    valor: String,
    placeholder: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit
) {
    // rememberSaveable keeps the dropdown open after device rotation
    var expandido by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true, // Selection only — keyboard must not appear
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(), // Required by M3 to anchor the dropdown
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

// Read-only field displaying account data that cannot be edited from this screen
@Composable
private fun CampoInformativo(label: String, valor: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Falls back to "No configurado" if the value is empty
        Spacer(Modifier.height(2.dp))
        Text(
            text = valor.ifEmpty { "No configurado" },
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
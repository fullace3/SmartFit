package com.example.proyectazo.ui.screens.ProgresoYRegistros

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.network.SessionManager
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.viewmodel.ProgresoYRegistro.AñadirRegistroViewModel
import com.example.proyectazo.ui.viewmodel.ProgresoYRegistro.GuardarEstado

/**
 * Screen for recording a new body measurement entry.
 * Allows the user to log weight, height and body measurements for a specific date.
 * Navigates back automatically after a successful save.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AñadirRegistroScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val userId = remember { SessionManager(context).getUserId() }
    val viewModel: AñadirRegistroViewModel = viewModel(
        factory = AñadirRegistroViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Reacts to save result — navigates back on success, shows error message on failure
    LaunchedEffect(uiState.guardarEstado) {
        when (val estado = uiState.guardarEstado) {
            is GuardarEstado.Exito -> {
                snackbarHostState.showSnackbar("Registro guardado correctamente")
                onBack()
            }
            is GuardarEstado.Error -> {
                snackbarHostState.showSnackbar(estado.mensaje)
                viewModel.resetGuardarEstado()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SmartFitTopBar(titulo = "Añadir registro", onBack = onBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Date input split into three separate fields (DD / MM / YYYY)
            // so the user controls each part independently without a date picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Introduce el día",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoDia(
                        valor = uiState.dia,
                        placeholder = "DD",
                        maxLength = 2,
                        modifier = Modifier.weight(1f), // Proportional widths via weight
                        onValorChange = viewModel::onDiaChange
                    )
                    CampoDia(
                        valor = uiState.mes,
                        placeholder = "MM",
                        maxLength = 2,
                        modifier = Modifier.weight(1f),
                        onValorChange = viewModel::onMesChange
                    )
                    CampoDia(
                        valor = uiState.anio,
                        placeholder = "YYYY",
                        maxLength = 4,
                        modifier = Modifier.weight(2f), // Year field is twice as wide as day/month
                        onValorChange = viewModel::onAnioChange
                    )
                }
                // Validation error shown inline below the date fieldss
                if (uiState.fechaError != null) {
                    Text(
                        text = uiState.fechaError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }

            // Weight slider — range 30–200 kg covers all realistic user values
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Indica tu peso actual",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Live value display updates as the slider moves
                    Text(
                        text = "${uiState.pesoKg.toInt()} kg",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Slider(
                    value = uiState.pesoKg,
                    onValueChange = viewModel::onPesoChange,
                    valueRange = 30f..200f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Optional body measurements — each field has a clear button for quick reset
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Medidas actuales",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                CampoMedida(
                    label = "Brazo",
                    valor = uiState.brazoCm,
                    onValorChange = viewModel::onBrazoChange,
                    onLimpiar = { viewModel.onBrazoChange("") }
                )
                CampoMedida(
                    label = "Cintura",
                    valor = uiState.cinturaCm,
                    onValorChange = viewModel::onCinturaChange,
                    onLimpiar = { viewModel.onCinturaChange("") }
                )
                CampoMedida(
                    label = "Pecho",
                    valor = uiState.pechoCm,
                    onValorChange = viewModel::onPechoChange,
                    onLimpiar = { viewModel.onPechoChange("") }
                )
                CampoMedida(
                    label = "Pierna",
                    valor = uiState.piernaCm,
                    onValorChange = viewModel::onPiernaChange,
                    onLimpiar = { viewModel.onPiernaChange("") }
                )
            }

            // Height slider — optional, range 100–250 cm
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Indica tu altura (opcional)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${uiState.alturaCm.toInt()} cm",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Slider(
                    value = uiState.alturaCm,
                    onValueChange = viewModel::onAlturaChange,
                    valueRange = 100f..250f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(4.dp))

            // Save button — disabled while the API call is in progress to prevent duplicate submissions
            Button(
                onClick = { viewModel.guardar(userId) },
                enabled = !uiState.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState.cargando) {
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
                    Text(
                        text = "Guardar registro",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// Date field — only accepts digits up to maxLength characters
// Validation is done inline to prevent invalid input from reaching the ViewModel
@Composable
private fun CampoDia(
    valor: String,
    placeholder: String,
    maxLength: Int,
    modifier: Modifier,
    onValorChange: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        // Filter: reject input that exceeds maxLength or contains non-digit characters
        onValueChange = { if (it.length <= maxLength && it.all { c -> c.isDigit() }) onValorChange(it) },
        placeholder = {
            Text(
                text = placeholder,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

// Measurement field — accepts up to 3 digits and 1 decimal place (e.g. "99.9")
// Clear button appears only when the field has content
@Composable
private fun CampoMedida(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit,
    onLimpiar: () -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { new ->
            // Regex allows formats like "0", "99", "99.9" — rejects anything else
            if (new.matches(Regex("^\\d{0,3}(\\.\\d{0,1})?\$"))) onValorChange(new)
        },
        label = { Text(label) },
        placeholder = { Text("Introduce el tamaño en cm") },
        trailingIcon = {
            // Only show the clear button when there is text to clear
            if (valor.isNotEmpty()) {
                IconButton(onClick = onLimpiar) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = "Limpiar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}
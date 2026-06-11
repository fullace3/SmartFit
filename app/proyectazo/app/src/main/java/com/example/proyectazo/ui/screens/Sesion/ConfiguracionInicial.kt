package com.example.proyectazo.ui.screens.Sesion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.viewmodel.Sesion.ConfiguracionInicialViewModel

/**
 * One-time setup screen shown after the first login.
 * Collects weight, height and preferred workout time before entering the app.
 * The back button is disabled in the NavGraph so the user cannot skip this step.
 * Navigates to Home automatically once the data is saved successfully.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionInicialScreen(
    onBack: () -> Unit,
    onConfigurado: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ConfiguracionInicialViewModel = viewModel(
        factory = ConfiguracionInicialViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    var mostrarTimePicker by remember { mutableStateOf(false) }

    // Initialize the time picker with the current ViewModel value
    val timePickerState = rememberTimePickerState(
        initialHour   = uiState.horaEntrenamiento.first,
        initialMinute = uiState.horaEntrenamiento.second,
        is24Hour = true
    )

    // Navigate to Home once the ViewModel confirms the data was saved
    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) onConfigurado()
    }

    // Time picker shown in a Dialog — only updates the ViewModel when the user taps OK,
    // not on every picker change, so cancelled selections are discarded
    if (mostrarTimePicker) {
        Dialog(onDismissRequest = { mostrarTimePicker = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Hora de entrenamiento", fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp))
                    TimeInput(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { mostrarTimePicker = false }) {
                            Text("Cancelar")  // Discard — picker state is not committed
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            // Commit the selected time to the ViewModel only on OK
                            viewModel.onHoraChange(timePickerState.hour, timePickerState.minute)
                            mostrarTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = { SmartFitTopBar(titulo = "Configuracion inicial", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Weight field — decimal keyboard, validated by the ViewModel
            Text("Peso en Kg", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.peso,
                onValueChange = viewModel::onPesoChange,
                placeholder = { Text("Peso") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = uiState.errorPeso != null,
                // supportingText shows the validation error inline below the field
                supportingText = uiState.errorPeso?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            Spacer(Modifier.height(16.dp))

            // Height field — numeric keyboard, validated by the ViewModel
            Text("Altura en cm", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.altura,
                onValueChange = viewModel::onAlturaChange,
                placeholder = { Text("Altura") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.errorAltura != null,
                supportingText = uiState.errorAltura?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            Spacer(Modifier.height(16.dp))

            // Workout time — read-only field, edited via the Dialog time picker
            Text("Hora de entrenamiento", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                // Format as "HH:MM" with leading zeros
                value = "%02d:%02d".format(
                    uiState.horaEntrenamiento.first,
                    uiState.horaEntrenamiento.second
                ),
                onValueChange = {},
                readOnly = true,  // Keyboard must not appear — time is set via the picker
                placeholder = { Text("Seleccionar hora") },
                trailingIcon = {
                    IconButton(onClick = { mostrarTimePicker = true }) {
                        Icon(Icons.Filled.AccessTime, contentDescription = "Seleccionar hora")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            Spacer(Modifier.height(32.dp))

            // Save button — disabled while the API call is in progress
            Button(
                onClick = { viewModel.guardar(onConfigurado) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Siguiente", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // API error shown below the button — field errors are shown inline above
            uiState.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
package com.example.proyectazo.ui.screens.DietasYComidas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.ui.viewmodel.DietaYComida.DietaViewModel

/**
 * Entry point for the diet section.
 * Acts as a router — decides which screen to show based on the current state:
 * - While loading show a spinner
 * - Active diet exists and user hasn't asked to switch to DietaActivaScreen
 * - No active diet or user wants to switch to TodasLasDietasScreen
 * This avoids duplicating navigation logic in the NavGraph.
 */
@Composable
fun DietaScreen(
    onCrearDieta: () -> Unit = {},
) {
    val context = LocalContext.current
    val viewModel: DietaViewModel = viewModel(
        factory = DietaViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState() // Collects StateFlow from ViewModel as a reactive Compose State to trigger automatic UI recomposition
    // Local state — controls whether to show the active diet or the full list
    // Does not need rememberSaveable because resetting to false on recomposition is intentional
    var mostrarTodas by remember { mutableStateOf(false) }

    // Find the active diet from the loaded list — null if none is set
    val dietaActiva = uiState.dietas.find { it.activo }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (dietaActiva != null && !mostrarTodas) {
        // Show the active diet view — user can tap "Cambiar" to see all diets
        DietaActivaScreen(
            dieta = dietaActiva,
            onNueva = onCrearDieta,
            onCambiar = { mostrarTodas = true }
        )
    } else {
        // Show all diets — back button only appears if there is already an active diet to return to
        TodasLasDietasScreen(
            dietas = uiState.dietas,
            isLoading = false,
            onCrearDieta = onCrearDieta,
            onSeleccionarDieta = { dietaId ->
                viewModel.seleccionarDieta(dietaId)
                mostrarTodas = false // Return to the active diet view after selecting
            },
            onBorrarDieta = { dietaId ->
                viewModel.borrarDieta(dietaId)
            },
            mostrarBack = dietaActiva != null,  // Hide back if there is no active diet to return to
            onBack = { mostrarTodas = false }
        )
    }
}
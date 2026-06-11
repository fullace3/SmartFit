package com.example.proyectazo.ui.screens.RutinasYEjercicio

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.network.EjercicioResponse
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.screens.ListaEjerciciosScreen
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.AñadirEjercicioViewModel

/**
 * Coordinator screen for adding an exercise to a routine.
 * Manages two sub-screens in-memory without separate routes:
 * - ListaEjerciciosScreen — browse and filter exercises
 * - DetalleEjercicioScreen — view details and confirm the selection
 *
 * Navigates back automatically once the exercise has been added to the routine.
 */
@Composable
fun AñadirEjercicioScreen(
    rutinaId: Int,
    onBack: () -> Unit
) {
    val viewModel: AñadirEjercicioViewModel = viewModel(
        factory = AñadirEjercicioViewModel.Factory(
            rutinaId = rutinaId,
            apiService = RetrofitClient.instance
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    // Holds the exercise the user tapped — null means the list is shown, non-null shows the detail
    var ejercicioDetalle by remember { mutableStateOf<EjercicioResponse?>(null) }

    // Once the ViewModel confirms the exercise was added, consume the event and go back
    LaunchedEffect(uiState.ejercicioAgregado) {
        if (uiState.ejercicioAgregado) {
            viewModel.onEjercicioAgregadoConsumed() // Reset the flag to avoid re-triggering
            onBack()
        }
    }

    if (ejercicioDetalle != null) {
        // Show the detail screen for the selected exercise
        DetalleEjercicioScreen(
            ejercicio = ejercicioDetalle!!,
            onBack = { ejercicioDetalle = null }, // Return to the list without adding
            onAnadir = { ejercicio ->
                viewModel.onEjercicioSeleccionado(ejercicio)
                ejercicioDetalle = null
            }
        )
    } else {
        // Show the full exercise list with search and filters
        ListaEjerciciosScreen(
            uiState = uiState,
            onBack = onBack,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onFiltroTipoChange  = viewModel::onFiltroTipoChange,
            onFiltroValorChange = viewModel::onFiltroValorChange,
            onEjercicioClick    = { ejercicioDetalle = it } // Tapping an exercise opens its detail
        )
    }
}
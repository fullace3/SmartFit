package com.example.proyectazo.ui.screens.RutinasYEjercicio

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.data.local.entity.EjercicioEntity
import com.example.proyectazo.screens.ListaEjerciciosScreen
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.AñadirEjercicioViewModel

@Composable
fun AñadirEjercicioScreen(
    rutinaId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AñadirEjercicioViewModel = viewModel(
        factory = AñadirEjercicioViewModel.Factory(rutinaId = rutinaId, context = context)
    )
    val uiState by viewModel.uiState.collectAsState()
    var ejercicioDetalle by remember { mutableStateOf<EjercicioEntity?>(null) }

    LaunchedEffect(uiState.ejercicioAgregado) {
        if (uiState.ejercicioAgregado) {
            viewModel.onEjercicioAgregadoConsumed()
            onBack()
        }
    }

    if (ejercicioDetalle != null) {
        DetalleEjercicioScreen(
            ejercicio = ejercicioDetalle!!,
            onBack = { ejercicioDetalle = null },
            onAnadir = { ejercicio ->
                viewModel.onEjercicioSeleccionado(ejercicio)
                ejercicioDetalle = null
            }
        )
    } else {
        ListaEjerciciosScreen(
            uiState = uiState,
            onBack = onBack,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onFiltroTipoChange  = viewModel::onFiltroTipoChange,
            onFiltroValorChange = viewModel::onFiltroValorChange,
            onEjercicioClick    = { ejercicioDetalle = it }
        )
    }
}

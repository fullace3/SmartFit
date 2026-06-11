package com.example.proyectazo.ui.viewmodel.RutinaYEjercicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.ApiService
import com.example.proyectazo.network.EjercicioResponse
import com.example.proyectazo.network.RutinaEjercicioRequest
import com.example.proyectazo.screens.FiltroTipo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI state for the exercise picker screen.
 * ejercicios holds the full catalog — ejerciciosFiltrados is the derived
 * subset after applying search and filters, computed by aplicarFiltros().
 * filtrosDisponibles updates when the filter type changes (muscle vs equipment).
 */
data class AñadirEjercicioUiState(
    val ejercicios: List<EjercicioResponse> = emptyList(),           // Full catalog from API
    val ejerciciosFiltrados: List<EjercicioResponse> = emptyList(),  // Filtered subset shown in the list
    val filtrosDisponibles: List<String> = emptyList(),              // Options shown in the filter panel
    val filtroTipo: FiltroTipo = FiltroTipo.MUSCULO,
    val filtroValor: String? = null,   // Currently selected filter value — null means no filter
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val ejercicioAgregado: Boolean = false  // One-shot event consumed by AñadirEjercicioScreen
)

/**
 * ViewModel for AñadirEjercicioScreen.
 * Loads the full exercise catalog once and applies search/filter locally —
 * no extra API calls are needed when the user changes the filter.
 * ejercicioAgregado is a one-shot event flag: set to true when an exercise
 * is added, consumed immediately by the screen via onEjercicioAgregadoConsumed().
 */
class AñadirEjercicioViewModel(
    private val rutinaId: Int,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AñadirEjercicioUiState())
    val uiState: StateFlow<AñadirEjercicioUiState> = _uiState.asStateFlow()

    init { cargarEjercicios() }

    /**
     * Fetches the full exercise catalog and builds the initial filter options.
     * Filter options are derived from the loaded list — no separate endpoint needed.
     */
    private fun cargarEjercicios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getEjercicios()
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            ejercicios = lista,
                            // Build muscle group filter options from the loaded list
                            filtrosDisponibles = lista
                                .mapNotNull { it.grupo_muscular }
                                .distinct()
                                .sorted()
                        ).aplicarFiltros()  // Apply default filters immediately
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false,
                            error = "Error ${response.code()}: No se pudieron cargar los ejercicios.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "No se pudieron cargar los ejercicios.")
                }
            }
        }
    }

    // Updates the search query and reapplies filters immediately
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query).aplicarFiltros() }
    }

    /**
     * Switches the filter type (muscle vs equipment) and rebuilds the available options.
     * Clears the selected filter value so the previous selection does not carry over.
     */
    fun onFiltroTipoChange(tipo: FiltroTipo) {
        _uiState.update { state ->
            val subFiltros = state.ejercicios
                .mapNotNull { if (tipo == FiltroTipo.MUSCULO) it.grupo_muscular else it.equipamiento }
                .distinct()
                .sorted()
            state.copy(filtroTipo = tipo, filtrosDisponibles = subFiltros, filtroValor = null)
                .aplicarFiltros()
        }
    }

    // null clears the filter — same toggle pattern used across the app
    fun onFiltroValorChange(valor: String?) {
        _uiState.update { it.copy(filtroValor = valor).aplicarFiltros() }
    }

    /**
     * Adds the selected exercise to the routine via the API.
     * orden is derived from the exercise's position in the filtered list —
     * coerceAtLeast(1) prevents orden = 0 if indexOf returns -1.
     * Sets ejercicioAgregado = true on success so the screen can navigate back.
     */
    fun onEjercicioSeleccionado(ejercicio: EjercicioResponse) {
        viewModelScope.launch {
            try {
                val orden = (_uiState.value.ejerciciosFiltrados.indexOf(ejercicio) + 1)
                    .coerceAtLeast(1)
                apiService.añadirEjercicioARutina(
                    datos = RutinaEjercicioRequest(
                        id_rutina    = rutinaId,
                        id_ejercicio = ejercicio.id_ejercicio,
                        orden        = orden
                    )
                )
                _uiState.update { it.copy(ejercicioAgregado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al agregar el ejercicio.") }
            }
        }
    }

    // Resets the one-shot event flag after the screen has reacted to it
    fun onEjercicioAgregadoConsumed() {
        _uiState.update { it.copy(ejercicioAgregado = false) }
    }

    /**
     * Extension function on the state that applies both search and filter simultaneously.
     * Called after every user interaction that could change the visible list.
     * Both conditions must be true for an exercise to appear in the results.
     */
    private fun AñadirEjercicioUiState.aplicarFiltros(): AñadirEjercicioUiState {
        val filtrados = ejercicios.filter { ej ->
            val coincideBusqueda = searchQuery.isBlank() ||
                    ej.nombre.contains(searchQuery, ignoreCase = true)
            val coincideFiltro = filtroValor == null || when (filtroTipo) {
                FiltroTipo.MUSCULO      -> ej.grupo_muscular == filtroValor
                FiltroTipo.EQUIPAMIENTO -> ej.equipamiento   == filtroValor
            }
            coincideBusqueda && coincideFiltro
        }
        return copy(ejerciciosFiltrados = filtrados)
    }

    class Factory(
        private val rutinaId: Int,
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AñadirEjercicioViewModel(rutinaId, apiService) as T
    }
}
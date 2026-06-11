package com.example.proyectazo.ui.viewmodel.DietaYComida

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lightweight food item for list display.
 * All nutritional values are per 100g, matching the database storage format.
 */
data class ComidaListItem(
    val id: Int,
    val nombre: String,
    val calorias100g: Int,
    val proteinas100g: Int,
    val carbohidratos100g: Int,
    val grasas100g: Int,
    val imagen: String? = null
)

/**
 * UI state for the food list screen.
 * comidasFiltradas is a computed property that applies search and sorting
 * without modifying the original list — the full catalog is always preserved
 * so switching filters does not require a new API call.
 */
data class ListaComidasUiState(
    val comidas: List<ComidaListItem> = emptyList(),
    val busqueda: String = "",
    val filtroActivo: String? = null,  // "Proteinas", "Carbohidratos" or "Grasas saludables"
    val isLoading: Boolean = true
) {
    /**
     * Derived list combining search and sort — recomputed on every state change.
     * Search is case-insensitive partial match.
     * Sort order depends on the active filter — no filter means original API order.
     */
    val comidasFiltradas: List<ComidaListItem>
        get() {
            // Step 1 — apply text search
            var lista = if (busqueda.isBlank()) comidas
            else comidas.filter { it.nombre.contains(busqueda, ignoreCase = true) }

            // Step 2 — sort by the active nutritional filter (descending)
            lista = when (filtroActivo) {
                "Proteinas"        -> lista.sortedByDescending { it.proteinas100g }
                "Carbohidratos"    -> lista.sortedByDescending { it.carbohidratos100g }
                "Grasas saludables"-> lista.sortedByDescending { it.grasas100g }
                else               -> lista  // No filter — preserve original order
            }
            return lista
        }
}

/**
 * ViewModel for ListaComidasScreen.
 * Loads the full food catalog once on init — filtering and sorting happen
 * locally via comidasFiltradas, so no additional API calls are needed.
 */
class ListaComidasViewModel(context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(ListaComidasUiState())
    val uiState: StateFlow<ListaComidasUiState> = _uiState

    init { cargar() }

    /**
     * Fetches the full food catalog and stores it in the state.
     * All subsequent filtering and sorting operate on this local copy.
     */
    private fun cargar() {
        viewModelScope.launch {
            try {
                val resp = api.getComidas()
                if (resp.isSuccessful) {
                    val lista = resp.body()?.map {
                        ComidaListItem(
                            id = it.id_comida,
                            nombre = it.nombre,
                            calorias100g = it.calorias_100g,
                            proteinas100g = it.proteinas_100g,
                            carbohidratos100g = it.carbohidratos_100g,
                            grasas100g = it.grasas_100g,
                            imagen = it.imagen
                        )
                    } ?: emptyList()
                    _uiState.update { it.copy(comidas = lista, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Updates the search query — comidasFiltradas recomputes automatically
    fun onBusquedaChange(v: String) = _uiState.update { it.copy(busqueda = v) }

    // Toggles the filter — tapping the active filter clears it
    fun onFiltroChange(filtro: String) {
        _uiState.update {
            it.copy(filtroActivo = if (it.filtroActivo == filtro) null else filtro)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ListaComidasViewModel(context) as T
    }
}
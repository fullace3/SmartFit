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
 * Lightweight representation of a diet plan for list display.
 * Avoids exposing the full API response model to the UI layer.
 * activo indicates whether this is the currently selected diet —
 * only one diet per user can be active at a time.
 */
data class DietaListItem(
    val id: Int,
    val nombre: String,
    val calorias: Int,
    val proteinas: Double,
    val carbohidratos: Double,
    val grasas: Double,
    val activo: Boolean
)

/**
 * UI state for the diet list screen.
 * The active diet is found by filtering dietas where activo == true —
 * DietaScreen uses this to decide which sub-screen to show.
 */
data class DietaUiState(
    val dietas: List<DietaListItem> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * ViewModel for DietaScreen — manages the full list of the user's diet plans.
 * Handles loading, activation and deletion, reloading the list after each mutation
 * to keep the UI in sync with the server.
 */
class DietaViewModel(context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(DietaUiState())
    val uiState: StateFlow<DietaUiState> = _uiState

    init { cargar() }

    /**
     * Fetches all diet plans for the current user and maps them to DietaListItem.
     * Called on init and after every mutation (activate, delete) to keep state fresh.
     */
    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val resp = api.getDietasUsuario(userId)
                if (resp.isSuccessful) {
                    val lista = resp.body()?.map {
                        DietaListItem(
                            id = it.id_dieta,
                            nombre = it.nombre,
                            calorias = it.objetivo_calorico,
                            proteinas = it.proteinas_g,
                            carbohidratos = it.carbohidratos_g,
                            grasas = it.grasas_g,
                            activo = it.activo
                        )
                    } ?: emptyList()
                    _uiState.update { it.copy(dietas = lista, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Activates a diet — the API deactivates all others automatically.
     * Reloads the list so the active flag updates across all items.
     */
    fun seleccionarDieta(dietaId: Int) {
        viewModelScope.launch {
            try {
                api.activarDieta(dietaId)
                cargar()  // Reload so activo reflects the new state for all items
            } catch (_: Exception) {}
        }
    }

    /**
     * Deletes a diet and reloads the list.
     * Only reloads on success — avoids a redundant network call if deletion fails.
     */
    fun borrarDieta(dietaId: Int) {
        viewModelScope.launch {
            try {
                val resp = api.borrarDieta(dietaId)
                if (resp.isSuccessful) cargar()
            } catch (_: Exception) {}
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DietaViewModel(context) as T
    }
}
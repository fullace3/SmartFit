package com.example.proyectazo.ui.viewmodel.DietaYComida

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the food detail screen.
 * All nutritional values are per 100g, matching the database storage format.
 * isLoading starts as true so the screen shows a spinner before the first API response.
 */
data class DetalleComidaUiState(
    val nombre: String = "",
    val calorias100g: Int = 0,
    val proteinas100g: Int = 0,
    val carbohidratos100g: Int = 0,
    val grasas100g: Int = 0,
    val imagen: String? = null,  // Filename or full URL — the screen builds the S3 URL if needed
    val isLoading: Boolean = true
)

/**
 * ViewModel for DetalleComidaScreen.
 * Loads a single food item by ID and exposes its nutritional data to the UI.
 * The comidaId is passed at construction time and the load starts immediately in init.
 */
class DetalleComidaViewModel(context: Context, private val comidaId: Int) : ViewModel() {

    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(DetalleComidaUiState())
    val uiState: StateFlow<DetalleComidaUiState> = _uiState

    init { cargar() }

    /**
     * Fetches the food item from the API and updates the UI state.
     * On failure, isLoading is set to false so the screen stops showing the spinner.
     */
    private fun cargar() {
        viewModelScope.launch {
            try {
                val resp = api.getComida(comidaId)
                if (resp.isSuccessful) {
                    val comida = resp.body()!!
                    _uiState.update {
                        it.copy(
                            nombre = comida.nombre,
                            calorias100g = comida.calorias_100g,
                            proteinas100g = comida.proteinas_100g,
                            carbohidratos100g = comida.carbohidratos_100g,
                            grasas100g = comida.grasas_100g,
                            imagen = comida.imagen,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Factory passes comidaId at construction — the ViewModel cannot be reused for a different food
    class Factory(private val context: Context, private val comidaId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetalleComidaViewModel(context, comidaId) as T
    }
}
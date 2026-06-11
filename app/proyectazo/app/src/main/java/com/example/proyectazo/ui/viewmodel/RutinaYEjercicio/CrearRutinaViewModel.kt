package com.example.proyectazo.ui.viewmodel.RutinaYEjercicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.ApiService
import com.example.proyectazo.network.RutinaRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Sealed class representing the routine creation states.
 * RutinaCreada carries the new routine's ID so the screen can navigate
 * to the exercise picker with the correct rutinaId.
 */
sealed class CrearRutinaUiState {
    object Idle    : CrearRutinaUiState()
    object Loading : CrearRutinaUiState()
    data class RutinaCreada(val rutinaId: Int) : CrearRutinaUiState()
    data class Error(val mensaje: String) : CrearRutinaUiState()
}

/**
 * ViewModel for CrearRutinaScreen.
 * Solves the duplicate routine problem: the first tap on "Añadir ejercicio"
 * creates the routine via the API and stores the ID in rutinaIdCreada.
 * Subsequent taps reuse the stored ID instead of creating a new routine.
 */
class RutinaViewModel(
    private val apiService: ApiService,
    private val userId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<CrearRutinaUiState>(CrearRutinaUiState.Idle)
    val uiState: StateFlow<CrearRutinaUiState> = _uiState.asStateFlow()

    // Persists the created routine's ID in memory for the lifetime of this ViewModel
    // Prevents a new routine being created every time the user navigates to the exercise picker
    private var rutinaIdCreada: Int? = null

    /**
     * Creates the routine on the first call and navigates to the exercise picker.
     * On subsequent calls (user adds more exercises), reuses the existing ID.
     * Validates the name before making the API call to avoid creating unnamed routines.
     */
    fun crearONavegar(nombre: String) {
        // Reuse the existing routine ID if the routine was already created in this session
        val idExistente = rutinaIdCreada
        if (idExistente != null) {
            _uiState.value = CrearRutinaUiState.RutinaCreada(idExistente)
            return
        }

        if (nombre.isBlank()) {
            _uiState.value = CrearRutinaUiState.Error("Escribe un nombre antes de añadir ejercicios")
            return
        }

        viewModelScope.launch {
            _uiState.value = CrearRutinaUiState.Loading
            try {
                val response = apiService.crearRutina(
                    RutinaRequest(nombre = nombre, id_usuario = userId)
                )
                if (response.isSuccessful) {
                    val rutinaId = response.body()?.id_rutina
                        ?: throw Exception("La API no devolvió id_rutina")
                    rutinaIdCreada = rutinaId  // Store so subsequent calls skip the API
                    _uiState.value = CrearRutinaUiState.RutinaCreada(rutinaId)
                } else {
                    _uiState.value = CrearRutinaUiState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = CrearRutinaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    // Resets to Idle after the screen has consumed the RutinaCreada event
    fun resetState() { _uiState.value = CrearRutinaUiState.Idle }

    class Factory(
        private val apiService: ApiService,
        private val userId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RutinaViewModel(apiService, userId) as T
    }
}

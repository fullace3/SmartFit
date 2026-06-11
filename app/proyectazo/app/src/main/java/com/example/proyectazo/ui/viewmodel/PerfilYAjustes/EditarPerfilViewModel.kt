package com.example.proyectazo.ui.viewmodel.PerfilYAjustes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.MedidaRequest
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sealed class representing the save operation states.
 * Using a sealed class instead of a boolean allows the UI to distinguish
 * between idle, loading, success and error without multiple separate flags.
 */
sealed class EditarGuardarEstado {
    object Idle     : EditarGuardarEstado()  // No save attempted yet
    object Cargando : EditarGuardarEstado()  // Save in progress — button is disabled
    object Exito    : EditarGuardarEstado()  // Save succeeded — screen navigates back
    data class Error(val mensaje: String) : EditarGuardarEstado()  // Save failed — message shown
}

/**
 * UI state for the profile editing screen.
 * Physical data (height, weight) is stored as String to match the text field input —
 * conversion to Double happens only at save time.
 */
data class EditarPerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val alturaCm: String = "",   // String so the text field can show partial input (e.g. "17")
    val pesoKg: String = "",
    val objetivo: String = "",
    val isLoading: Boolean = true,
    val guardarEstado: EditarGuardarEstado = EditarGuardarEstado.Idle
)

/**
 * ViewModel for EditarPerfilScreen.
 * Loads the user profile and their most recent body measurement on init.
 * On save, records a new measurement entry and stores the fitness goal locally.
 */
class EditarPerfilViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val session = SessionManager(context)
    private val userId = session.getUserId()

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState

    init { cargar() }

    /**
     * Loads user data and the most recent body measurement in parallel.
     * Uses the last measurement by date to pre-fill the height and weight fields.
     */
    private fun cargar() {
        viewModelScope.launch {
            try {
                val usuario = api.getUsuario(userId).body()

                // Load measurements separately — failure here should not block the profile load
                val medidas = try {
                    api.getMedidas(userId).body()?.sortedBy { it.fecha } ?: emptyList()
                } catch (e: Exception) { emptyList() }

                val ultima = medidas.lastOrNull()  // Most recent measurement by date

                _uiState.value = EditarPerfilUiState(
                    nombre   = usuario?.nombre ?: "",
                    email    = usuario?.email ?: "",
                    alturaCm = ultima?.altura_cm?.let { "${it.toInt()}" } ?: "",
                    pesoKg   = ultima?.peso_kg?.let { String.format("%.1f", it) } ?: "",
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Field setters — each triggers a recomposition of the affected field only
    fun onNombreChange(v: String)   = _uiState.update { it.copy(nombre = v) }
    fun onEmailChange(v: String)    = _uiState.update { it.copy(email = v) }
    fun onAlturaChange(v: String)   = _uiState.update { it.copy(alturaCm = v) }
    fun onPesoChange(v: String)     = _uiState.update { it.copy(pesoKg = v) }
    fun onObjetivoChange(v: String) = _uiState.update { it.copy(objetivo = v) }

    // Resets guardarEstado to Idle after the screen has reacted to Exito or Error
    fun resetEstado() = _uiState.update { it.copy(guardarEstado = EditarGuardarEstado.Idle) }

    /**
     * Saves the profile changes:
     * 1. Records a new body measurement entry (weight + height) via the API.
     * 2. Stores the fitness goal in SharedPreferences (no dedicated API endpoint).
     * Only saves a measurement if weight is a valid number — height is optional.
     */
    fun guardar() {
        val state = _uiState.value
        _uiState.update { it.copy(guardarEstado = EditarGuardarEstado.Cargando) }

        viewModelScope.launch {
            try {
                val peso   = state.pesoKg.toDoubleOrNull()
                val altura = state.alturaCm.toDoubleOrNull()

                if (peso != null) {
                    val request = MedidaRequest(
                        id_usuario = userId,
                        peso_kg    = peso,
                        altura_cm  = altura   // Nullable — height is optional
                    )
                    val resp = api.registrarMedida(request)
                    if (!resp.isSuccessful) {
                        _uiState.update {
                            it.copy(guardarEstado = EditarGuardarEstado.Error("Error al guardar: ${resp.code()}"))
                        }
                        return@launch
                    }
                }

                // Fitness goal is stored locally — the API has no dedicated goal endpoint
                val prefs = context.getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)
                prefs.edit().putString("objetivo_usuario", state.objetivo).apply()

                _uiState.update { it.copy(guardarEstado = EditarGuardarEstado.Exito) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardarEstado = EditarGuardarEstado.Error("Sin conexión con el servidor"))
                }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditarPerfilViewModel(context) as T
    }
}
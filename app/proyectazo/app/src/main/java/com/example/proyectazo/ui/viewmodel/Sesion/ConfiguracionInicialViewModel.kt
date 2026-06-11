package com.example.proyectazo.ui.viewmodel.Sesion

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
 * UI state for the initial setup screen.
 * horaEntrenamiento is stored as a Pair<Int, Int> (hour, minute) to match
 * the TimePickerState format and avoid string parsing until save time.
 * errorPeso and errorAltura are separate from the general error field so
 * each validation message appears inline below its own field.
 */
data class ConfiguracionInicialUiState(
    val peso: String = "",
    val altura: String = "",
    val horaEntrenamiento: Pair<Int, Int> = Pair(20, 0),  // Default: 20:00
    val isLoading: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null,
    val errorPeso: String? = null,     // Shown inline below the weight field
    val errorAltura: String? = null    // Shown inline below the height field
)

/**
 * ViewModel for ConfiguracionInicialScreen.
 * This screen runs exactly once — after the first login.
 * The completion flag (configuracion_completada_$userId) is always written
 * regardless of whether the API call succeeds, so the user is never stuck
 * on this screen if the server is temporarily unavailable.
 */
class ConfiguracionInicialViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val sessionManager = SessionManager(context)
    private val userId = sessionManager.getUserId()

    private val _uiState = MutableStateFlow(ConfiguracionInicialUiState())
    val uiState: StateFlow<ConfiguracionInicialUiState> = _uiState

    // Field setters clear their respective error on each change
    fun onPesoChange(v: String)   = _uiState.update { it.copy(peso = v, errorPeso = null) }
    fun onAlturaChange(v: String) = _uiState.update { it.copy(altura = v, errorAltura = null) }
    fun onHoraChange(hora: Int, minuto: Int) =
        _uiState.update { it.copy(horaEntrenamiento = Pair(hora, minuto)) }

    /**
     * Validates the fields, saves the body measurement via the API (best-effort),
     * and always writes the completion flag to SharedPreferences.
     *
     * The API call is wrapped in a silent try-catch intentionally —
     * if the server is down, the user should still be able to complete setup.
     * The measurement can be recorded later from the profile screen.
     */
    fun guardar(onExitoso: () -> Unit) {
        val state = _uiState.value

        val pesoNum   = state.peso.toDoubleOrNull()
        val alturaNum = state.altura.toDoubleOrNull()
        var hayError  = false

        // Validate both fields before making any API call
        if (pesoNum == null || pesoNum <= 0) {
            _uiState.update { it.copy(errorPeso = "Introduce un peso válido") }
            hayError = true
        }
        if (alturaNum == null || alturaNum <= 0) {
            _uiState.update { it.copy(errorAltura = "Introduce una altura válida") }
            hayError = true
        }
        if (hayError) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // Best-effort API call — failure is silently ignored so setup always completes
            try {
                api.registrarMedida(
                    MedidaRequest(
                        id_usuario = userId,
                        peso_kg    = pesoNum!!,
                        altura_cm  = alturaNum
                    )
                )
            } catch (_: Exception) { /* Setup continues even if the API is unreachable */ }

            // Always write the completion flag — keyed by userId so it works per account
            val horaStr = "%02d:%02d".format(
                state.horaEntrenamiento.first,
                state.horaEntrenamiento.second
            )
            context.getSharedPreferences("smartfit_config", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("configuracion_completada_$userId", true)  // Prevents showing this screen again
                .putString("hora_entrenamiento", horaStr)
                .apply()

            _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
            onExitoso()
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ConfiguracionInicialViewModel(context) as T
    }
}
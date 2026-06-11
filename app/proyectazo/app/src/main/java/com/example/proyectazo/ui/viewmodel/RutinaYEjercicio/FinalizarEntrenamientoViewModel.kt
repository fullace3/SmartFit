package com.example.proyectazo.ui.viewmodel.RutinaYEjercicio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.HistorialRequest
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import com.example.proyectazo.ui.screens.RutinasYEjercicio.ResultadoEntrenamiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Sealed class for the save operation state.
 * Guardado is the terminal success state — the screen navigates away on this.
 */
sealed class GuardarUiState {
    object Idle      : GuardarUiState()
    object Guardando : GuardarUiState()
    object Guardado  : GuardarUiState()
    data class Error(val msg: String) : GuardarUiState()
}

/**
 * ViewModel for FinalizarEntrenamientoScreen.
 * Saves one history entry per exercise containing only the completed sets.
 * Exercises where no sets were completed are skipped entirely.
 * Weight and reps are averaged across completed sets before saving —
 * the API stores one aggregate record per exercise, not one per set.
 */
class FinalizarEntrenamientoViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _state = MutableStateFlow<GuardarUiState>(GuardarUiState.Idle)
    val state: StateFlow<GuardarUiState> = _state

    /**
     * Iterates over all exercises in the workout result and saves a history
     * entry for each one that has at least one completed set.
     *
     * Aggregation strategy:
     * - peso_kg: average of completed set weights (0.0 if none were entered)
     * - repeticiones: average of completed set reps, minimum 1
     * - series: count of completed sets
     * - duracion_minutos: total session duration, minimum 1 minute
     */
    fun guardarEntrenamiento(resultado: ResultadoEntrenamiento) {
        viewModelScope.launch {
            _state.value = GuardarUiState.Guardando
            try {
                resultado.ejercicios.forEach { ejercicioRes ->
                    val seriesCompletadas = ejercicioRes.series.filter { it.completada }

                    // Skip exercises where the user did not complete any sets
                    if (seriesCompletadas.isNotEmpty()) {

                        // Average weight across completed sets — null entries (empty fields) default to 0
                        val pesoPromedio = seriesCompletadas
                            .mapNotNull { it.peso.toDoubleOrNull() }
                            .average()
                            .takeIf { !it.isNaN() } ?: 0.0

                        // Average reps — coerceAtLeast(1) prevents storing 0 reps
                        val repsPromedio = seriesCompletadas
                            .mapNotNull { it.reps.toIntOrNull() }
                            .average().toInt().coerceAtLeast(1)

                        api.registrarHistorial(
                            HistorialRequest(
                                id_usuario       = userId,
                                id_ejercicio     = ejercicioRes.id,
                                id_rutina        = resultado.rutinaId,
                                peso_kg          = pesoPromedio,
                                repeticiones     = repsPromedio,
                                series           = seriesCompletadas.size,
                                // coerceAtLeast(1) prevents saving 0 minutes for very short sessions
                                duracion_minutos = (resultado.tiempoSegundos / 60).coerceAtLeast(1)
                            )
                        )
                    }
                }
                _state.value = GuardarUiState.Guardado
            } catch (e: Exception) {
                _state.value = GuardarUiState.Error("Error al guardar: ${e.message}")
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FinalizarEntrenamientoViewModel(context) as T
    }
}
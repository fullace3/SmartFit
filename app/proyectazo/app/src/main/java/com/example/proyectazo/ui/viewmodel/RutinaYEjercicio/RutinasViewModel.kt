package com.example.proyectazo.ui.viewmodel.RutinaYEjercicio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.EjercicioResponse
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.RutinaResponse
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Combines a routine with its full exercise details.
 * The API returns exercise IDs in the routine-exercise relation —
 * this model joins them with the full EjercicioResponse for display.
 * Exercises are ordered by the 'orden' field defined in RUTINA_EJERCICIO.
 */
data class RutinaConEjercicios(
    val rutina: RutinaResponse,
    val ejercicios: List<EjercicioResponse>  // Ordered by 'orden' field from RUTINA_EJERCICIO
)

/**
 * Sealed UI state for the routines list screen.
 * Vacio is a separate state from Exito(emptyList()) so the screen
 * can show a specific empty state message instead of a blank list.
 */
sealed class RutinasUiState {
    object Cargando : RutinasUiState()
    data class Exito(val rutinas: List<RutinaConEjercicios>) : RutinasUiState()
    object Vacio    : RutinasUiState()
    data class Error(val mensaje: String) : RutinasUiState()
}

/**
 * ViewModel for PantallaRutinas.
 * Loads routines and the full exercise catalog in parallel using async/await,
 * then joins them locally to avoid one API call per routine.
 */
class RutinasViewModel(context: Context) : ViewModel() {

    private val session = SessionManager(context)
    private val userId get() = session.getUserId()

    private val _uiState = MutableStateFlow<RutinasUiState>(RutinasUiState.Cargando)
    val uiState: StateFlow<RutinasUiState> = _uiState

    init { cargarRutinas() }

    /**
     * Loading strategy:
     * 1. Fetch the exercise catalog and the user's routines in parallel (async/await).
     * 2. Build a lookup map from the catalog for O(1) exercise resolution.
     * 3. For each routine, fetch its exercise links and resolve full details from the map.
     * This avoids N×M API calls — only N calls for N routines plus 2 parallel calls upfront.
     */
    fun cargarRutinas() {
        viewModelScope.launch {
            _uiState.value = RutinasUiState.Cargando
            try {
                // Step 1 — fetch catalog and routines in parallel to halve load time
                val ejerciciosDeferred = async { RetrofitClient.instance.getEjercicios() }
                val rutinasDeferred    = async { RetrofitClient.instance.getRutinas(userId) }

                val ejerciciosResp = ejerciciosDeferred.await()
                val rutinasResp    = rutinasDeferred.await()

                if (!ejerciciosResp.isSuccessful || !rutinasResp.isSuccessful) {
                    _uiState.value = RutinasUiState.Error("Error al cargar los datos del servidor")
                    return@launch
                }

                // Step 2 — build lookup map for O(1) exercise resolution per routine
                val ejerciciosMap = (ejerciciosResp.body() ?: emptyList())
                    .associateBy { it.id_ejercicio }

                val rutinas = rutinasResp.body() ?: emptyList()

                if (rutinas.isEmpty()) {
                    _uiState.value = RutinasUiState.Vacio
                    return@launch
                }

                // Step 3 — for each routine, fetch its exercise links and resolve details
                val rutinasConEjercicios = rutinas.map { rutina ->
                    val relResp = RetrofitClient.instance.getEjerciciosDeRutina(rutina.id_rutina)
                    val ejerciciosDeRutina = if (relResp.isSuccessful) {
                        relResp.body()
                            ?.sortedBy { it.orden }
                            ?.mapNotNull { rel -> ejerciciosMap[rel.id_ejercicio] }
                            ?: emptyList()
                    } else {
                        emptyList()
                    }
                    RutinaConEjercicios(rutina = rutina, ejercicios = ejerciciosDeRutina)
                }

                _uiState.value = RutinasUiState.Exito(rutinasConEjercicios)

            } catch (e: Exception) {
                _uiState.value = RutinasUiState.Error("Sin conexión con el servidor")
            }
        }
    }

    /**
     * Deletes a routine and reloads the list on success.
     * Only reloads if the deletion was confirmed by the server —
     * avoids a redundant network call if the DELETE fails.
     */
    fun eliminarRutina(rutinaId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.borrarRutina(rutinaId)
                if (response.isSuccessful) {
                    cargarRutinas()
                } else {
                    _uiState.value = RutinasUiState.Error("Error al eliminar la rutina")
                }
            } catch (e: Exception) {
                _uiState.value = RutinasUiState.Error("Sin conexión con el servidor")
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RutinasViewModel(context) as T
    }
}
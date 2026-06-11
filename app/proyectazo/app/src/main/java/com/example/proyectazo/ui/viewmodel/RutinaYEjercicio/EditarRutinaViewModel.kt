package com.example.proyectazo.ui.viewmodel.RutinaYEjercicio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.EjercicioRutina
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.RutinaRequest
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the routine editing screen.
 * guardado is a one-shot event flag — set to true when the save succeeds
 * and consumed immediately by the screen via onGuardadoConsumed().
 */
data class EditarRutinaUiState(
    val nombre: String = "",
    val ejercicios: List<EjercicioRutina> = emptyList(),
    val isLoading: Boolean = true,
    val guardado: Boolean = false,  // One-shot event — triggers navigation back on true
    val error: String? = null
)

/**
 * ViewModel for EditarRutinaScreen.
 * cargarRutina() is public so the screen can call it via DisposableEffect
 * every time it returns to the foreground (e.g. after adding an exercise).
 * Exercise deletion updates the local list immediately without a full reload.
 */
class EditarRutinaViewModel(
    private val rutinaId: Int,
    private val context: Context
) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(EditarRutinaUiState())
    val uiState: StateFlow<EditarRutinaUiState> = _uiState.asStateFlow()

    init { cargarRutina() }

    /**
     * Loads the routine name and its exercises from the API.
     * Requires three calls: getRutinas (for the name), getEjerciciosDeRutina (for the links)
     * and getEjercicios (for the full exercise details).
     * The exercise catalog is converted to a map for O(1) lookup by ID.
     */
    fun cargarRutina() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val rutinasResp = api.getRutinas(userId)
                val rutina = rutinasResp.body()?.find { it.id_rutina == rutinaId }

                val relResp       = api.getEjerciciosDeRutina(rutinaId)
                val ejerciciosResp = api.getEjercicios()

                // Map by ID for O(1) lookup when joining with the routine-exercise relations
                val ejerciciosMap = ejerciciosResp.body()?.associateBy { it.id_ejercicio } ?: emptyMap()

                val ejercicios = relResp.body()
                    ?.sortedBy { it.orden }  // Preserve the display order defined by the user
                    ?.mapNotNull { rel ->
                        ejerciciosMap[rel.id_ejercicio]?.let { ej ->
                            EjercicioRutina(
                                id           = ej.id_ejercicio,
                                nombre       = ej.nombre,
                                series       = rel.series,
                                repeticiones = rel.repeticiones,
                                imagenUrl    = ej.imagen ?: ""
                            )
                        }
                    } ?: emptyList()

                _uiState.update {
                    it.copy(isLoading = false, nombre = rutina?.nombre ?: "", ejercicios = ejercicios)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar la rutina") }
            }
        }
    }

    fun onNombreChange(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    /**
     * Removes an exercise from the routine via the API and updates the local list immediately.
     * Avoids a full reload — the local state is the source of truth after a successful deletion.
     */
    fun eliminarEjercicio(ejercicioId: Int) {
        viewModelScope.launch {
            try {
                api.quitarEjercicioDeRutina(rutinaId, ejercicioId)
                // Update local list immediately instead of reloading from the API
                _uiState.update { state ->
                    state.copy(ejercicios = state.ejercicios.filter { it.id != ejercicioId })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al eliminar el ejercicio") }
            }
        }
    }

    /**
     * Saves the updated routine name via PUT.
     * Only the name is editable here — exercises are managed via add/remove operations.
     */
    fun guardarCambios() {
        val nombre = _uiState.value.nombre
        if (nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }
        viewModelScope.launch {
            try {
                api.editarRutina(rutinaId, RutinaRequest(nombre = nombre, id_usuario = userId))
                _uiState.update { it.copy(guardado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar los cambios") }
            }
        }
    }

    // Resets the one-shot guardado flag after the screen has navigated back
    fun onGuardadoConsumed() { _uiState.update { it.copy(guardado = false) } }

    class Factory(private val rutinaId: Int, private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditarRutinaViewModel(rutinaId, context) as T
    }
}

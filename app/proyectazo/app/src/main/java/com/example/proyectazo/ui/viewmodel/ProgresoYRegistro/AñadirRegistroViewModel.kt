package com.example.proyectazo.ui.viewmodel.ProgresoYRegistro

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.MedidaRequest
import com.example.proyectazo.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Sealed class for the save operation state.
 * Same pattern as EditarGuardarEstado — gives the UI four distinct states
 * without multiple boolean flags.
 */
sealed class GuardarEstado {
    object Idle     : GuardarEstado()
    object Cargando : GuardarEstado()
    object Exito    : GuardarEstado()
    data class Error(val mensaje: String) : GuardarEstado()
}

/**
 * UI state for the body measurement registration screen.
 * Date is split into three separate String fields (dia/mes/anio) to match
 * the three individual text fields in the UI — validation happens at save time.
 * Weight and height use Float to drive the Slider components directly.
 */
data class AñadirRegistroUiState(
    val dia: String = "",
    val mes: String = "",
    val anio: String = "",
    val fechaError: String? = null,   // Shown inline below the date fields on invalid input
    val pesoKg: Float = 70f,          // Default slider position
    val alturaCm: Float = 170f,       // Default slider position
    val brazoCm: String = "",
    val cinturaCm: String = "",
    val pechoCm: String = "",
    val piernaCm: String = "",
    val cargando: Boolean = false,
    val guardarEstado: GuardarEstado = GuardarEstado.Idle
)

/**
 * ViewModel for AñadirRegistroScreen.
 * Validates the date fields before saving and builds an ISO 8601 timestamp
 * using LocalDate to reject impossible dates (e.g. 31/02 or 00/13).
 */
class AñadirRegistroViewModel(context: Context) : ViewModel() {

    private val api = RetrofitClient.instance

    private val _uiState = MutableStateFlow(AñadirRegistroUiState())
    val uiState: StateFlow<AñadirRegistroUiState> = _uiState

    // Date setters clear fechaError on each change so the error disappears as the user corrects
    fun onDiaChange(v: String)    = _uiState.update { it.copy(dia = v, fechaError = null) }
    fun onMesChange(v: String)    = _uiState.update { it.copy(mes = v, fechaError = null) }
    fun onAnioChange(v: String)   = _uiState.update { it.copy(anio = v, fechaError = null) }
    fun onPesoChange(v: Float)    = _uiState.update { it.copy(pesoKg = v) }
    fun onAlturaChange(v: Float)  = _uiState.update { it.copy(alturaCm = v) }
    fun onBrazoChange(v: String)  = _uiState.update { it.copy(brazoCm = v) }
    fun onCinturaChange(v: String)= _uiState.update { it.copy(cinturaCm = v) }
    fun onPechoChange(v: String)  = _uiState.update { it.copy(pechoCm = v) }
    fun onPiernaChange(v: String) = _uiState.update { it.copy(piernaCm = v) }
    fun resetGuardarEstado()      = _uiState.update { it.copy(guardarEstado = GuardarEstado.Idle) }

    /**
     * Validates the date and saves the body measurement via the API.
     * Date validation uses LocalDate.of() which throws for impossible dates —
     * this catches cases like 31/02 or 00/13 that regex alone would not catch.
     * Measurement fields are optional — null is sent for empty strings.
     */
    fun guardar(userId: Int) {
        val state = _uiState.value

        // Validate and build ISO date before making the API call
        val fechaIso = buildFechaIso(state.dia, state.mes, state.anio)
        if (fechaIso == null) {
            _uiState.update { it.copy(fechaError = "Fecha inválida. Usa un día, mes y año reales.") }
            return
        }

        _uiState.update { it.copy(cargando = true, guardarEstado = GuardarEstado.Cargando) }

        val request = MedidaRequest(
            id_usuario        = userId,
            peso_kg           = state.pesoKg.toDouble(),
            altura_cm         = state.alturaCm.toDouble(),
            pecho_cm          = state.pechoCm.toDoubleOrNull(),
            pierna_cm         = state.piernaCm.toDoubleOrNull(),
            brazo_cm          = state.brazoCm.toDoubleOrNull(),
            // TODO: cintura_cm requires a dedicated column in MEDIDA_CORPORAL —
            // stored as null until the database schema is extended
            grasa_corporal_pct = null
        )

        viewModelScope.launch {
            try {
                val resp = api.registrarMedida(request)
                if (resp.isSuccessful) {
                    _uiState.update {
                        it.copy(cargando = false, guardarEstado = GuardarEstado.Exito)
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            guardarEstado = GuardarEstado.Error("Error ${resp.code()}: no se pudo guardar")
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        guardarEstado = GuardarEstado.Error("Sin conexión con el servidor")
                    )
                }
            }
        }
    }

    /**
     * Builds an ISO 8601 timestamp from the three date fields.
     * Returns null if any field is not a valid integer or if the date is impossible.
     * LocalDate.of() handles calendar validation (days per month, leap years, etc.)
     */
    private fun buildFechaIso(dia: String, mes: String, anio: String): String? {
        val d = dia.toIntOrNull()  ?: return null
        val m = mes.toIntOrNull()  ?: return null
        val y = anio.toIntOrNull() ?: return null
        return try {
            val date = LocalDate.of(y, m, d)  // Throws DateTimeException for invalid dates
            "${date}T00:00:00"
        } catch (e: Exception) {
            null  // Invalid date (e.g. Feb 31) returns null → error shown in the UI
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AñadirRegistroViewModel(context) as T
    }
}
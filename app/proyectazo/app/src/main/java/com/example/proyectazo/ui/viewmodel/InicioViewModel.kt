package com.example.proyectazo.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.DietaResponse
import com.example.proyectazo.network.HistorialDetalleResponse
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.RutinaResponse
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Sealed class representing the workout state for the selected day.
 * ConEntreno carries the full exercise list and session summary so the
 * home screen can display chips without additional API calls.
 */
sealed class EntrenoDelDia {
    object Cargando   : EntrenoDelDia()
    object SinEntreno : EntrenoDelDia()
    data class ConEntreno(
        val ejercicios: List<HistorialDetalleResponse>,
        val series: Int,
        val duracionEstimadaMin: Int,  // maxOf all exercises — they share the same session duration
        val rutinaId: Int,
        val rutinaNombre: String?      // Resolved from the routines list — null if routine was deleted
    ) : EntrenoDelDia()
    data class Error(val mensaje: String) : EntrenoDelDia()
}

/**
 * Sealed class representing the diet state for the home screen.
 * 404 is mapped to SinDieta instead of Error so the UI shows
 * a friendly empty state rather than an error message.
 */
sealed class DietaDelDia {
    object Cargando : DietaDelDia()
    object SinDieta : DietaDelDia()
    data class ConDieta(val dieta: DietaResponse) : DietaDelDia()
    data class Error(val mensaje: String) : DietaDelDia()
}

/**
 * ViewModel for PantallaInicio.
 * Loads the full training history and routine list once on init,
 * then filters locally when the user selects a different day in the DatePicker.
 * This avoids an API call on every date change — filtering is instant.
 */
class InicioViewModel(context: Context) : ViewModel() {

    private val session = SessionManager(context)
    private val userId get() = session.getUserId()

    // Full history and routines cached in memory — filtered locally per selected day
    private var historialCompleto: List<HistorialDetalleResponse> = emptyList()
    private var rutinas: List<RutinaResponse> = emptyList()

    private val _diaSeleccionado = MutableStateFlow(LocalDate.now())
    val diaSeleccionado: StateFlow<LocalDate> = _diaSeleccionado

    private val _entrenoDelDia = MutableStateFlow<EntrenoDelDia>(EntrenoDelDia.Cargando)
    val entrenoDelDia: StateFlow<EntrenoDelDia> = _entrenoDelDia

    private val _dietaDelDia = MutableStateFlow<DietaDelDia>(DietaDelDia.Cargando)
    val dietaDelDia: StateFlow<DietaDelDia> = _dietaDelDia

    init { cargarDatos() }

    private fun cargarDatos() {
        cargarHistorial()
        cargarDieta()
    }

    /**
     * Loads the full training history and the routine list in a single coroutine.
     * Routines are loaded alongside history so exercise entries can be joined
     * with their routine name without a second API call per entry.
     */
    private fun cargarHistorial() {
        viewModelScope.launch {
            try {
                val histResp = RetrofitClient.instance.getHistorial(userId)
                val rutResp  = RetrofitClient.instance.getRutinas(userId)

                if (histResp.isSuccessful && histResp.body() != null) {
                    historialCompleto = histResp.body()!!
                    rutinas = if (rutResp.isSuccessful) rutResp.body() ?: emptyList() else emptyList()
                    filtrarEntrenoPorDia(_diaSeleccionado.value)  // Show today's workout immediately
                } else {
                    _entrenoDelDia.value = EntrenoDelDia.Error("No se pudo cargar el historial")
                }
            } catch (e: Exception) {
                _entrenoDelDia.value = EntrenoDelDia.Error("Sin conexión")
            }
        }
    }

    /**
     * Loads the most recently created diet for the home screen summary.
     * 404 means no diet exists — mapped to SinDieta, not Error.
     */
    private fun cargarDieta() {
        viewModelScope.launch {
            try {
                val respuesta = RetrofitClient.instance.getDietaActual(userId)
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    _dietaDelDia.value = DietaDelDia.ConDieta(respuesta.body()!!)
                } else if (respuesta.code() == 404) {
                    _dietaDelDia.value = DietaDelDia.SinDieta  // No diet created yet
                } else {
                    _dietaDelDia.value = DietaDelDia.Error("Error al cargar la dieta")
                }
            } catch (e: Exception) {
                _dietaDelDia.value = DietaDelDia.Error("Sin conexión")
            }
        }
    }

    // Called by the screen when the user taps a different day in the DatePicker
    fun seleccionarDia(nuevoDia: LocalDate) {
        _diaSeleccionado.value = nuevoDia
        filtrarEntrenoPorDia(nuevoDia)
    }

    /**
     * Filters the cached history for the selected day without an API call.
     * History dates arrive as ISO 8601 strings ("2025-08-17T10:30:00") —
     * only the first 10 characters are compared to match the date portion.
     * duracionEstimadaMin uses maxOf because all exercises in a session
     * share the same total duration — summing would multiply it incorrectly.
     */
    private fun filtrarEntrenoPorDia(dia: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val ejerciciosDelDia = historialCompleto.filter { registro ->
            registro.fecha.take(10) == dia.format(formatter)  // Compare date part only
        }

        _entrenoDelDia.value = if (ejerciciosDelDia.isEmpty()) {
            EntrenoDelDia.SinEntreno
        } else {
            val totalSeries  = ejerciciosDelDia.sumOf { it.series }
            val rutinaId     = ejerciciosDelDia.first().id_rutina
            val rutinaNombre = rutinas.find { it.id_rutina == rutinaId }?.nombre
            EntrenoDelDia.ConEntreno(
                ejercicios          = ejerciciosDelDia,
                series              = totalSeries,
                duracionEstimadaMin = ejerciciosDelDia.maxOf { it.duracion_minutos },
                rutinaId            = rutinaId,
                rutinaNombre        = rutinaNombre
            )
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InicioViewModel(context) as T
    }
}
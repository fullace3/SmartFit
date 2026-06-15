package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.data.local.entity.DietaEntity
import com.example.proyectazo.data.local.entity.HistorialEntity
import com.example.proyectazo.data.repository.SmartFitRepository
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class EntrenoDelDia {
    object Cargando   : EntrenoDelDia()
    object SinEntreno : EntrenoDelDia()
    data class ConEntreno(
        val ejercicios: List<HistorialEntity>,
        val series: Int,
        val duracionEstimadaMin: Int,
        val rutinaId: Int,
        val rutinaNombre: String?
    ) : EntrenoDelDia()
    data class Error(val mensaje: String) : EntrenoDelDia()
}

sealed class DietaDelDia {
    object Cargando : DietaDelDia()
    object SinDieta : DietaDelDia()
    data class ConDieta(val dieta: DietaEntity) : DietaDelDia()
    data class Error(val mensaje: String) : DietaDelDia()
}

class InicioViewModel(context: Context) : ViewModel() {

    private val repo    = SmartFitRepository.getInstance(context)
    private val session = SessionManager(context)
    private val userId  get() = session.getUserId()

    private var historialCompleto: List<HistorialEntity> = emptyList()
    private var rutinaNombres: Map<Int, String> = emptyMap()

    private val _diaSeleccionado = MutableStateFlow(LocalDate.now())
    val diaSeleccionado: StateFlow<LocalDate> = _diaSeleccionado

    private val _entrenoDelDia = MutableStateFlow<EntrenoDelDia>(EntrenoDelDia.Cargando)
    val entrenoDelDia: StateFlow<EntrenoDelDia> = _entrenoDelDia

    private val _dietaDelDia = MutableStateFlow<DietaDelDia>(DietaDelDia.Cargando)
    val dietaDelDia: StateFlow<DietaDelDia> = _dietaDelDia

    init {
        cargarHistorial()
        cargarDieta()
    }

    private fun cargarHistorial() {
        viewModelScope.launch {
            try {
                historialCompleto = repo.getHistorial(userId)
                rutinaNombres = repo.getRutinas(userId).associate { it.id_rutina to it.nombre }
                filtrarEntrenoPorDia(_diaSeleccionado.value)
            } catch (e: Exception) {
                _entrenoDelDia.value = EntrenoDelDia.Error("No se pudo cargar el historial")
            }
        }
    }

    private fun cargarDieta() {
        viewModelScope.launch {
            try {
                val dieta = repo.getDietaActiva(userId)
                _dietaDelDia.value = if (dieta != null) DietaDelDia.ConDieta(dieta)
                                     else DietaDelDia.SinDieta
            } catch (e: Exception) {
                _dietaDelDia.value = DietaDelDia.Error("Error al cargar la dieta")
            }
        }
    }

    fun seleccionarDia(nuevoDia: LocalDate) {
        _diaSeleccionado.value = nuevoDia
        filtrarEntrenoPorDia(nuevoDia)
    }

    private fun filtrarEntrenoPorDia(dia: LocalDate) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val ejerciciosDelDia = historialCompleto.filter { registro ->
            registro.fecha.take(10) == dia.format(formatter)
        }
        _entrenoDelDia.value = if (ejerciciosDelDia.isEmpty()) {
            EntrenoDelDia.SinEntreno
        } else {
            val rutinaId = ejerciciosDelDia.first().id_rutina
            EntrenoDelDia.ConEntreno(
                ejercicios          = ejerciciosDelDia,
                series              = ejerciciosDelDia.sumOf { it.series },
                duracionEstimadaMin = ejerciciosDelDia.maxOf { it.duracion_minutos },
                rutinaId            = rutinaId,
                rutinaNombre        = rutinaNombres[rutinaId]
            )
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InicioViewModel(context) as T
    }
}

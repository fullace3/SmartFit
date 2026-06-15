package com.example.proyectazo.ui.viewmodel.ProgresoYRegistro

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.data.local.entity.MedidaEntity
import com.example.proyectazo.data.repository.SmartFitRepository
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── PROGRESO ──────────────────────────────────────────────────────────────────

data class ProgresoUiState(
    val pesoActual: String = "-",
    val pesoInicial: String = "-",
    val diferenciaPeso: String = "-",
    val imc: String = "-",
    val volumenSemana: List<Pair<String, Double>> = emptyList(),
    val volumenMes: List<Pair<String, Double>> = emptyList(),
    val volumenAnio: List<Pair<String, Double>> = emptyList(),
    val medidas: List<MedidaEntity> = emptyList(),
    val isLoading: Boolean = true
)

class ProgresoViewModel(context: Context) : ViewModel() {

    private val repo   = SmartFitRepository.getInstance(context)
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(ProgresoUiState())
    val uiState: StateFlow<ProgresoUiState> = _uiState

    init { recargar() }

    fun recargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val historial = repo.getHistorial(userId)
                val medidas   = repo.getMedidas(userId)

                val today = LocalDate.now()
                val fmt   = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                val medidasOrdenadas = medidas.sortedBy { it.fecha }
                val pesoActual  = medidasOrdenadas.lastOrNull()?.peso_kg
                val pesoInicial = medidasOrdenadas.firstOrNull()?.peso_kg
                val alturaCm    = medidasOrdenadas.lastOrNull()?.altura_cm

                val diferencia = if (pesoActual != null && pesoInicial != null)
                    String.format("%.1f", pesoActual - pesoInicial) else "-"

                val imc = if (pesoActual != null && alturaCm != null && alturaCm > 0) {
                    val alturaM = alturaCm / 100.0
                    String.format("%.1f", pesoActual / (alturaM * alturaM))
                } else "-"

                val diasSemana = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                val volSemana = diasSemana.mapIndexed { dayIndex, dia ->
                    val offset = (today.dayOfWeek.value - dayIndex - 1).let { if (it < 0) it + 7 else it }
                    val fecha  = today.minusDays(offset.toLong())
                    val vol    = historial.filter {
                        try { LocalDate.parse(it.fecha.take(10), fmt) == fecha } catch (_: Exception) { false }
                    }.sumOf { it.peso_kg * it.repeticiones * it.series }
                    dia to vol
                }

                val volMes = (1..4).map { semana ->
                    val finSemana    = today.minusDays(((semana - 1) * 7).toLong())
                    val inicioSemana = finSemana.minusDays(6)
                    val vol = historial.filter {
                        try {
                            val f = LocalDate.parse(it.fecha.take(10), fmt)
                            !f.isBefore(inicioSemana) && !f.isAfter(finSemana)
                        } catch (_: Exception) { false }
                    }.sumOf { it.peso_kg * it.repeticiones * it.series }
                    "S${5 - semana}" to vol
                }.reversed()

                val meses = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")
                val volAnio = meses.mapIndexed { i, mes ->
                    val vol = historial.filter {
                        try { LocalDate.parse(it.fecha.take(10), fmt).monthValue == i + 1 } catch (_: Exception) { false }
                    }.sumOf { it.peso_kg * it.repeticiones * it.series }
                    mes to vol
                }

                _uiState.value = ProgresoUiState(
                    isLoading      = false,
                    pesoActual     = pesoActual?.let  { String.format("%.1f", it) } ?: "-",
                    pesoInicial    = pesoInicial?.let { String.format("%.1f", it) } ?: "-",
                    diferenciaPeso = diferencia,
                    imc            = imc,
                    volumenSemana  = volSemana,
                    volumenMes     = volMes,
                    volumenAnio    = volAnio,
                    medidas        = medidasOrdenadas.reversed()
                )
            } catch (e: Exception) {
                _uiState.value = ProgresoUiState(isLoading = false)
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProgresoViewModel(context) as T
    }
}

// ── AÑADIR REGISTRO ───────────────────────────────────────────────────────────

sealed class GuardarEstado {
    object Idle     : GuardarEstado()
    object Cargando : GuardarEstado()
    object Exito    : GuardarEstado()
    data class Error(val mensaje: String) : GuardarEstado()
}

data class AñadirRegistroUiState(
    val dia: String = "",
    val mes: String = "",
    val anio: String = "",
    val fechaError: String? = null,
    val pesoKg: Float = 70f,
    val alturaCm: Float = 170f,
    val brazoCm: String = "",
    val cinturaCm: String = "",
    val pechoCm: String = "",
    val piernaCm: String = "",
    val cargando: Boolean = false,
    val guardarEstado: GuardarEstado = GuardarEstado.Idle
)

class AñadirRegistroViewModel(context: Context) : ViewModel() {

    private val repo = SmartFitRepository.getInstance(context)

    private val _uiState = MutableStateFlow(AñadirRegistroUiState())
    val uiState: StateFlow<AñadirRegistroUiState> = _uiState

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

    fun guardar(userId: Int) {
        val state = _uiState.value
        val fechaIso = buildFechaIso(state.dia, state.mes, state.anio)
        if (fechaIso == null) {
            _uiState.update { it.copy(fechaError = "Fecha inválida. Usa un día, mes y año reales.") }
            return
        }
        _uiState.update { it.copy(cargando = true, guardarEstado = GuardarEstado.Cargando) }
        viewModelScope.launch {
            try {
                repo.registrarMedida(
                    userId    = userId,
                    pesoKg    = state.pesoKg.toDouble(),
                    alturaCm  = state.alturaCm.toDouble(),
                    pechoCm   = state.pechoCm.toDoubleOrNull(),
                    piernaCm  = state.piernaCm.toDoubleOrNull(),
                    brazoCm   = state.brazoCm.toDoubleOrNull(),
                    cinturaCm = state.cinturaCm.toDoubleOrNull()
                )
                _uiState.update { it.copy(cargando = false, guardarEstado = GuardarEstado.Exito) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, guardarEstado = GuardarEstado.Error("Error al guardar"))
                }
            }
        }
    }

    private fun buildFechaIso(dia: String, mes: String, anio: String): String? {
        val d = dia.toIntOrNull()  ?: return null
        val m = mes.toIntOrNull()  ?: return null
        val y = anio.toIntOrNull() ?: return null
        return try {
            "${LocalDate.of(y, m, d)}T00:00:00"
        } catch (_: Exception) { null }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AñadirRegistroViewModel(context) as T
    }
}

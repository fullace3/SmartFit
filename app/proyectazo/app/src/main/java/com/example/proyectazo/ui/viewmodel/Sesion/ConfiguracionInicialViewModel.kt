package com.example.proyectazo.ui.viewmodel.Sesion

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.data.repository.SmartFitRepository
import com.example.proyectazo.network.SessionManager
import com.example.proyectazo.notificaciones.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfiguracionInicialUiState(
    val peso: String = "",
    val altura: String = "",
    val horaEntrenamiento: Pair<Int, Int> = Pair(20, 0),
    val isLoading: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null,
    val errorPeso: String? = null,
    val errorAltura: String? = null
)

class ConfiguracionInicialViewModel(private val context: Context) : ViewModel() {

    private val repo    = SmartFitRepository.getInstance(context)
    private val session = SessionManager(context)
    private val userId  = session.getUserId()

    private val _uiState = MutableStateFlow(ConfiguracionInicialUiState())
    val uiState: StateFlow<ConfiguracionInicialUiState> = _uiState

    fun onPesoChange(v: String)   = _uiState.update { it.copy(peso = v, errorPeso = null) }
    fun onAlturaChange(v: String) = _uiState.update { it.copy(altura = v, errorAltura = null) }
    fun onHoraChange(hora: Int, minuto: Int) =
        _uiState.update { it.copy(horaEntrenamiento = Pair(hora, minuto)) }

    fun guardar(onExitoso: () -> Unit) {
        val state     = _uiState.value
        val pesoNum   = state.peso.toDoubleOrNull()
        val alturaNum = state.altura.toDoubleOrNull()
        var hayError  = false

        if (pesoNum == null || pesoNum <= 0) {
            _uiState.update { it.copy(errorPeso = "Introduce un peso válido") }
            hayError = true
        }
        if (alturaNum == null || alturaNum <= 0) {
            _uiState.update { it.copy(errorAltura = "Introduce una altura válida") }
            hayError = true
        }
        if (hayError) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            repo.registrarMedida(
                userId    = userId,
                pesoKg    = pesoNum!!,
                alturaCm  = alturaNum
            )

            val horaStr = "%02d:%02d".format(
                state.horaEntrenamiento.first,
                state.horaEntrenamiento.second
            )
            context.getSharedPreferences("smartfit_config", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("configuracion_completada_$userId", true)
                .putString("hora_entrenamiento", horaStr)
                .apply()

            // Schedule the workout reminder for 30 min before the chosen time
            NotificationScheduler.programar(context, horaStr)

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
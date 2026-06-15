package com.example.proyectazo.ui.viewmodel.PerfilYAjustes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.data.repository.SmartFitRepository
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── PERFIL ────────────────────────────────────────────────────────────────────

data class PerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val alturaCm: String = "-",
    val pesoInicial: String = "-",
    val objetivo: String = "-",
    val isLoading: Boolean = true
)

class PerfilViewModel(private val context: Context) : ViewModel() {

    private val repo    = SmartFitRepository.getInstance(context)
    private val session = SessionManager(context)
    private val userId  = session.getUserId()

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState

    init { recargar() }

    fun recargar() {
        viewModelScope.launch {
            try {
                val usuario = repo.getUsuario(userId)
                val medidas = repo.getMedidas(userId).sortedBy { it.fecha }

                val pesoInicial = medidas.firstOrNull()?.peso_kg
                val altura      = medidas.lastOrNull()?.altura_cm

                val objetivo = context
                    .getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)
                    .getString("objetivo_usuario", "-") ?: "-"

                _uiState.value = PerfilUiState(
                    nombre      = usuario?.nombre ?: "",
                    email       = usuario?.email ?: "",
                    alturaCm    = altura?.let { String.format("%.2f", it / 100.0) + " m" } ?: "-",
                    pesoInicial = pesoInicial?.let { String.format("%.1f", it) + " Kg" } ?: "-",
                    objetivo    = objetivo,
                    isLoading   = false
                )
            } catch (e: Exception) {
                _uiState.value = PerfilUiState(isLoading = false)
            }
        }
    }

    fun cerrarSesion() = session.cerrarSesion()

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PerfilViewModel(context) as T
    }
}

// ── EDITAR PERFIL ─────────────────────────────────────────────────────────────

sealed class EditarGuardarEstado {
    object Idle     : EditarGuardarEstado()
    object Cargando : EditarGuardarEstado()
    object Exito    : EditarGuardarEstado()
    data class Error(val mensaje: String) : EditarGuardarEstado()
}

data class EditarPerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val alturaCm: String = "",
    val pesoKg: String = "",
    val objetivo: String = "",
    val isLoading: Boolean = true,
    val guardarEstado: EditarGuardarEstado = EditarGuardarEstado.Idle
)

class EditarPerfilViewModel(private val context: Context) : ViewModel() {

    private val repo    = SmartFitRepository.getInstance(context)
    private val session = SessionManager(context)
    private val userId  = session.getUserId()

    private val _uiState = MutableStateFlow(EditarPerfilUiState())
    val uiState: StateFlow<EditarPerfilUiState> = _uiState

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                val usuario = repo.getUsuario(userId)
                val medidas = repo.getMedidas(userId).sortedBy { it.fecha }
                val ultima  = medidas.lastOrNull()
                _uiState.value = EditarPerfilUiState(
                    nombre    = usuario?.nombre ?: "",
                    email     = usuario?.email ?: "",
                    alturaCm  = ultima?.altura_cm?.let { "${it.toInt()}" } ?: "",
                    pesoKg    = ultima?.peso_kg?.let { String.format("%.1f", it) } ?: "",
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNombreChange(v: String)   = _uiState.update { it.copy(nombre = v) }
    fun onEmailChange(v: String)    = _uiState.update { it.copy(email = v) }
    fun onAlturaChange(v: String)   = _uiState.update { it.copy(alturaCm = v) }
    fun onPesoChange(v: String)     = _uiState.update { it.copy(pesoKg = v) }
    fun onObjetivoChange(v: String) = _uiState.update { it.copy(objetivo = v) }
    fun resetEstado()               = _uiState.update { it.copy(guardarEstado = EditarGuardarEstado.Idle) }

    fun guardar() {
        val state = _uiState.value
        _uiState.update { it.copy(guardarEstado = EditarGuardarEstado.Cargando) }
        viewModelScope.launch {
            try {
                val peso   = state.pesoKg.toDoubleOrNull()
                val altura = state.alturaCm.toDoubleOrNull()
                if (peso != null) {
                    repo.registrarMedida(
                        userId   = userId,
                        pesoKg   = peso,
                        alturaCm = altura
                    )
                }
                context.getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)
                    .edit().putString("objetivo_usuario", state.objetivo).apply()

                _uiState.update { it.copy(guardarEstado = EditarGuardarEstado.Exito) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardarEstado = EditarGuardarEstado.Error("Error al guardar"))
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

// ── PREFERENCIAS ──────────────────────────────────────────────────────────────

data class PreferenciasUiState(
    val isLoading: Boolean = false,
    val cuentaEliminada: Boolean = false,
    val error: String? = null
)

class PreferenciasViewModel(private val context: Context) : ViewModel() {

    private val repo    = SmartFitRepository.getInstance(context)
    private val session = SessionManager(context)

    private val _uiState = MutableStateFlow(PreferenciasUiState())
    val uiState: StateFlow<PreferenciasUiState> = _uiState

    fun eliminarCuenta(onEliminado: () -> Unit) {
        val userId = session.getUserId()
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                repo.eliminarCuenta(userId)
                context.getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)
                    .edit().clear().apply()
                context.getSharedPreferences("smartfit_config", Context.MODE_PRIVATE)
                    .edit().clear().apply()
                _uiState.update { it.copy(isLoading = false, cuentaEliminada = true) }
                onEliminado()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PreferenciasViewModel(context) as T
    }
}

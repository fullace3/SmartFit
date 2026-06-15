package com.example.proyectazo.ui.viewmodel.Sesion

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.data.repository.SmartFitRepository
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── LOGIN ─────────────────────────────────────────────────────────────────

sealed class LoginUiState {
    object Idle    : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val mensaje: String) : LoginUiState()
}

class LoginViewModel(private val context: Context) : ViewModel() {

    private val repo    = SmartFitRepository.getInstance(context)
    private val session = SessionManager(context)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(nombre: String, password: String) {
        if (nombre.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Rellena todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val usuario = repo.login(nombre, password)
            if (usuario != null) {
                session.guardarSesion(userId = usuario.id_usuario, nombre = usuario.nombre)
                _uiState.value = LoginUiState.Success
            } else {
                _uiState.value = LoginUiState.Error("Usuario o contraseña incorrectos")
            }
        }
    }

    fun resetState() { _uiState.value = LoginUiState.Idle }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(context) as T
    }
}

// Alias kept so PantallaIncioSesion.kt compiles without changes
class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LoginViewModel(context) as T
}

// ── REGISTER ──────────────────────────────────────────────────────────────

sealed class RegisterUiState {
    object Idle    : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val mensaje: String) : RegisterUiState()
}

class RegisterViewModel(private val context: Context) : ViewModel() {

    private val repo = SmartFitRepository.getInstance(context)

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun registrar(nombre: String, email: String, password: String) {
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = RegisterUiState.Error("Rellena todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            val newId = repo.registrar(nombre, email, password)
            if (newId > 0) {
                _uiState.value = RegisterUiState.Success
            } else {
                _uiState.value = RegisterUiState.Error("El usuario o email ya existe")
            }
        }
    }

    fun resetState() { _uiState.value = RegisterUiState.Idle }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RegisterViewModel(context) as T
    }
}

// Alias kept so PantallaRegistro.kt compiles without changes
class RegisterViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        RegisterViewModel(context) as T
}
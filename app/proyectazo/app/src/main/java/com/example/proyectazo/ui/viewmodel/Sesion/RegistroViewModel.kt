package com.example.proyectazo.ui.viewmodel.Sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.UsuarioRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Sealed class representing the possible states of the registration screen.
 * Mirrors LoginUiState — same pattern for consistency across auth screens.
 */
sealed class RegisterUiState {
    object Idle    : RegisterUiState()  // Initial state — no action taken yet
    object Loading : RegisterUiState()  // Waiting for the API response — button is disabled
    object Success : RegisterUiState()  // Registration confirmed — screen navigates to Login
    data class Error(val mensaje: String) : RegisterUiState()  // Registration failed
}

/**
 * ViewModel for PantallaRegistro.
 * Does not require a Context — unlike LoginViewModel, no session data is stored
 * after registration. The user is redirected to Login to authenticate explicitly.
 * Validates fields locally before the API call to avoid unnecessary network requests.
 */
class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    /**
     * Attempts to register a new user.
     * Local validation runs first — empty fields are caught before the API call.
     * 400 means the username or email is already taken — the API returns this
     * when a duplicate is detected in the USUARIO table (email has a UNIQUE constraint).
     */
    fun registrar(nombre: String, email: String, password: String) {
        // Local validation — all three fields are required
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = RegisterUiState.Error("Rellena todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                val respuesta = RetrofitClient.instance.registro(
                    UsuarioRequest(nombre, email, password)
                )
                if (respuesta.isSuccessful) {
                    _uiState.value = RegisterUiState.Success
                } else if (respuesta.code() == 400) {
                    // 400 = duplicate username or email — maps to the UNIQUE constraint on email
                    _uiState.value = RegisterUiState.Error("El usuario o email ya existe")
                } else {
                    _uiState.value = RegisterUiState.Error("Error del servidor: ${respuesta.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error("Sin conexión con el servidor")
            }
        }
    }

    // Resets to Idle after the screen has consumed the Success or Error state
    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}

// No Context needed — RegisterViewModel has no dependency on SharedPreferences
class RegisterViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel() as T
    }
}
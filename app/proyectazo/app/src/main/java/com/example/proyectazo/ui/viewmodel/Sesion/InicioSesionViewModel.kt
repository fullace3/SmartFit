package com.example.proyectazo.ui.viewmodel.Sesion

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.LoginRequest
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Sealed class representing the possible states of the login screen.
 * Using a sealed class avoids multiple boolean flags (isLoading, isError, isSuccess)
 * and makes the when() expression in the composable exhaustive.
 */
sealed class LoginUiState {
    object Idle    : LoginUiState()  // Initial state — no action taken yet
    object Loading : LoginUiState()  // Waiting for the API response — button is disabled
    object Success : LoginUiState()  // Login confirmed — screen navigates away
    data class Error(val mensaje: String) : LoginUiState()  // Login failed — message shown
}

/**
 * ViewModel for PantallaIncioSesion.
 * Validates credentials locally before making the API call,
 * then stores the JWT token and user data in SessionManager on success.
 * The 401 case is handled separately to show a user-friendly message
 * instead of a generic server error.
 */
class LoginViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val sessionManager = SessionManager(context)

    /**
     * Attempts to log in with the provided credentials.
     * On success, fetches the user profile to get the display name,
     * then stores the full session (token + userId + name) in SharedPreferences.
     * On 401, shows a deliberately vague message — does not reveal which field is wrong.
     */
    fun login(nombre: String, password: String) {
        // Local validation — avoids a round trip for obviously empty fields
        if (nombre.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Rellena todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val respuesta = RetrofitClient.instance.login(LoginRequest(nombre, password))

                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val tokenResponse = respuesta.body()!!

                    // Fetch the user profile to get the display name for the session
                    val usuarioResponse = RetrofitClient.instance
                        .getUsuario(tokenResponse.id_usuario).body()

                    // Persist token, userId and name so all screens can access them
                    sessionManager.guardarSesion(
                        token  = tokenResponse.access_token,
                        userId = tokenResponse.id_usuario,
                        nombre = usuarioResponse?.nombre ?: "Usuario"
                    )
                    _uiState.value = LoginUiState.Success

                } else if (respuesta.code() == 401) {
                    // 401 = wrong credentials — intentionally vague to prevent user enumeration
                    _uiState.value = LoginUiState.Error("Usuario o contraseña incorrectos")
                } else {
                    _uiState.value = LoginUiState.Error("Error del servidor: ${respuesta.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Sin conexión con el servidor")
            }
        }
    }

    // Resets to Idle after the screen has consumed the Success or Error state
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

// Factory defined as a top-level class since LoginViewModel takes a Context parameter
class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(context) as T
    }
}
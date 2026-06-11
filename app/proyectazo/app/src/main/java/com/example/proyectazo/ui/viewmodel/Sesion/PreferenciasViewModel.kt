package com.example.proyectazo.ui.viewmodel.PerfilYAjustes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the preferences screen.
 * Only account deletion has async state here — notification time and
 * legal links are handled directly in the screen without ViewModel logic.
 */
data class PreferenciasUiState(
    val isLoading: Boolean = false,
    val cuentaEliminada: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for PreferenciasScreen.
 * Handles permanent account deletion — the most destructive action in the app.
 * On success, clears the local session so the user cannot navigate back
 * to any authenticated screen after the account is gone from the server.
 */
class PreferenciasViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val sessionManager = SessionManager(context)

    private val _uiState = MutableStateFlow(PreferenciasUiState())
    val uiState: StateFlow<PreferenciasUiState> = _uiState

    /**
     * Deletes the user account from the server and clears all local session data.
     * The server handles cascaded deletion of all user data (routines, diets, measurements).
     * Local SharedPreferences are cleared manually here because SessionManager.cerrarSesion()
     * only clears smartfit_session — this also removes smartfit_config entries for the user.
     * onEliminado() is called only on success so the NavGraph navigates to Login.
     */
    fun eliminarCuenta(onEliminado: () -> Unit) {
        val userId = sessionManager.getUserId()
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val resp = api.eliminarUsuario(userId)
                if (resp.isSuccessful) {
                    // Clear local session — user must not be able to navigate back after deletion
                    context.getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()
                    _uiState.update { it.copy(isLoading = false, cuentaEliminada = true) }
                    onEliminado()
                } else {
                    _uiState.update {
                        it.copy(isLoading = false,
                            error = "Error al eliminar la cuenta: ${resp.code()}")
                    }
                }
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
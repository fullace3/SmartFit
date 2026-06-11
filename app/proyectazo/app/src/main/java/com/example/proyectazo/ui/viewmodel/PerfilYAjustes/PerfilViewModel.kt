package com.example.proyectazo.ui.viewmodel.PerfilYAjustes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the profile screen.
 * Height is converted to metres and weight keeps one decimal place
 * so the UI can display them directly without formatting logic in the composable.
 * Default values are "-" so the screen shows a placeholder before data loads.
 */
data class PerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val alturaCm: String = "-",      // Formatted as "1.75 m" from the raw cm value
    val pesoInicial: String = "-",   // First recorded weight — not the most recent
    val objetivo: String = "-",
    val isLoading: Boolean = true
)

/**
 * ViewModel for PantallaPerfil.
 * Shared with EditarPerfilScreen via the same back stack entry so that
 * changes made in edit mode are reflected immediately on return.
 * Uses async/await to load user data and measurements concurrently.
 */
class PerfilViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val session = SessionManager(context)
    private val userId = session.getUserId()

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState

    init { recargar() }

    /**
     * Loads user profile and body measurements concurrently using async/await.
     * This halves the load time compared to sequential API calls.
     * pesoInicial uses the first measurement ever recorded (oldest by date),
     * while height uses the most recent one.
     */
    fun recargar() {
        viewModelScope.launch {
            try {
                // Launch both requests in parallel
                val usuarioDeferred = async { api.getUsuario(userId) }
                val medidasDeferred = async {
                    try { api.getMedidas(userId).body() ?: emptyList() }
                    catch (e: Exception) { emptyList() }
                }

                val usuario = usuarioDeferred.await().body()
                val medidas = medidasDeferred.await().sortedBy { it.fecha }

                // First measurement = initial weight, last measurement = current height
                val pesoInicial = medidas.firstOrNull()?.peso_kg
                val altura      = medidas.lastOrNull()?.altura_cm

                // Fitness goal is stored in SharedPreferences — no API endpoint for it
                val prefs   = context.getSharedPreferences("smartfit_session", Context.MODE_PRIVATE)
                val objetivo = prefs.getString("objetivo_usuario", "-") ?: "-"

                _uiState.value = PerfilUiState(
                    nombre      = usuario?.nombre ?: "",
                    email       = usuario?.email ?: "",
                    // Convert cm to metres for display (e.g. 175 → "1.75 m")
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

    // Delegates to SessionManager which clears smartfit_session SharedPreferences
    fun cerrarSesion() {
        session.cerrarSesion()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PerfilViewModel(context) as T
    }
}
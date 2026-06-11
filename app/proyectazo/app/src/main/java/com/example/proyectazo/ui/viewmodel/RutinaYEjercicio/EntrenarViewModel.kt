package com.example.proyectazo.ui.viewmodel.RutinaYEjercicio

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
 * Holds the previous session's performance for a single exercise.
 * Used as a reference display in EntrenarScreen so the user knows
 * what weight and reps they achieved last time.
 */
data class SeriePrevia(
    val peso: String,  // Formatted as a string for direct display — "-" if no history exists
    val reps: String
)

/**
 * UI state for EntrenarScreen.
 * previasPorEjercicio maps each ejercicioId to its most recent logged performance.
 * The map is keyed by ID so the screen can look up any exercise in O(1).
 */
data class EntrenarUiState(
    val previasPorEjercicio: Map<Int, SeriePrevia> = emptyMap(),
    val isLoading: Boolean = true
)

/**
 * ViewModel for EntrenarScreen.
 * Responsible only for loading the previous session data —
 * the live workout state (sets, reps, weight) is managed directly
 * in the composable since it does not need to survive recomposition beyond the session.
 */
class EntrenarViewModel(private val context: Context) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(EntrenarUiState())
    val uiState: StateFlow<EntrenarUiState> = _uiState

    /**
     * Loads the full training history and extracts the most recent entry
     * for each exercise in the current routine.
     * Uses a single API call for all exercises to avoid N separate requests.
     * Exercises with no history get a placeholder SeriePrevia("-", "-").
     */
    fun cargarPrevias(ejercicioIds: List<Int>) {
        viewModelScope.launch {
            try {
                val resp = api.getHistorial(userId)
                if (resp.isSuccessful) {
                    val historial = resp.body() ?: emptyList()

                    // For each exercise, find the most recent entry by date
                    val previas = ejercicioIds.associateWith { id ->
                        val entrada = historial
                            .filter { it.id_ejercicio == id }
                            .maxByOrNull { it.fecha }  // Most recent by ISO date string

                        if (entrada != null) {
                            SeriePrevia(
                                peso = "${entrada.peso_kg}",
                                reps = "${entrada.repeticiones}"
                            )
                        } else {
                            SeriePrevia(peso = "-", reps = "-")  // No history for this exercise
                        }
                    }
                    _uiState.update { it.copy(previasPorEjercicio = previas, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EntrenarViewModel(context) as T
    }
}
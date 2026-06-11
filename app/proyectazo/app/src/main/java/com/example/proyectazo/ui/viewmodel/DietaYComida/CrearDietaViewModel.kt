package com.example.proyectazo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.network.DietaComidaRequest
import com.example.proyectazo.network.DietaRequest
import com.example.proyectazo.network.RetrofitClient
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Represents a single food item added to a diet plan.
 * uid is generated from System.nanoTime() to uniquely identify each entry
 * even if the same food is added multiple times to different days.
 * dietaComidaId is only set for items loaded from the API (edit mode) —
 * null means the item is new and needs to be POSTed.
 */
data class AlimentoItem(
    val id: Int,
    val nombre: String,
    val calorias: Int,
    val proteinas: Int = 0,
    val carbohidratos: Int = 0,
    val grasas: Int = 0,
    val dia: String? = null,
    val tipo: String = "Desayuno",
    val uid: Long = System.nanoTime(),     // Local unique key — not persisted in the database
    val dietaComidaId: Int? = null         // ID in DIETA_COMIDA table — null for new items
)

/**
 * UI state for the diet creation/editing screen.
 * Macro totals are computed properties derived from the food list —
 * they update automatically whenever alimentos changes.
 */
data class CrearDietaUiState(
    val nombreDieta: String = "",
    val objetivo: String = "",
    val alimentos: List<AlimentoItem> = emptyList(),
    val isLoading: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null,
    val editando: Boolean = false,    // True when editing an existing diet, false when creating
    val dietaId: Int? = null
) {
    // Computed totals — recalculated on every recomposition from the current food list
    val proteinas: Int get() = alimentos.sumOf { it.proteinas }
    val carbohidratos: Int get() = alimentos.sumOf { it.carbohidratos }
    val grasas: Int get() = alimentos.sumOf { it.grasas }
    val calorasTotales: Int get() = alimentos.sumOf { it.calorias }
}

/**
 * ViewModel for CrearDietaScreen — handles both creation and editing of diet plans.
 * When dietaIdEditar is provided, the ViewModel loads the existing diet and switches
 * to edit mode, where save performs a PUT instead of a POST.
 */
class CrearDietaViewModel(
    private val context: Context,
    private val dietaIdEditar: Int? = null
) : ViewModel() {

    private val api = RetrofitClient.instance
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(CrearDietaUiState())
    val uiState: StateFlow<CrearDietaUiState> = _uiState

    // Snapshot of the original food list loaded from the API.
    // Used in edit mode to detect which items were removed by the user.
    private var alimentosOriginales: List<AlimentoItem> = emptyList()

    init {
        // Load existing diet data immediately if in edit mode
        if (dietaIdEditar != null) cargarDietaExistente(dietaIdEditar)
    }

    /**
     * Loads an existing diet and its food items from the API.
     * Populates alimentosOriginales so deleted items can be detected on save.
     */
    private fun cargarDietaExistente(dietaId: Int) {
        _uiState.update { it.copy(isLoading = true, editando = true, dietaId = dietaId) }
        viewModelScope.launch {
            try {
                // Load diet metadata to get the name
                val dietasResp = api.getDietasUsuario(userId)
                val dieta = dietasResp.body()?.find { it.id_dieta == dietaId }
                if (dieta != null) {
                    _uiState.update { it.copy(nombreDieta = dieta.nombre) }
                }

                // Load the food items linked to this diet
                val comidasResp = api.getComidasDeDieta(dietaId)
                if (comidasResp.isSuccessful) {
                    val dietaComidas = comidasResp.body() ?: emptyList()
                    val alimentos = dietaComidas.mapNotNull { dc ->
                        val comida = dc.comida
                        if (comida != null) {
                            AlimentoItem(
                                id = comida.id_comida,
                                nombre = comida.nombre,
                                calorias = comida.calorias_100g,
                                proteinas = comida.proteinas_100g,
                                carbohidratos = comida.carbohidratos_100g,
                                grasas = comida.grasas_100g,
                                dia = dc.dia,
                                tipo = dc.tipo,
                                dietaComidaId = dc.id  // Store the junction table ID for deletion
                            )
                        } else null
                    }
                    alimentosOriginales = alimentos  // Snapshot for edit comparison
                    _uiState.update { it.copy(alimentos = alimentos, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNombreDietaChange(v: String) = _uiState.update { it.copy(nombreDieta = v) }
    fun onObjetivoChange(v: String) = _uiState.update { it.copy(objetivo = v) }

    // Append a new food item to the list — uid ensures uniqueness even for duplicates
    fun agregarAlimento(alimento: AlimentoItem) {
        _uiState.update { it.copy(alimentos = it.alimentos + alimento) }
    }

    // Remove a food item by its local uid — not by id, since the same food can appear multiple times
    fun eliminarAlimento(uid: Long) {
        _uiState.update { it.copy(alimentos = it.alimentos.filter { a -> a.uid != uid }) }
    }

    /**
     * Routes to the correct save strategy based on whether we are creating or editing.
     */
    fun guardar(onExitoso: () -> Unit) {
        val state = _uiState.value
        if (state.editando && state.dietaId != null) {
            guardarEdicion(state, state.dietaId, onExitoso)
        } else {
            crearNueva(state, onExitoso)
        }
    }

    /**
     * Creates a new diet via POST, then links each food item to it via DIETA_COMIDA.
     * Macro totals are calculated from the food list at the moment of saving.
     */
    private fun crearNueva(state: CrearDietaUiState, onExitoso: () -> Unit) {
        val nombre = state.nombreDieta.ifBlank { "Nueva dieta" }
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val ahora = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val request = DietaRequest(
                    nombre = nombre,
                    objetivo_calorico = state.calorasTotales,
                    proteinas_g = state.proteinas.toDouble(),
                    carbohidratos_g = state.carbohidratos.toDouble(),
                    grasas_g = state.grasas.toDouble(),
                    fecha_inicio = ahora,
                    id_usuario = userId
                )
                val resp = api.crearDieta(request)
                if (resp.isSuccessful) {
                    val dietaCreada = resp.body()
                    if (dietaCreada != null) {
                        // Link each food to the newly created diet
                        state.alimentos.forEach { alimento ->
                            api.añadirComidaADieta(
                                DietaComidaRequest(
                                    id_dieta = dietaCreada.id_dieta,
                                    id_comida = alimento.id,
                                    tipo = alimento.tipo,
                                    dia = alimento.dia ?: "Lun"
                                )
                            )
                        }
                    }
                    _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
                    onExitoso()
                } else {
                    val errorBody = resp.errorBody()?.string() ?: "Error ${resp.code()}"
                    _uiState.update { it.copy(isLoading = false, error = errorBody) }
                    android.widget.Toast.makeText(context, "Error: $errorBody", android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Updates an existing diet using a three-step strategy:
     * 1. PUT the diet metadata (name, macros).
     * 2. DELETE food items that were in the original list but are no longer in the current list.
     * 3. POST food items that are new (no dietaComidaId means they were added in this session).
     */
    private fun guardarEdicion(state: CrearDietaUiState, dietaId: Int, onExitoso: () -> Unit) {
        val nombre = state.nombreDieta.ifBlank { "Nueva dieta" }
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // Step 1 — update diet metadata
                val request = DietaRequest(
                    nombre = nombre,
                    objetivo_calorico = state.calorasTotales,
                    proteinas_g = state.proteinas.toDouble(),
                    carbohidratos_g = state.carbohidratos.toDouble(),
                    grasas_g = state.grasas.toDouble(),
                    fecha_inicio = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    id_usuario = userId
                )
                val respActualizar = api.actualizarDieta(dietaId, request)
                if (!respActualizar.isSuccessful) {
                    val errorBody = respActualizar.errorBody()?.string() ?: "Error ${respActualizar.code()}"
                    _uiState.update { it.copy(isLoading = false, error = errorBody) }
                    android.widget.Toast.makeText(context, "Error al actualizar: $errorBody", android.widget.Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Step 2 — delete removed items by comparing uids against the original snapshot
                val uidsActuales = state.alimentos.map { it.uid }.toSet()
                val alimentosEliminados = alimentosOriginales.filter { it.uid !in uidsActuales }
                alimentosEliminados.forEach { eliminado ->
                    if (eliminado.dietaComidaId != null) {
                        try {
                            api.eliminarComidaDeDieta(eliminado.dietaComidaId)
                        } catch (_: Exception) { /* ignore individual failures */ }
                    }
                }

                // Step 3 — add new items (those without a dietaComidaId were added in this session)
                val alimentosNuevos = state.alimentos.filter { it.dietaComidaId == null }
                alimentosNuevos.forEach { alimento ->
                    try {
                        api.añadirComidaADieta(
                            DietaComidaRequest(
                                id_dieta = dietaId,
                                id_comida = alimento.id,
                                tipo = alimento.tipo,
                                dia = alimento.dia ?: "Lun"
                            )
                        )
                    } catch (_: Exception) { /* ignore individual failures */ }
                }

                _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
                onExitoso()

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Factory that passes the optional dietaId to the ViewModel.
     * null = create mode, non-null = edit mode.
     */
    class Factory(private val context: Context, private val dietaId: Int? = null) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CrearDietaViewModel(context, dietaId) as T
    }
}
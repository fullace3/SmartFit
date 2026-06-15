package com.example.proyectazo.ui.viewmodel.RutinaYEjercicio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.data.local.entity.EjercicioEntity
import com.example.proyectazo.data.local.entity.RutinaEntity
import com.example.proyectazo.data.repository.SmartFitRepository
import com.example.proyectazo.network.SessionManager
import com.example.proyectazo.ui.screens.RutinasYEjercicio.ResultadoEntrenamiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FiltroTipo { MUSCULO, EQUIPAMIENTO }

data class EjercicioRutina(
    val id: Int,
    val nombre: String,
    val series: Int = 3,
    val repeticiones: Int = 10,
    val imagenUrl: String = ""
)

data class RutinaConEjercicios(
    val rutina: RutinaEntity,
    val ejercicios: List<EjercicioEntity>
)

// ── RUTINAS LIST ──────────────────────────────────────────────────────────────

sealed class RutinasUiState {
    object Cargando : RutinasUiState()
    data class Exito(val rutinas: List<RutinaConEjercicios>) : RutinasUiState()
    object Vacio    : RutinasUiState()
    data class Error(val mensaje: String) : RutinasUiState()
}

class RutinasViewModel(context: Context) : ViewModel() {

    private val repo   = SmartFitRepository.getInstance(context)
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow<RutinasUiState>(RutinasUiState.Cargando)
    val uiState: StateFlow<RutinasUiState> = _uiState

    init { cargarRutinas() }

    fun cargarRutinas() {
        viewModelScope.launch {
            _uiState.value = RutinasUiState.Cargando
            try {
                val rutinas = repo.getRutinas(userId)
                if (rutinas.isEmpty()) {
                    _uiState.value = RutinasUiState.Vacio
                    return@launch
                }
                val lista = rutinas.map { rutina ->
                    RutinaConEjercicios(
                        rutina     = rutina,
                        ejercicios = repo.getEjerciciosDeRutina(rutina.id_rutina)
                    )
                }
                _uiState.value = RutinasUiState.Exito(lista)
            } catch (e: Exception) {
                _uiState.value = RutinasUiState.Error("Error al cargar las rutinas")
            }
        }
    }

    fun eliminarRutina(rutinaId: Int) {
        viewModelScope.launch {
            try {
                repo.borrarRutina(rutinaId)
                cargarRutinas()
            } catch (e: Exception) {
                _uiState.value = RutinasUiState.Error("Error al eliminar la rutina")
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RutinasViewModel(context) as T
    }
}

// ── CREAR RUTINA ──────────────────────────────────────────────────────────────

sealed class CrearRutinaUiState {
    object Idle    : CrearRutinaUiState()
    object Loading : CrearRutinaUiState()
    data class RutinaCreada(val rutinaId: Int) : CrearRutinaUiState()
    data class Error(val mensaje: String) : CrearRutinaUiState()
}

class RutinaViewModel(private val context: Context) : ViewModel() {

    private val repo   = SmartFitRepository.getInstance(context)
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow<CrearRutinaUiState>(CrearRutinaUiState.Idle)
    val uiState: StateFlow<CrearRutinaUiState> = _uiState.asStateFlow()

    private var rutinaIdCreada: Int? = null

    fun crearONavegar(nombre: String) {
        val idExistente = rutinaIdCreada
        if (idExistente != null) {
            _uiState.value = CrearRutinaUiState.RutinaCreada(idExistente)
            return
        }
        if (nombre.isBlank()) {
            _uiState.value = CrearRutinaUiState.Error("Escribe un nombre antes de añadir ejercicios")
            return
        }
        viewModelScope.launch {
            _uiState.value = CrearRutinaUiState.Loading
            try {
                val id = repo.crearRutina(nombre, userId).toInt()
                rutinaIdCreada = id
                _uiState.value = CrearRutinaUiState.RutinaCreada(id)
            } catch (e: Exception) {
                _uiState.value = CrearRutinaUiState.Error("Error al crear la rutina")
            }
        }
    }

    fun resetState() { _uiState.value = CrearRutinaUiState.Idle }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RutinaViewModel(context) as T
    }
}

// ── EDITAR RUTINA ─────────────────────────────────────────────────────────────

data class EditarRutinaUiState(
    val nombre: String = "",
    val ejercicios: List<EjercicioRutina> = emptyList(),
    val isLoading: Boolean = true,
    val guardado: Boolean = false,
    val error: String? = null
)

class EditarRutinaViewModel(
    private val rutinaId: Int,
    private val context: Context
) : ViewModel() {

    private val repo   = SmartFitRepository.getInstance(context)
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(EditarRutinaUiState())
    val uiState: StateFlow<EditarRutinaUiState> = _uiState.asStateFlow()

    init { cargarRutina() }

    fun cargarRutina() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val rutinas    = repo.getRutinas(userId)
                val rutina     = rutinas.find { it.id_rutina == rutinaId }
                val relaciones = repo.getRelacionesDeRutina(rutinaId)
                val ejercicios = relaciones.mapNotNull { rel ->
                    repo.getEjercicio(rel.id_ejercicio)?.let { ej ->
                        EjercicioRutina(
                            id           = ej.id_ejercicio,
                            nombre       = ej.nombre,
                            series       = rel.series,
                            repeticiones = rel.repeticiones,
                            imagenUrl    = ej.imagen ?: ""
                        )
                    }
                }
                _uiState.update {
                    it.copy(isLoading = false, nombre = rutina?.nombre ?: "", ejercicios = ejercicios)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar la rutina") }
            }
        }
    }

    fun onNombreChange(nombre: String) = _uiState.update { it.copy(nombre = nombre) }

    fun eliminarEjercicio(ejercicioId: Int) {
        viewModelScope.launch {
            try {
                repo.quitarEjercicioDeRutina(rutinaId, ejercicioId)
                _uiState.update { state ->
                    state.copy(ejercicios = state.ejercicios.filter { it.id != ejercicioId })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al eliminar el ejercicio") }
            }
        }
    }

    fun guardarCambios() {
        val nombre = _uiState.value.nombre
        if (nombre.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }
        viewModelScope.launch {
            try {
                repo.editarRutina(rutinaId, nombre)
                _uiState.update { it.copy(guardado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al guardar los cambios") }
            }
        }
    }

    fun onGuardadoConsumed() = _uiState.update { it.copy(guardado = false) }

    class Factory(private val rutinaId: Int, private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditarRutinaViewModel(rutinaId, context) as T
    }
}

// ── AÑADIR EJERCICIO ──────────────────────────────────────────────────────────

data class AñadirEjercicioUiState(
    val ejercicios: List<EjercicioEntity> = emptyList(),
    val ejerciciosFiltrados: List<EjercicioEntity> = emptyList(),
    val filtrosDisponibles: List<String> = emptyList(),
    val filtroTipo: FiltroTipo = FiltroTipo.MUSCULO,
    val filtroValor: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val ejercicioAgregado: Boolean = false
)

class AñadirEjercicioViewModel(
    private val rutinaId: Int,
    private val context: Context
) : ViewModel() {

    private val repo = SmartFitRepository.getInstance(context)

    private val _uiState = MutableStateFlow(AñadirEjercicioUiState())
    val uiState: StateFlow<AñadirEjercicioUiState> = _uiState.asStateFlow()

    init { cargarEjercicios() }

    private fun cargarEjercicios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val lista = repo.getEjercicios()
                _uiState.update { state ->
                    state.copy(
                        isLoading          = false,
                        ejercicios         = lista,
                        filtrosDisponibles = lista.mapNotNull { it.grupo_muscular }.distinct().sorted()
                    ).aplicarFiltros()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "No se pudieron cargar los ejercicios") }
            }
        }
    }

    fun onSearchQueryChange(query: String) =
        _uiState.update { it.copy(searchQuery = query).aplicarFiltros() }

    fun onFiltroTipoChange(tipo: FiltroTipo) {
        _uiState.update { state ->
            val subFiltros = state.ejercicios
                .mapNotNull { if (tipo == FiltroTipo.MUSCULO) it.grupo_muscular else it.equipamiento }
                .distinct().sorted()
            state.copy(filtroTipo = tipo, filtrosDisponibles = subFiltros, filtroValor = null)
                .aplicarFiltros()
        }
    }

    fun onFiltroValorChange(valor: String?) =
        _uiState.update { it.copy(filtroValor = valor).aplicarFiltros() }

    fun onEjercicioSeleccionado(ejercicio: EjercicioEntity) {
        viewModelScope.launch {
            try {
                val orden = (_uiState.value.ejerciciosFiltrados.indexOf(ejercicio) + 1).coerceAtLeast(1)
                repo.añadirEjercicioARutina(rutinaId, ejercicio.id_ejercicio, orden)
                _uiState.update { it.copy(ejercicioAgregado = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al agregar el ejercicio") }
            }
        }
    }

    fun onEjercicioAgregadoConsumed() = _uiState.update { it.copy(ejercicioAgregado = false) }

    private fun AñadirEjercicioUiState.aplicarFiltros(): AñadirEjercicioUiState {
        val filtrados = ejercicios.filter { ej ->
            val coincideBusqueda = searchQuery.isBlank() || ej.nombre.contains(searchQuery, ignoreCase = true)
            val coincideFiltro   = filtroValor == null || when (filtroTipo) {
                FiltroTipo.MUSCULO      -> ej.grupo_muscular == filtroValor
                FiltroTipo.EQUIPAMIENTO -> ej.equipamiento   == filtroValor
            }
            coincideBusqueda && coincideFiltro
        }
        return copy(ejerciciosFiltrados = filtrados)
    }

    class Factory(private val rutinaId: Int, private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AñadirEjercicioViewModel(rutinaId, context) as T
    }
}

// ── ENTRENAR ──────────────────────────────────────────────────────────────────

data class SeriePrevia(val peso: String, val reps: String)

data class EntrenarUiState(
    val previasPorEjercicio: Map<Int, SeriePrevia> = emptyMap(),
    val isLoading: Boolean = true
)

class EntrenarViewModel(private val context: Context) : ViewModel() {

    private val repo   = SmartFitRepository.getInstance(context)
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(EntrenarUiState())
    val uiState: StateFlow<EntrenarUiState> = _uiState

    fun cargarPrevias(ejercicioIds: List<Int>) {
        viewModelScope.launch {
            try {
                val historial = repo.getHistorial(userId)
                val previas = ejercicioIds.associateWith { id ->
                    val entrada = historial.filter { it.id_ejercicio == id }.maxByOrNull { it.fecha }
                    if (entrada != null) SeriePrevia("${entrada.peso_kg}", "${entrada.repeticiones}")
                    else SeriePrevia("-", "-")
                }
                _uiState.update { it.copy(previasPorEjercicio = previas, isLoading = false) }
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

// ── FINALIZAR ENTRENAMIENTO ───────────────────────────────────────────────────

sealed class GuardarUiState {
    object Idle      : GuardarUiState()
    object Guardando : GuardarUiState()
    object Guardado  : GuardarUiState()
    data class Error(val msg: String) : GuardarUiState()
}

class FinalizarEntrenamientoViewModel(private val context: Context) : ViewModel() {

    private val repo   = SmartFitRepository.getInstance(context)
    private val userId = SessionManager(context).getUserId()

    private val _state = MutableStateFlow<GuardarUiState>(GuardarUiState.Idle)
    val state: StateFlow<GuardarUiState> = _state

    fun guardarEntrenamiento(resultado: ResultadoEntrenamiento) {
        viewModelScope.launch {
            _state.value = GuardarUiState.Guardando
            try {
                resultado.ejercicios.forEach { ejercicioRes ->
                    val seriesCompletadas = ejercicioRes.series.filter { it.completada }
                    if (seriesCompletadas.isNotEmpty()) {
                        val pesoPromedio = seriesCompletadas
                            .mapNotNull { it.peso.toDoubleOrNull() }
                            .average().takeIf { !it.isNaN() } ?: 0.0
                        val repsPromedio = seriesCompletadas
                            .mapNotNull { it.reps.toIntOrNull() }
                            .average().toInt().coerceAtLeast(1)
                        val nombreEj = repo.getEjercicio(ejercicioRes.id)?.nombre ?: ""
                        repo.registrarHistorial(
                            userId          = userId,
                            ejercicioId     = ejercicioRes.id,
                            rutinaId        = resultado.rutinaId,
                            pesoKg          = pesoPromedio,
                            repeticiones    = repsPromedio,
                            series          = seriesCompletadas.size,
                            duracionMin     = (resultado.tiempoSegundos / 60).coerceAtLeast(1),
                            nombreEjercicio = nombreEj
                        )
                    }
                }
                _state.value = GuardarUiState.Guardado
            } catch (e: Exception) {
                _state.value = GuardarUiState.Error("Error al guardar: ${e.message}")
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FinalizarEntrenamientoViewModel(context) as T
    }
}

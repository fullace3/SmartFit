package com.example.proyectazo.ui.viewmodel.DietaYComida

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyectazo.data.local.entity.DietaEntity
import com.example.proyectazo.data.repository.SmartFitRepository
import com.example.proyectazo.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ── LISTA DE DIETAS ───────────────────────────────────────────────────────────

data class DietaListItem(
    val id: Int,
    val nombre: String,
    val calorias: Int,
    val proteinas: Double,
    val carbohidratos: Double,
    val grasas: Double,
    val activo: Boolean
)

data class DietaUiState(
    val dietas: List<DietaListItem> = emptyList(),
    val isLoading: Boolean = true
)

class DietaViewModel(context: Context) : ViewModel() {

    private val repo   = SmartFitRepository.getInstance(context)
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(DietaUiState())
    val uiState: StateFlow<DietaUiState> = _uiState

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val lista = repo.getDietas(userId).map {
                    DietaListItem(
                        id            = it.id_dieta,
                        nombre        = it.nombre,
                        calorias      = it.objetivo_calorico,
                        proteinas     = it.proteinas_g,
                        carbohidratos = it.carbohidratos_g,
                        grasas        = it.grasas_g,
                        activo        = it.activo
                    )
                }
                _uiState.update { it.copy(dietas = lista, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun seleccionarDieta(dietaId: Int) {
        viewModelScope.launch {
            try {
                repo.activarDieta(dietaId, userId)
                cargar()
            } catch (_: Exception) {}
        }
    }

    fun borrarDieta(dietaId: Int) {
        viewModelScope.launch {
            try {
                repo.borrarDieta(dietaId)
                cargar()
            } catch (_: Exception) {}
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DietaViewModel(context) as T
    }
}

// ── LISTA DE COMIDAS ──────────────────────────────────────────────────────────

data class ComidaListItem(
    val id: Int,
    val nombre: String,
    val calorias100g: Int,
    val proteinas100g: Int,
    val carbohidratos100g: Int,
    val grasas100g: Int,
    val imagen: String? = null
)

data class ListaComidasUiState(
    val comidas: List<ComidaListItem> = emptyList(),
    val busqueda: String = "",
    val filtroActivo: String? = null,
    val isLoading: Boolean = true
) {
    val comidasFiltradas: List<ComidaListItem>
        get() {
            var lista = if (busqueda.isBlank()) comidas
                        else comidas.filter { it.nombre.contains(busqueda, ignoreCase = true) }
            lista = when (filtroActivo) {
                "Proteinas"         -> lista.sortedByDescending { it.proteinas100g }
                "Carbohidratos"     -> lista.sortedByDescending { it.carbohidratos100g }
                "Grasas saludables" -> lista.sortedByDescending { it.grasas100g }
                else                -> lista
            }
            return lista
        }
}

class ListaComidasViewModel(context: Context) : ViewModel() {

    private val repo = SmartFitRepository.getInstance(context)

    private val _uiState = MutableStateFlow(ListaComidasUiState())
    val uiState: StateFlow<ListaComidasUiState> = _uiState

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                val lista = repo.getComidas().map {
                    ComidaListItem(
                        id                = it.id_comida,
                        nombre            = it.nombre,
                        calorias100g      = it.calorias_100g,
                        proteinas100g     = it.proteinas_100g,
                        carbohidratos100g = it.carbohidratos_100g,
                        grasas100g        = it.grasas_100g,
                        imagen            = it.imagen
                    )
                }
                _uiState.update { it.copy(comidas = lista, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onBusquedaChange(v: String) = _uiState.update { it.copy(busqueda = v) }

    fun onFiltroChange(filtro: String) {
        _uiState.update { it.copy(filtroActivo = if (it.filtroActivo == filtro) null else filtro) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ListaComidasViewModel(context) as T
    }
}

// ── DETALLE COMIDA ────────────────────────────────────────────────────────────

data class DetalleComidaUiState(
    val nombre: String = "",
    val calorias100g: Int = 0,
    val proteinas100g: Int = 0,
    val carbohidratos100g: Int = 0,
    val grasas100g: Int = 0,
    val imagen: String? = null,
    val isLoading: Boolean = true
)

class DetalleComidaViewModel(context: Context, private val comidaId: Int) : ViewModel() {

    private val repo = SmartFitRepository.getInstance(context)

    private val _uiState = MutableStateFlow(DetalleComidaUiState())
    val uiState: StateFlow<DetalleComidaUiState> = _uiState

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            try {
                val comida = repo.getComida(comidaId)
                if (comida != null) {
                    _uiState.update {
                        it.copy(
                            nombre            = comida.nombre,
                            calorias100g      = comida.calorias_100g,
                            proteinas100g     = comida.proteinas_100g,
                            carbohidratos100g = comida.carbohidratos_100g,
                            grasas100g        = comida.grasas_100g,
                            imagen            = comida.imagen,
                            isLoading         = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    class Factory(private val context: Context, private val comidaId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetalleComidaViewModel(context, comidaId) as T
    }
}

// ── CREAR / EDITAR DIETA ──────────────────────────────────────────────────────

data class AlimentoItem(
    val id: Int,
    val nombre: String,
    val calorias: Int,
    val proteinas: Int = 0,
    val carbohidratos: Int = 0,
    val grasas: Int = 0,
    val dia: String? = null,
    val tipo: String = "Desayuno",
    val uid: Long = System.nanoTime(),
    val dietaComidaId: Int? = null
)

data class CrearDietaUiState(
    val nombreDieta: String = "",
    val objetivo: String = "",
    val alimentos: List<AlimentoItem> = emptyList(),
    val isLoading: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null,
    val editando: Boolean = false,
    val dietaId: Int? = null
) {
    val proteinas: Int        get() = alimentos.sumOf { it.proteinas }
    val carbohidratos: Int    get() = alimentos.sumOf { it.carbohidratos }
    val grasas: Int           get() = alimentos.sumOf { it.grasas }
    val caloriasTotal: Int    get() = alimentos.sumOf { it.calorias }
}

class CrearDietaViewModel(
    private val context: Context,
    private val dietaIdEditar: Int? = null
) : ViewModel() {

    private val repo   = SmartFitRepository.getInstance(context)
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(CrearDietaUiState())
    val uiState: StateFlow<CrearDietaUiState> = _uiState

    private var alimentosOriginales: List<AlimentoItem> = emptyList()

    init { if (dietaIdEditar != null) cargarDietaExistente(dietaIdEditar) }

    private fun cargarDietaExistente(dietaId: Int) {
        _uiState.update { it.copy(isLoading = true, editando = true, dietaId = dietaId) }
        viewModelScope.launch {
            try {
                val dieta = repo.getDietas(userId).find { it.id_dieta == dietaId }
                if (dieta != null) _uiState.update { it.copy(nombreDieta = dieta.nombre) }

                val comidasDieta = repo.getComidasDeDieta(dietaId)
                val alimentos = comidasDieta.map { dc ->
                    AlimentoItem(
                        id            = dc.id_comida,
                        nombre        = dc.nombre,
                        calorias      = dc.calorias_100g,
                        proteinas     = dc.proteinas_100g,
                        carbohidratos = dc.carbohidratos_100g,
                        grasas        = dc.grasas_100g,
                        dia           = dc.dia,
                        tipo          = dc.tipo,
                        dietaComidaId = dc.id
                    )
                }
                alimentosOriginales = alimentos
                _uiState.update { it.copy(alimentos = alimentos, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNombreDietaChange(v: String) = _uiState.update { it.copy(nombreDieta = v) }
    fun onObjetivoChange(v: String)    = _uiState.update { it.copy(objetivo = v) }

    fun agregarAlimento(alimento: AlimentoItem) =
        _uiState.update { it.copy(alimentos = it.alimentos + alimento) }

    fun eliminarAlimento(uid: Long) =
        _uiState.update { it.copy(alimentos = it.alimentos.filter { a -> a.uid != uid }) }

    fun guardar(onExitoso: () -> Unit) {
        val state = _uiState.value
        if (state.editando && state.dietaId != null) guardarEdicion(state, state.dietaId, onExitoso)
        else crearNueva(state, onExitoso)
    }

    private fun crearNueva(state: CrearDietaUiState, onExitoso: () -> Unit) {
        val nombre = state.nombreDieta.ifBlank { "Nueva dieta" }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val dietaId = repo.crearDieta(
                    nombre      = nombre,
                    caloriasObj = state.caloriasTotal,
                    proteinasG  = state.proteinas.toDouble(),
                    carbsG      = state.carbohidratos.toDouble(),
                    grasasG     = state.grasas.toDouble(),
                    userId      = userId
                ).toInt()
                state.alimentos.forEach { alimento ->
                    repo.añadirComidaADieta(
                        dietaId  = dietaId,
                        comidaId = alimento.id,
                        tipo     = alimento.tipo,
                        dia      = alimento.dia ?: "Lun"
                    )
                }
                _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
                onExitoso()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun guardarEdicion(state: CrearDietaUiState, dietaId: Int, onExitoso: () -> Unit) {
        val nombre = state.nombreDieta.ifBlank { "Nueva dieta" }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                repo.editarDieta(
                    dietaId     = dietaId,
                    nombre      = nombre,
                    caloriasObj = state.caloriasTotal,
                    proteinasG  = state.proteinas.toDouble(),
                    carbsG      = state.carbohidratos.toDouble(),
                    grasasG     = state.grasas.toDouble()
                )
                val uidsActuales = state.alimentos.map { it.uid }.toSet()
                alimentosOriginales.filter { it.uid !in uidsActuales }.forEach { eliminado ->
                    if (eliminado.dietaComidaId != null) {
                        try { repo.eliminarComidaDeDieta(eliminado.dietaComidaId) } catch (_: Exception) {}
                    }
                }
                state.alimentos.filter { it.dietaComidaId == null }.forEach { alimento ->
                    try {
                        repo.añadirComidaADieta(dietaId, alimento.id, alimento.tipo, alimento.dia ?: "Lun")
                    } catch (_: Exception) {}
                }
                _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
                onExitoso()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    class Factory(private val context: Context, private val dietaId: Int? = null) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CrearDietaViewModel(context, dietaId) as T
    }
}

// ── DIETA ACTIVA (NUEVO — resuelve TODO calorías hardcodeadas) ────────────────

data class DietaActivaUiState(
    val dieta: DietaEntity? = null,
    val caloriasConsumidas: Int = 0,
    val proteinasConsumidas: Double = 0.0,
    val carbsConsumidos: Double = 0.0,
    val grasasConsumidas: Double = 0.0,
    val isLoading: Boolean = true,
    val sinDieta: Boolean = false
)

class DietaActivaViewModel(context: Context) : ViewModel() {

    private val repo   = SmartFitRepository.getInstance(context)
    private val userId = SessionManager(context).getUserId()

    private val _uiState = MutableStateFlow(DietaActivaUiState())
    val uiState: StateFlow<DietaActivaUiState> = _uiState

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val dieta = repo.getDietaActiva(userId)
                if (dieta == null) {
                    _uiState.update { it.copy(isLoading = false, sinDieta = true) }
                    return@launch
                }
                val comidas = repo.getComidasDeDieta(dieta.id_dieta)
                _uiState.update {
                    it.copy(
                        dieta              = dieta,
                        caloriasConsumidas = comidas.sumOf { c -> c.calorias_100g },
                        proteinasConsumidas= comidas.sumOf { c -> c.proteinas_100g }.toDouble(),
                        carbsConsumidos    = comidas.sumOf { c -> c.carbohidratos_100g }.toDouble(),
                        grasasConsumidas   = comidas.sumOf { c -> c.grasas_100g }.toDouble(),
                        isLoading          = false,
                        sinDieta           = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DietaActivaViewModel(context) as T
    }
}

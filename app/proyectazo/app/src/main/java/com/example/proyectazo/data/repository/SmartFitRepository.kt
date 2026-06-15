package com.example.proyectazo.data.repository

import android.content.Context
import com.example.proyectazo.data.local.SmartFitDatabase.SmartFitDatabase
import com.example.proyectazo.data.local.entity.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SmartFitRepository private constructor(context: Context) {

    private val db = SmartFitDatabase.getInstance(context)

    // ── AUTH ──────────────────────────────────────────────────────────────────

    suspend fun login(nombre: String, password: String): UsuarioEntity? =
        db.usuarioDao().login(nombre, password)

    suspend fun registrar(nombre: String, email: String, password: String): Long {
        if (db.usuarioDao().existeNombreOEmail(nombre, email) > 0) return -1L
        return db.usuarioDao().insertar(
            UsuarioEntity(nombre = nombre, email = email, password_hash = password)
        )
    }

    // ── USUARIO ───────────────────────────────────────────────────────────────

    suspend fun getUsuario(userId: Int): UsuarioEntity? =
        db.usuarioDao().getById(userId)

    suspend fun eliminarCuenta(userId: Int) =
        db.usuarioDao().eliminar(userId)

    // ── EJERCICIOS ────────────────────────────────────────────────────────────

    suspend fun getEjercicios(): List<EjercicioEntity> =
        db.ejercicioDao().getAll()

    suspend fun getEjercicio(id: Int): EjercicioEntity? =
        db.ejercicioDao().getById(id)

    // ── RUTINAS ───────────────────────────────────────────────────────────────

    suspend fun getRutinas(userId: Int): List<RutinaEntity> =
        db.rutinaDao().getByUsuario(userId)

    suspend fun crearRutina(nombre: String, userId: Int): Long =
        db.rutinaDao().insertar(RutinaEntity(nombre = nombre, id_usuario = userId))

    suspend fun editarRutina(rutinaId: Int, nombre: String) =
        db.rutinaDao().updateNombre(rutinaId, nombre)

    suspend fun borrarRutina(rutinaId: Int) =
        db.rutinaDao().eliminar(rutinaId)

    // ── RUTINA-EJERCICIO ──────────────────────────────────────────────────────

    suspend fun getEjerciciosDeRutina(rutinaId: Int): List<EjercicioEntity> {
        val relaciones = db.rutinaEjercicioDao().getByRutina(rutinaId)
        return relaciones.mapNotNull { db.ejercicioDao().getById(it.id_ejercicio) }
    }

    suspend fun getRelacionesDeRutina(rutinaId: Int): List<RutinaEjercicioEntity> =
        db.rutinaEjercicioDao().getByRutina(rutinaId)

    suspend fun añadirEjercicioARutina(
        rutinaId: Int,
        ejercicioId: Int,
        orden: Int,
        series: Int = 3,
        repeticiones: Int = 10
    ) = db.rutinaEjercicioDao().insertar(
        RutinaEjercicioEntity(
            id_rutina    = rutinaId,
            id_ejercicio = ejercicioId,
            series       = series,
            repeticiones = repeticiones,
            orden        = orden
        )
    )

    suspend fun quitarEjercicioDeRutina(rutinaId: Int, ejercicioId: Int) =
        db.rutinaEjercicioDao().eliminar(rutinaId, ejercicioId)

    // ── HISTORIAL ─────────────────────────────────────────────────────────────

    suspend fun registrarHistorial(
        userId: Int,
        ejercicioId: Int,
        rutinaId: Int,
        pesoKg: Double,
        repeticiones: Int,
        series: Int,
        duracionMin: Int,
        nombreEjercicio: String = ""
    ): Long {
        val fecha = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return db.historialDao().insertar(
            HistorialEntity(
                id_usuario       = userId,
                id_ejercicio     = ejercicioId,
                id_rutina        = rutinaId,
                nombre_ejercicio = nombreEjercicio,
                peso_kg          = pesoKg,
                repeticiones     = repeticiones,
                series           = series,
                duracion_minutos = duracionMin,
                fecha            = fecha
            )
        )
    }

    suspend fun getHistorial(userId: Int): List<HistorialEntity> =
        db.historialDao().getByUsuario(userId)

    // ── COMIDAS ───────────────────────────────────────────────────────────────

    suspend fun getComidas(): List<ComidaEntity> =
        db.comidaDao().getAll()

    suspend fun getComida(id: Int): ComidaEntity? =
        db.comidaDao().getById(id)

    // ── DIETAS ────────────────────────────────────────────────────────────────

    suspend fun getDietas(userId: Int): List<DietaEntity> =
        db.dietaDao().getByUsuario(userId)

    suspend fun getDietaActiva(userId: Int): DietaEntity? =
        db.dietaDao().getActiva(userId)

    suspend fun crearDieta(
        nombre: String,
        caloriasObj: Int,
        proteinasG: Double,
        carbsG: Double,
        grasasG: Double,
        userId: Int
    ): Long {
        val fecha = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return db.dietaDao().insertar(
            DietaEntity(
                nombre           = nombre,
                objetivo_calorico= caloriasObj,
                proteinas_g      = proteinasG,
                carbohidratos_g  = carbsG,
                grasas_g         = grasasG,
                fecha_inicio     = fecha,
                id_usuario       = userId,
                activo           = false
            )
        )
    }

    suspend fun editarDieta(
        dietaId: Int,
        nombre: String,
        caloriasObj: Int,
        proteinasG: Double,
        carbsG: Double,
        grasasG: Double
    ) {
        val actual = db.dietaDao().getById(dietaId) ?: return
        db.dietaDao().actualizar(
            actual.copy(
                nombre            = nombre,
                objetivo_calorico = caloriasObj,
                proteinas_g       = proteinasG,
                carbohidratos_g   = carbsG,
                grasas_g          = grasasG
            )
        )
    }

    suspend fun borrarDieta(dietaId: Int) =
        db.dietaDao().eliminar(dietaId)

    suspend fun activarDieta(dietaId: Int, userId: Int) {
        db.dietaDao().desactivarTodas(userId)
        db.dietaDao().activar(dietaId)
    }

    // ── DIETA-COMIDA ──────────────────────────────────────────────────────────

    suspend fun getComidasDeDieta(dietaId: Int) =
        db.dietaComidaDao().getConDetalle(dietaId)

    suspend fun añadirComidaADieta(
        dietaId: Int,
        comidaId: Int,
        tipo: String,
        dia: String
    ): Long = db.dietaComidaDao().insertar(
        DietaComidaEntity(id_dieta = dietaId, id_comida = comidaId, tipo = tipo, dia = dia)
    )

    suspend fun eliminarComidaDeDieta(dietaComidaId: Int) =
        db.dietaComidaDao().eliminar(dietaComidaId)

    // ── MEDIDAS ───────────────────────────────────────────────────────────────

    suspend fun registrarMedida(
        userId: Int,
        pesoKg: Double,
        alturaCm: Double? = null,
        pechoCm: Double? = null,
        piernaCm: Double? = null,
        brazoCm: Double? = null,
        cinturaCm: Double? = null
    ): Long {
        val fecha = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return db.medidaDao().insertar(
            MedidaEntity(
                id_usuario = userId,
                fecha      = fecha,
                peso_kg    = pesoKg,
                altura_cm  = alturaCm,
                pecho_cm   = pechoCm,
                pierna_cm  = piernaCm,
                brazo_cm   = brazoCm,
                cintura_cm = cinturaCm
            )
        )
    }

    suspend fun getMedidas(userId: Int): List<MedidaEntity> =
        db.medidaDao().getByUsuario(userId)

    // ── SINGLETON ─────────────────────────────────────────────────────────────

    companion object {
        @Volatile private var INSTANCE: SmartFitRepository? = null

        fun getInstance(context: Context): SmartFitRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SmartFitRepository(context).also { INSTANCE = it }
            }
    }
}

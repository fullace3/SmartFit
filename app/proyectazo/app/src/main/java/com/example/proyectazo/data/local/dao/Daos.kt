package com.example.proyectazo.data.local.dao

import androidx.room.*
import com.example.proyectazo.data.local.entity.*

// ── USUARIO ───────────────────────────────────────────────────────────────────

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuario WHERE nombre = :nombre AND password_hash = :password LIMIT 1")
    suspend fun login(nombre: String, password: String): UsuarioEntity?

    @Query("SELECT * FROM usuario WHERE id_usuario = :id LIMIT 1")
    suspend fun getById(id: Int): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuario WHERE nombre = :nombre OR email = :email")
    suspend fun existeNombreOEmail(nombre: String, email: String): Int

    @Query("DELETE FROM usuario WHERE id_usuario = :id")
    suspend fun eliminar(id: Int)
}

// ── EJERCICIO ─────────────────────────────────────────────────────────────────

@Dao
interface EjercicioDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(ejercicios: List<EjercicioEntity>)

    @Query("SELECT * FROM ejercicio ORDER BY nombre ASC")
    suspend fun getAll(): List<EjercicioEntity>

    @Query("SELECT * FROM ejercicio WHERE id_ejercicio = :id LIMIT 1")
    suspend fun getById(id: Int): EjercicioEntity?
}

// ── RUTINA ────────────────────────────────────────────────────────────────────

@Dao
interface RutinaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(rutina: RutinaEntity): Long

    @Query("SELECT * FROM rutina WHERE id_usuario = :userId ORDER BY id_rutina DESC")
    suspend fun getByUsuario(userId: Int): List<RutinaEntity>

    @Query("SELECT * FROM rutina WHERE id_rutina = :id LIMIT 1")
    suspend fun getById(id: Int): RutinaEntity?

    @Query("UPDATE rutina SET nombre = :nombre WHERE id_rutina = :id")
    suspend fun updateNombre(id: Int, nombre: String)

    @Query("DELETE FROM rutina WHERE id_rutina = :id")
    suspend fun eliminar(id: Int)
}

// ── RUTINA-EJERCICIO ──────────────────────────────────────────────────────────

@Dao
interface RutinaEjercicioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(rel: RutinaEjercicioEntity)

    @Query("SELECT * FROM rutina_ejercicio WHERE id_rutina = :rutinaId ORDER BY orden ASC")
    suspend fun getByRutina(rutinaId: Int): List<RutinaEjercicioEntity>

    @Query("DELETE FROM rutina_ejercicio WHERE id_rutina = :rutinaId AND id_ejercicio = :ejercicioId")
    suspend fun eliminar(rutinaId: Int, ejercicioId: Int)
}

// ── HISTORIAL ─────────────────────────────────────────────────────────────────

@Dao
interface HistorialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(entrada: HistorialEntity): Long

    @Query("SELECT * FROM historial WHERE id_usuario = :userId ORDER BY fecha DESC")
    suspend fun getByUsuario(userId: Int): List<HistorialEntity>
}

// ── COMIDA ────────────────────────────────────────────────────────────────────

@Dao
interface ComidaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodas(comidas: List<ComidaEntity>)

    @Query("SELECT * FROM comida ORDER BY nombre ASC")
    suspend fun getAll(): List<ComidaEntity>

    @Query("SELECT * FROM comida WHERE id_comida = :id LIMIT 1")
    suspend fun getById(id: Int): ComidaEntity?
}

// ── DIETA ─────────────────────────────────────────────────────────────────────

@Dao
interface DietaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(dieta: DietaEntity): Long

    @Update
    suspend fun actualizar(dieta: DietaEntity)

    @Query("SELECT * FROM dieta WHERE id_usuario = :userId ORDER BY id_dieta DESC")
    suspend fun getByUsuario(userId: Int): List<DietaEntity>

    @Query("SELECT * FROM dieta WHERE id_usuario = :userId AND activo = 1 LIMIT 1")
    suspend fun getActiva(userId: Int): DietaEntity?

    @Query("UPDATE dieta SET activo = 0 WHERE id_usuario = :userId")
    suspend fun desactivarTodas(userId: Int)

    @Query("UPDATE dieta SET activo = 1 WHERE id_dieta = :dietaId")
    suspend fun activar(dietaId: Int)

    @Query("SELECT * FROM dieta WHERE id_dieta = :id LIMIT 1")
    suspend fun getById(id: Int): DietaEntity?

    @Query("DELETE FROM dieta WHERE id_dieta = :id")
    suspend fun eliminar(id: Int)
}

// ── DIETA-COMIDA ──────────────────────────────────────────────────────────────

data class DietaComidaConDetalle(
    val id: Int,
    val id_dieta: Int,
    val tipo: String,
    val dia: String,
    val id_comida: Int,
    val nombre: String,
    val calorias_100g: Int,
    val proteinas_100g: Int,
    val carbohidratos_100g: Int,
    val grasas_100g: Int,
    val imagen: String?
)

@Dao
interface DietaComidaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(rel: DietaComidaEntity): Long

    @Query("""
        SELECT dc.id, dc.id_dieta, dc.tipo, dc.dia,
               c.id_comida, c.nombre, c.calorias_100g, c.proteinas_100g,
               c.carbohidratos_100g, c.grasas_100g, c.imagen
        FROM dieta_comida dc
        INNER JOIN comida c ON dc.id_comida = c.id_comida
        WHERE dc.id_dieta = :dietaId
    """)
    suspend fun getConDetalle(dietaId: Int): List<DietaComidaConDetalle>

    @Query("DELETE FROM dieta_comida WHERE id = :id")
    suspend fun eliminar(id: Int)
}

// ── MEDIDA ────────────────────────────────────────────────────────────────────

@Dao
interface MedidaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(medida: MedidaEntity): Long

    @Query("SELECT * FROM medida WHERE id_usuario = :userId ORDER BY fecha ASC")
    suspend fun getByUsuario(userId: Int): List<MedidaEntity>
}

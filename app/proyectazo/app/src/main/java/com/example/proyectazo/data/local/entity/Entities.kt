package com.example.proyectazo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id_usuario: Int = 0,
    val nombre: String,
    val email: String,
    val password_hash: String,
    val fecha_registro: String = ""
)

@Entity(tableName = "ejercicio")
data class EjercicioEntity(
    @PrimaryKey(autoGenerate = true) val id_ejercicio: Int = 0,
    val nombre: String,
    val grupo_muscular: String? = null,
    val equipamiento: String? = null,
    val descripcion: String? = null,
    val imagen: String? = null
)

@Entity(tableName = "rutina",
    foreignKeys = [ForeignKey(
        entity = UsuarioEntity::class,
        parentColumns = ["id_usuario"],
        childColumns = ["id_usuario"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("id_usuario")]
)
data class RutinaEntity(
    @PrimaryKey(autoGenerate = true) val id_rutina: Int = 0,
    val nombre: String,
    val id_usuario: Int
)

@Entity(
    tableName = "rutina_ejercicio",
    primaryKeys = ["id_rutina", "id_ejercicio"],
    foreignKeys = [
        ForeignKey(entity = RutinaEntity::class,   parentColumns = ["id_rutina"],   childColumns = ["id_rutina"],   onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EjercicioEntity::class, parentColumns = ["id_ejercicio"], childColumns = ["id_ejercicio"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("id_rutina"), Index("id_ejercicio")]
)
data class RutinaEjercicioEntity(
    val id_rutina: Int,
    val id_ejercicio: Int,
    val series: Int = 3,
    val repeticiones: Int = 10,
    val orden: Int = 0
)

@Entity(tableName = "historial",
    foreignKeys = [ForeignKey(
        entity = UsuarioEntity::class,
        parentColumns = ["id_usuario"],
        childColumns = ["id_usuario"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("id_usuario")]
)
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id_registro: Int = 0,
    val id_usuario: Int,
    val id_ejercicio: Int,
    val id_rutina: Int,
    val nombre_ejercicio: String = "",
    val peso_kg: Double = 0.0,
    val repeticiones: Int = 0,
    val series: Int = 0,
    val duracion_minutos: Int = 0,
    val fecha: String = ""
)

@Entity(tableName = "comida")
data class ComidaEntity(
    @PrimaryKey(autoGenerate = true) val id_comida: Int = 0,
    val nombre: String,
    val calorias_100g: Int = 0,
    val proteinas_100g: Int = 0,
    val carbohidratos_100g: Int = 0,
    val grasas_100g: Int = 0,
    val id_usuario: Int? = null,
    val imagen: String? = null
)

@Entity(tableName = "dieta",
    foreignKeys = [ForeignKey(
        entity = UsuarioEntity::class,
        parentColumns = ["id_usuario"],
        childColumns = ["id_usuario"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("id_usuario")]
)
data class DietaEntity(
    @PrimaryKey(autoGenerate = true) val id_dieta: Int = 0,
    val nombre: String,
    val objetivo_calorico: Int = 0,
    val proteinas_g: Double = 0.0,
    val carbohidratos_g: Double = 0.0,
    val grasas_g: Double = 0.0,
    val fecha_inicio: String = "",
    val fecha_fin: String? = null,
    val id_usuario: Int,
    val activo: Boolean = false
)

@Entity(
    tableName = "dieta_comida",
    foreignKeys = [
        ForeignKey(entity = DietaEntity::class,  parentColumns = ["id_dieta"],  childColumns = ["id_dieta"],  onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ComidaEntity::class, parentColumns = ["id_comida"], childColumns = ["id_comida"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("id_dieta"), Index("id_comida")]
)
data class DietaComidaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val id_dieta: Int,
    val id_comida: Int,
    val tipo: String = "Desayuno",
    val dia: String = "Lun"
)

@Entity(tableName = "medida",
    foreignKeys = [ForeignKey(
        entity = UsuarioEntity::class,
        parentColumns = ["id_usuario"],
        childColumns = ["id_usuario"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("id_usuario")]
)
data class MedidaEntity(
    @PrimaryKey(autoGenerate = true) val id_medida: Int = 0,
    val id_usuario: Int,
    val fecha: String = "",
    val peso_kg: Double = 0.0,
    val altura_cm: Double? = null,
    val pecho_cm: Double? = null,
    val pierna_cm: Double? = null,
    val brazo_cm: Double? = null,
    val cintura_cm: Double? = null,
    val grasa_corporal_pct: Double? = null,
    val edad: Int? = null,
    val sexo: String? = null
)

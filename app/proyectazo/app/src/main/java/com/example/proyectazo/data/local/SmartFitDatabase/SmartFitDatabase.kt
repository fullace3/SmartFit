package com.example.proyectazo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.proyectazo.data.local.dao.*
import com.example.proyectazo.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UsuarioEntity::class,
        EjercicioEntity::class,
        RutinaEntity::class,
        RutinaEjercicioEntity::class,
        HistorialEntity::class,
        ComidaEntity::class,
        DietaEntity::class,
        DietaComidaEntity::class,
        MedidaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmartFitDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun ejercicioDao(): EjercicioDao
    abstract fun rutinaDao(): RutinaDao
    abstract fun rutinaEjercicioDao(): RutinaEjercicioDao
    abstract fun historialDao(): HistorialDao
    abstract fun comidaDao(): ComidaDao
    abstract fun dietaDao(): DietaDao
    abstract fun dietaComidaDao(): DietaComidaDao
    abstract fun medidaDao(): MedidaDao

    companion object {
        @Volatile
        private var INSTANCE: SmartFitDatabase? = null

        fun getInstance(context: Context): SmartFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartFitDatabase::class.java,
                    "smartfit.db"
                )
                    .addCallback(SeedCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Inserts seed data on first install — exercises and global food items
    private class SeedCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                database.ejercicioDao().insertarTodos(seedEjercicios())
                database.comidaDao().insertarTodas(seedComidas())
            }
        }
    }
}

// ── SEED EJERCICIOS ───────────────────────────────────────────────────────
// Catálogo real de 100 ejercicios (origen: EjerciciosCompleto.csv del proyecto
// original). El campo 'imagen' guarda solo el nombre del recurso local en
// res/drawable, SIN extensión ni ruta — son imágenes propias, ya no URLs de S3.
// Se resuelven a R.drawable.<nombre> en tiempo de ejecución vía
// ImagenesLocales.resolver() (ui/util/ImagenesLocales.kt).
private fun seedEjercicios(): List<EjercicioEntity> = listOf(
    // Pecho
    EjercicioEntity(nombre = "Paralela Flexión", grupo_muscular = "Pecho", equipamiento = "Barras paralelas", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "paralela_flexion"),
    EjercicioEntity(nombre = "Barra Press de banca", grupo_muscular = "Pecho", equipamiento = "Barra", descripcion = "Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.", imagen = "press_banca_barra"),
    EjercicioEntity(nombre = "Barra Press de banca inclinado", grupo_muscular = "Pecho", equipamiento = "Barra", descripcion = "Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.", imagen = "press_banca_inclinado"),
    EjercicioEntity(nombre = "Barra Press de banca declinado", grupo_muscular = "Pecho", equipamiento = "Barra", descripcion = "Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.", imagen = "press_banca_declinado"),
    EjercicioEntity(nombre = "Peso corporal Flexión", grupo_muscular = "Pecho", equipamiento = "Peso corporal", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "peso_corporal_flexion"),
    EjercicioEntity(nombre = "Barra Floor Press", grupo_muscular = "Pecho", equipamiento = "Barra", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "barra_floor_press"),
    EjercicioEntity(nombre = "Doble Mancuerna Press de banca", grupo_muscular = "Pecho", equipamiento = "Mancuerna", descripcion = "Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.", imagen = "doble_mancuerna_press_banca"),
    EjercicioEntity(nombre = "Doble Mancuerna Press de banca inclinado", grupo_muscular = "Pecho", equipamiento = "Mancuerna", descripcion = "Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.", imagen = "doble_mancuerna_press_inclinado"),
    EjercicioEntity(nombre = "Doble Mancuerna Press de banca declinado", grupo_muscular = "Pecho", equipamiento = "Mancuerna", descripcion = "Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.", imagen = "doble_mancuerna_press_banca_declinado"),
    // Espalda
    EjercicioEntity(nombre = "Polea Straight Bar Pullover", grupo_muscular = "Espalda", equipamiento = "Polea", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "polea_straight_bar_pullover"),
    EjercicioEntity(nombre = "Doble Mancuerna Banco inclinado Prono Remo", grupo_muscular = "Espalda", equipamiento = "Mancuerna", descripcion = "Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.", imagen = "doble_mancuerna_banco_inclinado_prono_remo"),
    EjercicioEntity(nombre = "Barra Pendlay Remo", grupo_muscular = "Espalda", equipamiento = "Barra", descripcion = "Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.", imagen = "barra_pendlay_remo"),
    EjercicioEntity(nombre = "Barra Bent Over Remo", grupo_muscular = "Espalda", equipamiento = "Barra", descripcion = "Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.", imagen = "barra_beent_over_remo"),
    EjercicioEntity(nombre = "Doble Mancuerna Prono Remo", grupo_muscular = "Espalda", equipamiento = "Mancuerna", descripcion = "Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.", imagen = "doble_mancuerna_prono_remo"),
    EjercicioEntity(nombre = "Barra Conventional Peso muerto", grupo_muscular = "Espalda", equipamiento = "Barra", descripcion = "Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.", imagen = "barra_convetional_peso_muerto"),
    EjercicioEntity(nombre = "Barra Sumo Peso muerto", grupo_muscular = "Espalda", equipamiento = "Barra", descripcion = "Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.", imagen = "barra_sumo_peso_muerto"),
    EjercicioEntity(nombre = "Barra Good Morning", grupo_muscular = "Espalda", equipamiento = "Barra", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "barra_good_morning"),
    EjercicioEntity(nombre = "Barra Inversa Grip Bent Over Remo", grupo_muscular = "Espalda", equipamiento = "Barra", descripcion = "Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.", imagen = "barra_inversa_grip_bent_over_remo"),
    EjercicioEntity(nombre = "Doble Mancuerna Sentado Good Morning", grupo_muscular = "Espalda", equipamiento = "Mancuerna", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "doble_mancuerna_sentado_good_morning"),
    EjercicioEntity(nombre = "Doble Mancuerna Renegade Remo", grupo_muscular = "Espalda", equipamiento = "Mancuerna", descripcion = "Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.", imagen = "doble_mancuerna_renegade_remo"),
    EjercicioEntity(nombre = "Bar Dominada", grupo_muscular = "Espalda", equipamiento = "Barra de dominadas", descripcion = "Cuélgate con los brazos extendidos. Tira de tu cuerpo hacia arriba hasta pasar la barbilla por encima de la barra y baja de forma controlada.", imagen = "bar_dominada"),
    EjercicioEntity(nombre = "Bar Scapular Dominada", grupo_muscular = "Espalda", equipamiento = "Barra de dominadas", descripcion = "Cuélgate con los brazos extendidos. Tira de tu cuerpo hacia arriba hasta pasar la barbilla por encima de la barra y baja de forma controlada.", imagen = "bar_scapular_dominada"),
    EjercicioEntity(nombre = "Polea V Grip Sentado Bajo Remo", grupo_muscular = "Espalda", equipamiento = "Polea", descripcion = "Con el torso inclinado o apoyado, tira del peso hacia tu abdomen contrayendo la espalda y controlando la bajada al estirar los brazos.", imagen = "polea_v_grip_sentado_bajo_remo"),
    EjercicioEntity(nombre = "Polea Agarre ancho Lat Pulldown", grupo_muscular = "Espalda", equipamiento = "Polea", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "polea_agarre_ancho_lat_pulldown"),
    EjercicioEntity(nombre = "Polea Inversa Grip Lat Pulldown", grupo_muscular = "Espalda", equipamiento = "Polea", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "polea_inversa_grip_lat_pulldown"),
    EjercicioEntity(nombre = "Mancuerna Pullover", grupo_muscular = "Espalda", equipamiento = "Mancuerna", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "mancuerna_pullover"),
    // Hombros
    EjercicioEntity(nombre = "Polea Face Pull", grupo_muscular = "Hombros", equipamiento = "Polea", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "polea_face_pull"),
    EjercicioEntity(nombre = "Polea Sentado Face Pull", grupo_muscular = "Hombros", equipamiento = "Polea", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "polea_sentado_face_pull"),
    EjercicioEntity(nombre = "Barra Por encima de la cabeza Press", grupo_muscular = "Hombros", equipamiento = "Barra", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "barra_por_encima_de_la_cabeza_press"),
    EjercicioEntity(nombre = "Barra Push Press", grupo_muscular = "Hombros", equipamiento = "Barra", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "barra_push_press"),
    EjercicioEntity(nombre = "Barra Elevación frontal", grupo_muscular = "Hombros", equipamiento = "Barra", descripcion = "Eleva el peso hacia adelante con los brazos casi rectos hasta la altura de los ojos y baja de forma controlada.", imagen = "barra_elevacion_frontal"),
    EjercicioEntity(nombre = "Doble Mancuerna Push Press", grupo_muscular = "Hombros", equipamiento = "Mancuerna", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "doble_mancuerna_push_press"),
    EjercicioEntity(nombre = "Doble Mancuerna Elevación Lateral", grupo_muscular = "Hombros", equipamiento = "Mancuerna", descripcion = "Con una ligera flexión de codos, eleva los brazos hacia los lados hasta la altura de los hombros y baja controlando el peso.", imagen = "doble_mancuerna_elevacion_lateral"),
    EjercicioEntity(nombre = "Doble Mancuerna Sentado Elevación Lateral", grupo_muscular = "Hombros", equipamiento = "Mancuerna", descripcion = "Con una ligera flexión de codos, eleva los brazos hacia los lados hasta la altura de los hombros y baja controlando el peso.", imagen = "doble_mancuerna_sentado_elevacion_lateral"),
    EjercicioEntity(nombre = "Doble Mancuerna Sentado Por encima de la cabeza Press", grupo_muscular = "Hombros", equipamiento = "Mancuerna", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "doble_mancuerna_sentado_por_encima_de_la_cabeza_extensio"),
    EjercicioEntity(nombre = "Doble Mancuerna Por encima de la cabeza Press", grupo_muscular = "Hombros", equipamiento = "Mancuerna", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "doble_mancuerna_por_encima_de_la_cabeza_press"),
    EjercicioEntity(nombre = "Doble Mancuerna De pie Scaption", grupo_muscular = "Hombros", equipamiento = "Mancuerna", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "doble_mancuerna_de_pie_scaption"),
    EjercicioEntity(nombre = "Doble Mancuerna Arnold Press", grupo_muscular = "Hombros", equipamiento = "Mancuerna", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "doble_mancuerna_arnold_press"),
    // Trapecio
    EjercicioEntity(nombre = "Barra Shrug", grupo_muscular = "Trapecio", equipamiento = "Barra", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "barra_shrug"),
    EjercicioEntity(nombre = "Doble Mancuerna Shrug", grupo_muscular = "Trapecio", equipamiento = "Mancuerna", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "doble_mancuerna_shrug"),
    // Bíceps
    EjercicioEntity(nombre = "Polea Supino Curl de bíceps", grupo_muscular = "Bíceps", equipamiento = "Polea", descripcion = "Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.", imagen = "polea_supino_curl_de_biceps"),
    EjercicioEntity(nombre = "Barra Curl de bíceps", grupo_muscular = "Bíceps", equipamiento = "Barra", descripcion = "Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.", imagen = "barra_curl_de_biceps"),
    EjercicioEntity(nombre = "Bar Dominada supina", grupo_muscular = "Bíceps", equipamiento = "Barra de dominadas", descripcion = "Cuélgate con los brazos extendidos. Tira de tu cuerpo hacia arriba hasta pasar la barbilla por encima de la barra y baja de forma controlada.", imagen = "bar_dominada_supina"),
    EjercicioEntity(nombre = "Alterno Doble Mancuerna Curl de bíceps", grupo_muscular = "Bíceps", equipamiento = "Mancuerna", descripcion = "Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.", imagen = "alterno_doble_mancuerna_curl_de_biceps"),
    EjercicioEntity(nombre = "Alterno Doble Mancuerna Hammer Curl", grupo_muscular = "Bíceps", equipamiento = "Mancuerna", descripcion = "Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.", imagen = "alterno_doble_mancuerna_hammer_curl"),
    EjercicioEntity(nombre = "A un brazo Mancuerna Banco inclinado Preacher Curl", grupo_muscular = "Bíceps", equipamiento = "Mancuerna", descripcion = "Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.", imagen = "a_un_brazo_mancuerna_banco_inclinado_preacher_curl"),
    EjercicioEntity(nombre = "Doble Mancuerna Hammer Curl", grupo_muscular = "Bíceps", equipamiento = "Mancuerna", descripcion = "Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.", imagen = "doble_mancuerna_hammer_curl"),
    // Tríceps
    EjercicioEntity(nombre = "Peso corporal Diamond Flexión", grupo_muscular = "Tríceps", equipamiento = "Peso corporal", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "peso_corporal_diamond_flexion"),
    EjercicioEntity(nombre = "Polea Cuerda Por encima de la cabeza Extensión de tríceps", grupo_muscular = "Tríceps", equipamiento = "Polea", descripcion = "Con los codos fijos, extiende los brazos por completo para contraer los tríceps y vuelve despacio a la posición inicial.", imagen = "polea_cuerda_por_encima_de_la_cabeza_extension_de_tri"),
    EjercicioEntity(nombre = "Paralela Agarre estrecho Flexión", grupo_muscular = "Tríceps", equipamiento = "Barras paralelas", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "paralela_agarre_estrecho_flexion"),
    EjercicioEntity(nombre = "Barra Agarre estrecho Press de banca", grupo_muscular = "Tríceps", equipamiento = "Barra", descripcion = "Túmbate en el banco, baja el peso de forma controlada hasta el pecho y empuja con fuerza hasta extender los brazos.", imagen = "barra_agarre_estrecho_press_de_banca"),
    EjercicioEntity(nombre = "Superband Assisted Dips", grupo_muscular = "Tríceps", equipamiento = "Superband", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "superband_assisted_dips"),
    EjercicioEntity(nombre = "Doble Mancuerna Tríceps Kickback", grupo_muscular = "Tríceps", equipamiento = "Mancuerna", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "doble_mancuerna_triceps_kickback"),
    EjercicioEntity(nombre = "Mancuerna Sentado Por encima de la cabeza Extensión de tríceps", grupo_muscular = "Tríceps", equipamiento = "Mancuerna", descripcion = "Con los codos fijos, extiende los brazos por completo para contraer los tríceps y vuelve despacio a la posición inicial.", imagen = "mancuerna_sentado_por_encima_de_la_cabeza_extensio"),
    EjercicioEntity(nombre = "Barra Sentado Por encima de la cabeza Extensión de tríceps", grupo_muscular = "Tríceps", equipamiento = "Barra", descripcion = "Con los codos fijos, extiende los brazos por completo para contraer los tríceps y vuelve despacio a la posición inicial.", imagen = "barra_sentado_por_encima_de_la_cabeza_extension_de_tri"),
    EjercicioEntity(nombre = "Peso corporal Dips", grupo_muscular = "Tríceps", equipamiento = "Peso corporal", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "peso_corporal_dips"),
    EjercicioEntity(nombre = "Barra Skull Crusher", grupo_muscular = "Tríceps", equipamiento = "Barra", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "barra_skull_crusher"),
    // Cuádriceps
    EjercicioEntity(nombre = "Barra Thruster", grupo_muscular = "Cuádriceps", equipamiento = "Barra", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "barra_thruster"),
    EjercicioEntity(nombre = "Peso corporal Sentadilla", grupo_muscular = "Cuádriceps", equipamiento = "Peso corporal", descripcion = "Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.", imagen = "peso_corporal_sentadilla"),
    EjercicioEntity(nombre = "Mancuerna Goblet Sentadilla", grupo_muscular = "Cuádriceps", equipamiento = "Mancuerna", descripcion = "Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.", imagen = "mancuerna_goblet_sentadilla"),
    EjercicioEntity(nombre = "Barra Posición frontal Sentadilla", grupo_muscular = "Cuádriceps", equipamiento = "Barra", descripcion = "Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.", imagen = "barra_posicion_frontal_sentadilla"),
    EjercicioEntity(nombre = "Barra Alto Bar Espalda Sentadilla", grupo_muscular = "Cuádriceps", equipamiento = "Barra", descripcion = "Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.", imagen = "barra_alto_bar_espalda_sentadilla"),
    EjercicioEntity(nombre = "Barra Bajo Bar Espalda Sentadilla", grupo_muscular = "Cuádriceps", equipamiento = "Barra", descripcion = "Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.", imagen = "barra_bajo_bar_espalda_sentadilla"),
    EjercicioEntity(nombre = "Doble Mancuerna Estilo maleta Sentadilla búlgara", grupo_muscular = "Cuádriceps", equipamiento = "Mancuerna", descripcion = "Flexiona las rodillas y baja las caderas manteniendo la espalda recta y el pecho arriba. Empuja con las piernas para volver a subir.", imagen = "doble_mancuerna_estilo_maleta_sentadilla_bulgara"),
    EjercicioEntity(nombre = "Doble Mancuerna Estilo maleta Subida al cajón", grupo_muscular = "Cuádriceps", equipamiento = "Mancuerna", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "doble_mancuerna_estilo_maleta_subida_al_cajon"),
    EjercicioEntity(nombre = "Doble Mancuerna Estilo maleta Alterno Hacia adelante Zancada", grupo_muscular = "Cuádriceps", equipamiento = "Mancuerna", descripcion = "Da un paso y flexiona ambas rodillas hasta que la pierna trasera casi toque el suelo. Empuja para volver a la posición inicial.", imagen = "doble_mancuerna_estilo_maleta_alterno_hacia_adelante"),
    EjercicioEntity(nombre = "Doble Mancuerna Estilo maleta Alterno Inversa Zancada", grupo_muscular = "Cuádriceps", equipamiento = "Mancuerna", descripcion = "Da un paso y flexiona ambas rodillas hasta que la pierna trasera casi toque el suelo. Empuja para volver a la posición inicial.", imagen = "doble_mancuerna_estilo_maleta_alterno_inversa_zancada"),
    EjercicioEntity(nombre = "Barra Posición trasera Caminando Zancada", grupo_muscular = "Cuádriceps", equipamiento = "Barra", descripcion = "Da un paso y flexiona ambas rodillas hasta que la pierna trasera casi toque el suelo. Empuja para volver a la posición inicial.", imagen = "barra_posicion_trasera_caminando_zancada"),
    EjercicioEntity(nombre = "Barra Posición trasera Subida al cajón", grupo_muscular = "Cuádriceps", equipamiento = "Barra", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "barra_posicion_trasera_subida_al_cajon"),
    // Isquiosurales
    EjercicioEntity(nombre = "Doble Mancuerna Romanian Peso muerto", grupo_muscular = "Isquiosurales", equipamiento = "Mancuerna", descripcion = "Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.", imagen = "doble_mancuerna_romanian_peso_muerto"),
    EjercicioEntity(nombre = "Barra Romanian Peso muerto", grupo_muscular = "Isquiosurales", equipamiento = "Barra", descripcion = "Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.", imagen = "barra_romanian_peso_muerto"),
    EjercicioEntity(nombre = "Barra Stiff Legged Peso muerto", grupo_muscular = "Isquiosurales", equipamiento = "Barra", descripcion = "Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.", imagen = "barra_stiff_legged_peso_muerto"),
    EjercicioEntity(nombre = "Barra A una pierna Romanian Peso muerto", grupo_muscular = "Isquiosurales", equipamiento = "Barra", descripcion = "Manteniendo la espalda neutra, flexiona la cadera para bajar el peso a lo largo de las piernas. Vuelve a extender la cadera para subir y aprieta glúteos.", imagen = "barra_a_una_pierna_romanian_peso_muerto"),
    // Glúteos
    EjercicioEntity(nombre = "Peso corporal Puente de glúteo", grupo_muscular = "Glúteos", equipamiento = "Peso corporal", descripcion = "Tumbado boca arriba con las rodillas flexionadas, empuja con los talones para elevar la cadera apretando los glúteos al máximo al subir.", imagen = "peso_corporal_puente_de_gluteo"),
    EjercicioEntity(nombre = "Mancuerna Puente de glúteo", grupo_muscular = "Glúteos", equipamiento = "Mancuerna", descripcion = "Tumbado boca arriba con las rodillas flexionadas, empuja con los talones para elevar la cadera apretando los glúteos al máximo al subir.", imagen = "mancuerna_puente_de_gluteo"),
    EjercicioEntity(nombre = "Barra Cadera Thrust", grupo_muscular = "Glúteos", equipamiento = "Barra", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "barra_cadera_thrust"),
    EjercicioEntity(nombre = "Barra A una pierna Cadera Thrust", grupo_muscular = "Glúteos", equipamiento = "Barra", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "barra_a_una_pierna_cadera_thrust"),
    EjercicioEntity(nombre = "Peso corporal Cadera Thrust", grupo_muscular = "Glúteos", equipamiento = "Peso corporal", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "peso_corporal_cadera_thrust"),
    EjercicioEntity(nombre = "Mancuerna Cadera Thrust", grupo_muscular = "Glúteos", equipamiento = "Mancuerna", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "mancuerna_cadera_thrust"),
    // Abdominales
    EjercicioEntity(nombre = "Peso corporal Bird Dog", grupo_muscular = "Abdominales", equipamiento = "Peso corporal", descripcion = "Mantén la columna neutra y el abdomen tenso. Extiende las extremidades opuestas de forma lenta y controlada sin perder la postura.", imagen = "peso_corporal_bird_dog"),
    EjercicioEntity(nombre = "Pelota de estabilidad Giro ruso", grupo_muscular = "Abdominales", equipamiento = "Fitball", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "pelota_de_estabilidad_giro_ruso"),
    EjercicioEntity(nombre = "Paralela Escalador", grupo_muscular = "Abdominales", equipamiento = "Barras paralelas", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "paralela_escalador"),
    EjercicioEntity(nombre = "Peso corporal Bicho muerto", grupo_muscular = "Abdominales", equipamiento = "Peso corporal", descripcion = "Mantén la columna neutra y el abdomen tenso. Extiende las extremidades opuestas de forma lenta y controlada sin perder la postura.", imagen = "peso_corporal_bicho_muerto"),
    EjercicioEntity(nombre = "Peso corporal Kneeling Forearm Plancha", grupo_muscular = "Abdominales", equipamiento = "Peso corporal", descripcion = "Apóyate en los antebrazos y las puntas de los pies. Mantén el cuerpo en línea recta contrayendo fuerte el abdomen y los glúteos.", imagen = "peso_corporal_kneeling_forearm_plancha"),
    EjercicioEntity(nombre = "Ab Wheel Kneeling Rollout", grupo_muscular = "Abdominales", equipamiento = "Ab Wheel", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "ab_wheel_kneeling_rollout"),
    EjercicioEntity(nombre = "Polea Kneeling Encogimiento", grupo_muscular = "Abdominales", equipamiento = "Polea", descripcion = "Tumbado boca arriba, contrae el abdomen para elevar ligeramente los hombros del suelo sin tirar del cuello, y vuelve a bajar despacio.", imagen = "polea_kneeling_encogimiento"),
    EjercicioEntity(nombre = "Paralela L Sit", grupo_muscular = "Abdominales", equipamiento = "Barras paralelas", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "paralela_l_sit"),
    EjercicioEntity(nombre = "Peso corporal De pie Walkout Plancha", grupo_muscular = "Abdominales", equipamiento = "Peso corporal", descripcion = "Apóyate en los antebrazos y las puntas de los pies. Mantén el cuerpo en línea recta contrayendo fuerte el abdomen y los glúteos.", imagen = "peso_corporal_de_pie_walkout_plancha"),
    EjercicioEntity(nombre = "Peso corporal Plancha Lateral", grupo_muscular = "Abdominales", equipamiento = "Peso corporal", descripcion = "Apóyate en los antebrazos y las puntas de los pies. Mantén el cuerpo en línea recta contrayendo fuerte el abdomen y los glúteos.", imagen = "peso_corporal_plancha_lateral"),
    EjercicioEntity(nombre = "Bar Inverted Hanging Encogimiento", grupo_muscular = "Abdominales", equipamiento = "Barra de dominadas", descripcion = "Tumbado boca arriba, contrae el abdomen para elevar ligeramente los hombros del suelo sin tirar del cuello, y vuelve a bajar despacio.", imagen = "bar_inverted_hanging_encogimiento"),
    EjercicioEntity(nombre = "Barra Kneeling Rollout", grupo_muscular = "Abdominales", equipamiento = "Barra", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "barra_kneeling_rollout"),
    EjercicioEntity(nombre = "Bar Hanging Rodilla Elevación", grupo_muscular = "Abdominales", equipamiento = "Barra de dominadas", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "bar_hanging_rodilla_elevacion"),
    EjercicioEntity(nombre = "Bar Hanging Rodillas a Codos", grupo_muscular = "Abdominales", equipamiento = "Barra de dominadas", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "bar_hanging_rodillas_a_codos"),
    EjercicioEntity(nombre = "Bar Hanging Pierna Elevación", grupo_muscular = "Abdominales", equipamiento = "Barra de dominadas", descripcion = "Agarra el equipamiento de forma segura. Realiza el movimiento en todo su rango de recorrido de forma controlada, manteniendo el core firme y sin dar tirones.", imagen = "bar_hanging_pierna_elevacion"),
    // Gemelos
    EjercicioEntity(nombre = "Doble Mancuerna Estilo maleta Elevación de gemelos", grupo_muscular = "Gemelos", equipamiento = "Mancuerna", descripcion = "Eleva los talones apoyándote en la punta de los pies para contraer los gemelos. Aguanta un segundo arriba y baja estirando por completo.", imagen = "doble_mancuerna_estilo_maleta_elevacion_de_gemelos"),
    EjercicioEntity(nombre = "Doble Mancuerna Sentado Elevación de gemelos", grupo_muscular = "Gemelos", equipamiento = "Mancuerna", descripcion = "Eleva los talones apoyándote en la punta de los pies para contraer los gemelos. Aguanta un segundo arriba y baja estirando por completo.", imagen = "doble_mancuerna_sentado_elevacion_de_gemelos"),
    EjercicioEntity(nombre = "Barra Sentado Elevación de gemelos", grupo_muscular = "Gemelos", equipamiento = "Barra", descripcion = "Eleva los talones apoyándote en la punta de los pies para contraer los gemelos. Aguanta un segundo arriba y baja estirando por completo.", imagen = "barra_sentado_elevacion_de_gemelos"),
    EjercicioEntity(nombre = "Peso corporal Mano Assisted Pies elevados Elevación de gemelos", grupo_muscular = "Gemelos", equipamiento = "Peso corporal", descripcion = "Eleva los talones apoyándote en la punta de los pies para contraer los gemelos. Aguanta un segundo arriba y baja estirando por completo.", imagen = "peso_corporal_mano_assisted_pies_elevados_elevacion_d"),
    EjercicioEntity(nombre = "Barra Posición trasera Elevación de gemelos", grupo_muscular = "Gemelos", equipamiento = "Barra", descripcion = "Eleva los talones apoyándote en la punta de los pies para contraer los gemelos. Aguanta un segundo arriba y baja estirando por completo.", imagen = "barra_posicion_trasera_elevacion_de_gemelos"),
    // Antebrazos
    EjercicioEntity(nombre = "Barra Inversa Grip Curl de bíceps", grupo_muscular = "Antebrazos", equipamiento = "Barra", descripcion = "Manteniendo los codos pegados al costado del cuerpo, flexiona los brazos para subir el peso hacia los hombros y baja controlando el movimiento.", imagen = "barra_inversa_grip_curl_de_biceps"),
)

// ── SEED COMIDAS ──────────────────────────────────────────────────────────
// Datos nutricionales por 100g — fuente: USDA FoodData Central
private fun seedComidas(): List<ComidaEntity> = listOf(
    // Proteínas
    ComidaEntity(nombre = "Pollo a la plancha", calorias_100g = 165, proteinas_100g = 31, carbohidratos_100g = 0, grasas_100g = 4),
    ComidaEntity(nombre = "Salmón al horno", calorias_100g = 208, proteinas_100g = 20, carbohidratos_100g = 0, grasas_100g = 13),
    ComidaEntity(nombre = "Atún en lata", calorias_100g = 116, proteinas_100g = 26, carbohidratos_100g = 0, grasas_100g = 1),
    ComidaEntity(nombre = "Ternera magra", calorias_100g = 135, proteinas_100g = 21, carbohidratos_100g = 0, grasas_100g = 5),
    ComidaEntity(nombre = "Pechuga de pavo", calorias_100g = 135, proteinas_100g = 30, carbohidratos_100g = 0, grasas_100g = 1),
    ComidaEntity(nombre = "Huevos enteros", calorias_100g = 143, proteinas_100g = 13, carbohidratos_100g = 1, grasas_100g = 10),
    ComidaEntity(nombre = "Claras de huevo", calorias_100g = 52, proteinas_100g = 11, carbohidratos_100g = 1, grasas_100g = 0),
    ComidaEntity(nombre = "Queso cottage", calorias_100g = 98, proteinas_100g = 11, carbohidratos_100g = 3, grasas_100g = 4),
    ComidaEntity(nombre = "Yogur griego natural", calorias_100g = 59, proteinas_100g = 10, carbohidratos_100g = 4, grasas_100g = 0),
    ComidaEntity(nombre = "Tofu firme", calorias_100g = 76, proteinas_100g = 8, carbohidratos_100g = 2, grasas_100g = 4),
    // Carbohidratos
    ComidaEntity(nombre = "Arroz blanco cocido", calorias_100g = 130, proteinas_100g = 3, carbohidratos_100g = 28, grasas_100g = 0),
    ComidaEntity(nombre = "Arroz integral cocido", calorias_100g = 111, proteinas_100g = 3, carbohidratos_100g = 23, grasas_100g = 1),
    ComidaEntity(nombre = "Pasta cocida", calorias_100g = 158, proteinas_100g = 6, carbohidratos_100g = 31, grasas_100g = 1),
    ComidaEntity(nombre = "Avena", calorias_100g = 389, proteinas_100g = 17, carbohidratos_100g = 66, grasas_100g = 7),
    ComidaEntity(nombre = "Pan integral", calorias_100g = 247, proteinas_100g = 13, carbohidratos_100g = 41, grasas_100g = 4),
    ComidaEntity(nombre = "Patata cocida", calorias_100g = 87, proteinas_100g = 2, carbohidratos_100g = 20, grasas_100g = 0),
    ComidaEntity(nombre = "Boniato cocido", calorias_100g = 86, proteinas_100g = 2, carbohidratos_100g = 20, grasas_100g = 0),
    ComidaEntity(nombre = "Quinoa cocida", calorias_100g = 120, proteinas_100g = 4, carbohidratos_100g = 22, grasas_100g = 2),
    ComidaEntity(nombre = "Lentejas cocidas", calorias_100g = 116, proteinas_100g = 9, carbohidratos_100g = 20, grasas_100g = 0),
    ComidaEntity(nombre = "Garbanzos cocidos", calorias_100g = 164, proteinas_100g = 9, carbohidratos_100g = 27, grasas_100g = 3),
    ComidaEntity(nombre = "Plátano", calorias_100g = 89, proteinas_100g = 1, carbohidratos_100g = 23, grasas_100g = 0),
    ComidaEntity(nombre = "Manzana", calorias_100g = 52, proteinas_100g = 0, carbohidratos_100g = 14, grasas_100g = 0),
    // Grasas saludables
    ComidaEntity(nombre = "Aguacate", calorias_100g = 160, proteinas_100g = 2, carbohidratos_100g = 9, grasas_100g = 15),
    ComidaEntity(nombre = "Almendras", calorias_100g = 579, proteinas_100g = 21, carbohidratos_100g = 22, grasas_100g = 50),
    ComidaEntity(nombre = "Nueces", calorias_100g = 654, proteinas_100g = 15, carbohidratos_100g = 14, grasas_100g = 65),
    ComidaEntity(nombre = "Aceite de oliva virgen extra", calorias_100g = 884, proteinas_100g = 0, carbohidratos_100g = 0, grasas_100g = 100),
    ComidaEntity(nombre = "Mantequilla de cacahuete natural", calorias_100g = 588, proteinas_100g = 25, carbohidratos_100g = 20, grasas_100g = 50),
    ComidaEntity(nombre = "Semillas de chía", calorias_100g = 486, proteinas_100g = 17, carbohidratos_100g = 42, grasas_100g = 31),
    ComidaEntity(nombre = "Aceite de coco", calorias_100g = 862, proteinas_100g = 0, carbohidratos_100g = 0, grasas_100g = 100),
    // Verduras
    ComidaEntity(nombre = "Brócoli", calorias_100g = 34, proteinas_100g = 3, carbohidratos_100g = 7, grasas_100g = 0),
    ComidaEntity(nombre = "Espinacas", calorias_100g = 23, proteinas_100g = 3, carbohidratos_100g = 4, grasas_100g = 0),
    ComidaEntity(nombre = "Lechuga romana", calorias_100g = 17, proteinas_100g = 1, carbohidratos_100g = 3, grasas_100g = 0),
    ComidaEntity(nombre = "Tomate", calorias_100g = 18, proteinas_100g = 1, carbohidratos_100g = 4, grasas_100g = 0),
    ComidaEntity(nombre = "Pepino", calorias_100g = 15, proteinas_100g = 1, carbohidratos_100g = 4, grasas_100g = 0),
    // Lácteos
    ComidaEntity(nombre = "Leche entera", calorias_100g = 61, proteinas_100g = 3, carbohidratos_100g = 5, grasas_100g = 3),
    ComidaEntity(nombre = "Leche desnatada", calorias_100g = 35, proteinas_100g = 4, carbohidratos_100g = 5, grasas_100g = 0),
)
package com.example.proyectazo.data.local.SmartFitDatabase

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
        @Volatile private var INSTANCE: SmartFitDatabase? = null

        fun getInstance(context: Context): SmartFitDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SmartFitDatabase::class.java,
                    "smartfit.db"
                )
                    .addCallback(SeedCallback)
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private object SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    database.ejercicioDao().insertarTodos(seedEjercicios())
                    database.comidaDao().insertarTodas(seedComidas())
                }
            }
        }
    }
}

// ── SEED DATA ─────────────────────────────────────────────────────────────────

private fun seedEjercicios(): List<EjercicioEntity> = listOf(
    // Pecho
    EjercicioEntity(nombre = "Press de banca",          grupo_muscular = "Pecho",     equipamiento = "Barra",      descripcion = "Tumbado en banco, baja la barra al pecho y empuja hacia arriba.", imagen = "https://smartfit-exercises.s3.amazonaws.com/press_banca.jpg"),
    EjercicioEntity(nombre = "Press inclinado",         grupo_muscular = "Pecho",     equipamiento = "Barra",      descripcion = "Press en banco inclinado para la parte superior del pecho.", imagen = "https://smartfit-exercises.s3.amazonaws.com/press_inclinado.jpg"),
    EjercicioEntity(nombre = "Aperturas con mancuernas",grupo_muscular = "Pecho",     equipamiento = "Mancuernas", descripcion = "Tumbado, abre los brazos en arco hasta sentir el pecho estirado.", imagen = "https://smartfit-exercises.s3.amazonaws.com/aperturas.jpg"),
    EjercicioEntity(nombre = "Fondos en paralelas",     grupo_muscular = "Pecho",     equipamiento = "Peso corporal", descripcion = "Baja el cuerpo entre las paralelas y empuja hacia arriba.", imagen = "https://smartfit-exercises.s3.amazonaws.com/fondos.jpg"),
    EjercicioEntity(nombre = "Flexiones",               grupo_muscular = "Pecho",     equipamiento = "Peso corporal", descripcion = "Posición de plancha, baja el pecho al suelo y sube.", imagen = "https://smartfit-exercises.s3.amazonaws.com/flexiones.jpg"),
    // Espalda
    EjercicioEntity(nombre = "Dominadas",               grupo_muscular = "Espalda",   equipamiento = "Peso corporal", descripcion = "Agarra la barra y sube hasta que el mentón supere la barra.", imagen = "https://smartfit-exercises.s3.amazonaws.com/dominadas.jpg"),
    EjercicioEntity(nombre = "Remo con barra",          grupo_muscular = "Espalda",   equipamiento = "Barra",      descripcion = "Inclinado hacia adelante, tira la barra hacia el abdomen.", imagen = "https://smartfit-exercises.s3.amazonaws.com/remo_barra.jpg"),
    EjercicioEntity(nombre = "Jalón al pecho",          grupo_muscular = "Espalda",   equipamiento = "Máquina",    descripcion = "Sentado en polea, tira la barra hacia el pecho.", imagen = "https://smartfit-exercises.s3.amazonaws.com/jalon_pecho.jpg"),
    EjercicioEntity(nombre = "Remo con mancuerna",      grupo_muscular = "Espalda",   equipamiento = "Mancuernas", descripcion = "Apoyado en banco, tira la mancuerna hacia la cadera.", imagen = "https://smartfit-exercises.s3.amazonaws.com/remo_mancuerna.jpg"),
    EjercicioEntity(nombre = "Peso muerto",             grupo_muscular = "Espalda",   equipamiento = "Barra",      descripcion = "Levanta la barra del suelo manteniendo la espalda recta.", imagen = "https://smartfit-exercises.s3.amazonaws.com/peso_muerto.jpg"),
    // Hombros
    EjercicioEntity(nombre = "Press militar",           grupo_muscular = "Hombros",   equipamiento = "Barra",      descripcion = "De pie o sentado, empuja la barra por encima de la cabeza.", imagen = "https://smartfit-exercises.s3.amazonaws.com/press_militar.jpg"),
    EjercicioEntity(nombre = "Elevaciones laterales",   grupo_muscular = "Hombros",   equipamiento = "Mancuernas", descripcion = "Levanta las mancuernas a los lados hasta la altura del hombro.", imagen = "https://smartfit-exercises.s3.amazonaws.com/elevaciones_laterales.jpg"),
    EjercicioEntity(nombre = "Pájaros",                 grupo_muscular = "Hombros",   equipamiento = "Mancuernas", descripcion = "Inclinado, eleva las mancuernas hacia los lados.", imagen = "https://smartfit-exercises.s3.amazonaws.com/pajaros.jpg"),
    EjercicioEntity(nombre = "Elevaciones frontales",   grupo_muscular = "Hombros",   equipamiento = "Mancuernas", descripcion = "Levanta las mancuernas al frente hasta la altura del hombro.", imagen = "https://smartfit-exercises.s3.amazonaws.com/elevaciones_frontales.jpg"),
    // Bíceps
    EjercicioEntity(nombre = "Curl de bíceps",          grupo_muscular = "Bíceps",    equipamiento = "Barra",      descripcion = "De pie, flexiona los codos para subir la barra.", imagen = "https://smartfit-exercises.s3.amazonaws.com/curl_biceps.jpg"),
    EjercicioEntity(nombre = "Curl con mancuernas",     grupo_muscular = "Bíceps",    equipamiento = "Mancuernas", descripcion = "Alterna o simultáneo, flexiona los codos con mancuernas.", imagen = "https://smartfit-exercises.s3.amazonaws.com/curl_mancuernas.jpg"),
    EjercicioEntity(nombre = "Curl martillo",           grupo_muscular = "Bíceps",    equipamiento = "Mancuernas", descripcion = "Agarre neutro, flexiona los codos hacia los hombros.", imagen = "https://smartfit-exercises.s3.amazonaws.com/curl_martillo.jpg"),
    EjercicioEntity(nombre = "Curl en polea baja",      grupo_muscular = "Bíceps",    equipamiento = "Máquina",    descripcion = "De pie frente a la polea baja, tira hacia arriba.", imagen = "https://smartfit-exercises.s3.amazonaws.com/curl_polea.jpg"),
    // Tríceps
    EjercicioEntity(nombre = "Press francés",           grupo_muscular = "Tríceps",   equipamiento = "Barra",      descripcion = "Tumbado, baja la barra a la frente y extiende los codos.", imagen = "https://smartfit-exercises.s3.amazonaws.com/press_frances.jpg"),
    EjercicioEntity(nombre = "Extensiones en polea",    grupo_muscular = "Tríceps",   equipamiento = "Máquina",    descripcion = "De pie, empuja la cuerda hacia abajo extendiendo los codos.", imagen = "https://smartfit-exercises.s3.amazonaws.com/extensiones_polea.jpg"),
    EjercicioEntity(nombre = "Fondos en banco",         grupo_muscular = "Tríceps",   equipamiento = "Peso corporal", descripcion = "Manos en banco, baja y sube el cuerpo flexionando los codos.", imagen = "https://smartfit-exercises.s3.amazonaws.com/fondos_banco.jpg"),
    EjercicioEntity(nombre = "Patada de tríceps",       grupo_muscular = "Tríceps",   equipamiento = "Mancuernas", descripcion = "Inclinado, extiende el codo hacia atrás con mancuerna.", imagen = "https://smartfit-exercises.s3.amazonaws.com/patada_triceps.jpg"),
    // Piernas
    EjercicioEntity(nombre = "Sentadilla",              grupo_muscular = "Piernas",   equipamiento = "Barra",      descripcion = "Con barra en trapecios, baja hasta que los muslos queden paralelos al suelo.", imagen = "https://smartfit-exercises.s3.amazonaws.com/sentadilla.jpg"),
    EjercicioEntity(nombre = "Prensa de piernas",       grupo_muscular = "Piernas",   equipamiento = "Máquina",    descripcion = "Sentado en la máquina, empuja la plataforma con los pies.", imagen = "https://smartfit-exercises.s3.amazonaws.com/prensa.jpg"),
    EjercicioEntity(nombre = "Extensión de cuádriceps", grupo_muscular = "Piernas",   equipamiento = "Máquina",    descripcion = "Sentado, extiende las rodillas contra la resistencia.", imagen = "https://smartfit-exercises.s3.amazonaws.com/extension_cuad.jpg"),
    EjercicioEntity(nombre = "Curl femoral",            grupo_muscular = "Piernas",   equipamiento = "Máquina",    descripcion = "Tumbado boca abajo, flexiona las rodillas contra la resistencia.", imagen = "https://smartfit-exercises.s3.amazonaws.com/curl_femoral.jpg"),
    EjercicioEntity(nombre = "Zancadas",                grupo_muscular = "Piernas",   equipamiento = "Mancuernas", descripcion = "Da un paso adelante y baja la rodilla trasera al suelo.", imagen = "https://smartfit-exercises.s3.amazonaws.com/zancadas.jpg"),
    EjercicioEntity(nombre = "Elevación de talones",    grupo_muscular = "Piernas",   equipamiento = "Máquina",    descripcion = "De pie, eleva los talones para trabajar los gemelos.", imagen = "https://smartfit-exercises.s3.amazonaws.com/elevacion_talones.jpg"),
    EjercicioEntity(nombre = "Sentadilla búlgara",      grupo_muscular = "Piernas",   equipamiento = "Mancuernas", descripcion = "Pie trasero en banco, baja el cuerpo con el pie delantero.", imagen = "https://smartfit-exercises.s3.amazonaws.com/sentadilla_bulgara.jpg"),
    // Abdomen
    EjercicioEntity(nombre = "Crunch abdominal",        grupo_muscular = "Abdomen",   equipamiento = "Peso corporal", descripcion = "Tumbado, eleva el torso hacia las rodillas.", imagen = "https://smartfit-exercises.s3.amazonaws.com/crunch.jpg"),
    EjercicioEntity(nombre = "Plancha",                 grupo_muscular = "Abdomen",   equipamiento = "Peso corporal", descripcion = "Apoyado en antebrazos y pies, mantén el cuerpo recto.", imagen = "https://smartfit-exercises.s3.amazonaws.com/plancha.jpg"),
    EjercicioEntity(nombre = "Elevación de piernas",    grupo_muscular = "Abdomen",   equipamiento = "Peso corporal", descripcion = "Tumbado, sube las piernas rectas hasta 90 grados.", imagen = "https://smartfit-exercises.s3.amazonaws.com/elevacion_piernas.jpg"),
    EjercicioEntity(nombre = "Rueda abdominal",         grupo_muscular = "Abdomen",   equipamiento = "Accesorios", descripcion = "Arrodillado, rueda hacia adelante y vuelve.", imagen = "https://smartfit-exercises.s3.amazonaws.com/rueda_abdominal.jpg"),
    EjercicioEntity(nombre = "Crunch en polea",         grupo_muscular = "Abdomen",   equipamiento = "Máquina",    descripcion = "De rodillas, tira la cuerda hacia abajo flexionando el torso.", imagen = "https://smartfit-exercises.s3.amazonaws.com/crunch_polea.jpg"),
    EjercicioEntity(nombre = "Mountain climbers",       grupo_muscular = "Abdomen",   equipamiento = "Peso corporal", descripcion = "En posición de plancha, alterna rodillas al pecho.", imagen = "https://smartfit-exercises.s3.amazonaws.com/mountain_climbers.jpg")
)

private fun seedComidas(): List<ComidaEntity> = listOf(
    // Proteínas
    ComidaEntity(nombre = "Pechuga de pollo",       calorias_100g = 165, proteinas_100g = 31, carbohidratos_100g = 0,  grasas_100g = 4,  imagen = "https://smartfit-foods.s3.amazonaws.com/pollo.jpg"),
    ComidaEntity(nombre = "Atún en lata",           calorias_100g = 116, proteinas_100g = 26, carbohidratos_100g = 0,  grasas_100g = 1,  imagen = "https://smartfit-foods.s3.amazonaws.com/atun.jpg"),
    ComidaEntity(nombre = "Salmón",                 calorias_100g = 208, proteinas_100g = 20, carbohidratos_100g = 0,  grasas_100g = 13, imagen = "https://smartfit-foods.s3.amazonaws.com/salmon.jpg"),
    ComidaEntity(nombre = "Huevos",                 calorias_100g = 155, proteinas_100g = 13, carbohidratos_100g = 1,  grasas_100g = 11, imagen = "https://smartfit-foods.s3.amazonaws.com/huevos.jpg"),
    ComidaEntity(nombre = "Ternera magra",          calorias_100g = 250, proteinas_100g = 26, carbohidratos_100g = 0,  grasas_100g = 15, imagen = "https://smartfit-foods.s3.amazonaws.com/ternera.jpg"),
    ComidaEntity(nombre = "Claras de huevo",        calorias_100g = 52,  proteinas_100g = 11, carbohidratos_100g = 1,  grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/claras.jpg"),
    ComidaEntity(nombre = "Pavo en lonchas",        calorias_100g = 109, proteinas_100g = 22, carbohidratos_100g = 1,  grasas_100g = 2,  imagen = "https://smartfit-foods.s3.amazonaws.com/pavo.jpg"),
    ComidaEntity(nombre = "Queso cottage",          calorias_100g = 98,  proteinas_100g = 11, carbohidratos_100g = 3,  grasas_100g = 4,  imagen = "https://smartfit-foods.s3.amazonaws.com/cottage.jpg"),
    ComidaEntity(nombre = "Greek yogurt 0%",        calorias_100g = 59,  proteinas_100g = 10, carbohidratos_100g = 4,  grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/greek_yogurt.jpg"),
    ComidaEntity(nombre = "Proteína whey",          calorias_100g = 380, proteinas_100g = 75, carbohidratos_100g = 8,  grasas_100g = 5,  imagen = "https://smartfit-foods.s3.amazonaws.com/whey.jpg"),
    // Carbohidratos
    ComidaEntity(nombre = "Arroz blanco",           calorias_100g = 130, proteinas_100g = 3,  carbohidratos_100g = 28, grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/arroz.jpg"),
    ComidaEntity(nombre = "Avena",                  calorias_100g = 389, proteinas_100g = 17, carbohidratos_100g = 66, grasas_100g = 7,  imagen = "https://smartfit-foods.s3.amazonaws.com/avena.jpg"),
    ComidaEntity(nombre = "Pasta integral",         calorias_100g = 150, proteinas_100g = 6,  carbohidratos_100g = 29, grasas_100g = 1,  imagen = "https://smartfit-foods.s3.amazonaws.com/pasta.jpg"),
    ComidaEntity(nombre = "Pan integral",           calorias_100g = 247, proteinas_100g = 9,  carbohidratos_100g = 41, grasas_100g = 3,  imagen = "https://smartfit-foods.s3.amazonaws.com/pan.jpg"),
    ComidaEntity(nombre = "Patata cocida",          calorias_100g = 87,  proteinas_100g = 2,  carbohidratos_100g = 20, grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/patata.jpg"),
    ComidaEntity(nombre = "Plátano",                calorias_100g = 89,  proteinas_100g = 1,  carbohidratos_100g = 23, grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/platano.jpg"),
    ComidaEntity(nombre = "Batata",                 calorias_100g = 86,  proteinas_100g = 2,  carbohidratos_100g = 20, grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/batata.jpg"),
    ComidaEntity(nombre = "Quinoa",                 calorias_100g = 120, proteinas_100g = 4,  carbohidratos_100g = 21, grasas_100g = 2,  imagen = "https://smartfit-foods.s3.amazonaws.com/quinoa.jpg"),
    ComidaEntity(nombre = "Manzana",                calorias_100g = 52,  proteinas_100g = 0,  carbohidratos_100g = 14, grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/manzana.jpg"),
    // Grasas saludables
    ComidaEntity(nombre = "Aguacate",               calorias_100g = 160, proteinas_100g = 2,  carbohidratos_100g = 9,  grasas_100g = 15, imagen = "https://smartfit-foods.s3.amazonaws.com/aguacate.jpg"),
    ComidaEntity(nombre = "Almendras",              calorias_100g = 579, proteinas_100g = 21, carbohidratos_100g = 22, grasas_100g = 50, imagen = "https://smartfit-foods.s3.amazonaws.com/almendras.jpg"),
    ComidaEntity(nombre = "Aceite de oliva",        calorias_100g = 884, proteinas_100g = 0,  carbohidratos_100g = 0,  grasas_100g = 100,imagen = "https://smartfit-foods.s3.amazonaws.com/aceite.jpg"),
    ComidaEntity(nombre = "Nueces",                 calorias_100g = 654, proteinas_100g = 15, carbohidratos_100g = 14, grasas_100g = 65, imagen = "https://smartfit-foods.s3.amazonaws.com/nueces.jpg"),
    ComidaEntity(nombre = "Mantequilla de cacahuete",calorias_100g= 588, proteinas_100g = 25, carbohidratos_100g = 20, grasas_100g = 50, imagen = "https://smartfit-foods.s3.amazonaws.com/mantequilla_cacahuete.jpg"),
    ComidaEntity(nombre = "Semillas de chía",       calorias_100g = 486, proteinas_100g = 17, carbohidratos_100g = 42, grasas_100g = 31, imagen = "https://smartfit-foods.s3.amazonaws.com/chia.jpg"),
    // Verduras
    ComidaEntity(nombre = "Brócoli",                calorias_100g = 34,  proteinas_100g = 3,  carbohidratos_100g = 7,  grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/brocoli.jpg"),
    ComidaEntity(nombre = "Espinacas",              calorias_100g = 23,  proteinas_100g = 3,  carbohidratos_100g = 4,  grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/espinacas.jpg"),
    ComidaEntity(nombre = "Pepino",                 calorias_100g = 16,  proteinas_100g = 1,  carbohidratos_100g = 4,  grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/pepino.jpg"),
    ComidaEntity(nombre = "Tomate",                 calorias_100g = 18,  proteinas_100g = 1,  carbohidratos_100g = 4,  grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/tomate.jpg"),
    ComidaEntity(nombre = "Lechuga",                calorias_100g = 15,  proteinas_100g = 1,  carbohidratos_100g = 3,  grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/lechuga.jpg"),
    // Lácteos y extras
    ComidaEntity(nombre = "Leche desnatada",        calorias_100g = 34,  proteinas_100g = 3,  carbohidratos_100g = 5,  grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/leche.jpg"),
    ComidaEntity(nombre = "Queso fresco 0%",        calorias_100g = 70,  proteinas_100g = 12, carbohidratos_100g = 3,  grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/queso_fresco.jpg"),
    ComidaEntity(nombre = "Lentejas cocidas",       calorias_100g = 116, proteinas_100g = 9,  carbohidratos_100g = 20, grasas_100g = 0,  imagen = "https://smartfit-foods.s3.amazonaws.com/lentejas.jpg"),
    ComidaEntity(nombre = "Garbanzos cocidos",      calorias_100g = 164, proteinas_100g = 9,  carbohidratos_100g = 27, grasas_100g = 3,  imagen = "https://smartfit-foods.s3.amazonaws.com/garbanzos.jpg"),
    ComidaEntity(nombre = "Arroz integral",         calorias_100g = 111, proteinas_100g = 3,  carbohidratos_100g = 23, grasas_100g = 1,  imagen = "https://smartfit-foods.s3.amazonaws.com/arroz_integral.jpg"),
    ComidaEntity(nombre = "Edamame",                calorias_100g = 122, proteinas_100g = 11, carbohidratos_100g = 10, grasas_100g = 5,  imagen = "https://smartfit-foods.s3.amazonaws.com/edamame.jpg")
)

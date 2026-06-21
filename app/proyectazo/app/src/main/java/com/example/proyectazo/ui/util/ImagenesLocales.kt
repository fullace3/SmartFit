package com.example.proyectazo.ui.util

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Resuelve el campo `imagen` de EjercicioEntity/ComidaEntity (que guarda solo
 * el nombre del recurso, ej. "barra_press_de_banca") al id real de R.drawable.
 *
 * Antes el campo `imagen` era una URL de S3 y se cargaba con Coil (AsyncImage).
 * Ahora es un recurso local embebido en la APK, así que se resuelve con
 * getIdentifier() y se pinta con painterResource(id).
 *
 * Cachea los resultados en memoria porque getIdentifier() usa reflexión
 * sobre R.drawable y no es gratis — evita repetir la búsqueda en cada
 * recomposición de una lista larga (ej. ListaEjerciciosScreen).
 */
object ImagenesLocales {

    private val cache = HashMap<String, Int>()

    @DrawableRes
    fun resolver(context: Context, nombreRecurso: String?): Int? {
        if (nombreRecurso.isNullOrBlank()) return null

        cache[nombreRecurso]?.let { return it }

        val id = context.resources.getIdentifier(
            nombreRecurso,
            "drawable",
            context.packageName
        )
        if (id == 0) return null // No existe ese drawable — evita crash, deja que la UI muestre el fallback

        cache[nombreRecurso] = id
        return id
    }
}

/**
 * Versión Composable de conveniencia — usa el LocalContext automáticamente.
 * Ejemplo de uso en una screen:
 *
 *   val resId = rememberDrawableId(ejercicio.imagen)
 *   if (resId != null) {
 *       Image(painter = painterResource(id = resId), contentDescription = ejercicio.nombre)
 *   } else {
 *       // fallback: icono o inicial del nombre, como ya hacía el código original
 *   }
 */
@Composable
fun rememberDrawableId(nombreRecurso: String?): Int? {
    val context = LocalContext.current
    return ImagenesLocales.resolver(context, nombreRecurso)
}
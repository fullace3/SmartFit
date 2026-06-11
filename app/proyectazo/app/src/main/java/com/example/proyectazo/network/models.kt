package com.example.proyectazo.network

/**
 * Data Transfer Objects (DTOs) for the SmartFit API.
 * Request classes define what the app sends to the server.
 * Response classes define what the app receives and maps to UI state.
 * All fields use snake_case to match the FastAPI JSON field names directly.
 */

// ── USUARIO ───────────────────────────────────
data class LoginRequest(
    val nombre: String,
    val password: String // Plain text — transmitted over HTTPS, never stored locally
)

data class UsuarioRequest(
    val nombre: String,
    val email: String,
    val password: String // Sent as plain text, hashed by the API before storing
)

data class UsuarioResponse(
    val id_usuario: Int,
    val nombre: String,
    val email: String,
    val fecha_registro: String // International standard format string (ISO 8601) — parsed to Date only if required by the UI.
)

data class TokenResponse(
    val access_token: String, // JWT token stored in SharedPreferences (smartfit_session)
    val token_type: String, // Always "bearer" — required by the OAuth2 standard
    val id_usuario: Int // Stored locally so the app can make user-specific requests
)

// ── EJERCICIO ─────────────────────────────────
data class EjercicioResponse(
    val id_ejercicio: Int,
    val nombre: String,
    val grupo_muscular: String?, // Nullable — used for filtering, may not be set for all exercises
    val equipamiento: String?, // Nullable — used for filtering by required equipment
    val descripcion: String?,
    val imagen: String? // URL pointing to the image stored in S3
)

// ── RUTINA ────────────────────────────────────
data class RutinaRequest(
    val nombre: String,
    val id_usuario: Int
)

data class RutinaResponse(
    val id_rutina: Int,
    val nombre: String,
    val id_usuario: Int
)

// ── RUTINA-EJERCICIO ──────────────────────────
data class RutinaEjercicioRequest(
    val id_rutina: Int,
    val id_ejercicio: Int,
    val series: Int = 3, // Default values match the API defaults
    val repeticiones: Int = 10,
    val orden: Int // Controls the display order within the routine
)

data class RutinaEjercicioResponse(
    val id_rutina: Int,
    val id_ejercicio: Int,
    val series: Int,
    val repeticiones: Int,
    val orden: Int
)

// ── DIETA ─────────────────────────────────────
data class DietaRequest(
    val nombre: String,
    val objetivo_calorico: Int,
    val proteinas_g: Double,
    val carbohidratos_g: Double,
    val grasas_g: Double,
    val fecha_inicio: String,       // ISO 8601 (International Standard) format: "2025-01-01T00:00:00"
    val fecha_fin: String? = null,
    val id_usuario: Int
)

data class DietaResponse(
    val id_dieta: Int,
    val nombre: String,
    val objetivo_calorico: Int,
    val proteinas_g: Double,
    val carbohidratos_g: Double,
    val grasas_g: Double,
    val fecha_inicio: String,
    val fecha_fin: String?,
    val id_usuario: Int,
    val activo: Boolean = false // Only one diet per user can be active at a time
)

// ── DIETA-COMIDA ─────────────────────────────
data class DietaComidaRequest(
    val id_dieta: Int,
    val id_comida: Int,
    val tipo: String, // Expected values: "Desayuno", "Comida" or "Cena"
    val dia: String // Expected values: "Lunes", "Martes", etc.
)

data class DietaComidaResponse(
    val id: Int,
    val id_dieta: Int,
    val id_comida: Int,
    val tipo: String,
    val dia: String,
    val comida: ComidaResponse? = null  // Nested object returned by the API — avoids a second request
)

// ── COMIDA ────────────────────────────────────
data class ComidaRequest(
    val nombre: String,
    val calorias_100g: Int, // All nutritional values are per 100g for consistency
    val proteinas_100g: Int,
    val carbohidratos_100g: Int,
    val grasas_100g: Int,
    val id_usuario: Int,
    val imagen: String? = null // Optional URL — not all food items have an image
)

data class ComidaResponse(
    val id_comida: Int,
    val nombre: String,
    val calorias_100g: Int,
    val proteinas_100g: Int,
    val carbohidratos_100g: Int,
    val grasas_100g: Int,
    val id_usuario: Int?, // Nullable — global foods (not user-created) have no owner
    val imagen: String?,
    val dia: String? = null // Only populated when returned inside a DietaComidaResponse
)

// ── MEDIDAS ───────────────────────────────────
data class MedidaRequest(
    val id_usuario: Int,
    val peso_kg: Double,
    val altura_cm: Double? = null,
    val pecho_cm: Double? = null,
    val pierna_cm: Double? = null,
    val brazo_cm: Double? = null,
    val grasa_corporal_pct: Double? = null,
    val edad: Int? = null,
    val sexo: String? = null // Expected values: "M" or "F"
)

data class MedidaResponse(
    val id_medida: Int,
    val id_usuario: Int,
    val fecha: String, // Set automatically by the server on creation
    val peso_kg: Double,
    val altura_cm: Double?,
    val pecho_cm: Double?,
    val pierna_cm: Double?,
    val brazo_cm: Double?,
    val grasa_corporal_pct: Double?,
    val edad: Int?,
    val sexo: String?
)

// ── PROGRESO ──────────────────────────────────
data class ProgresoResponse(
    val fecha: String, // Format: "YYYY-MM-DD"
    val volumen: Double // Calculated by the API: weight × reps × sets
)

// ── Historial ──────────────────────────────────
data class HistorialRequest(
    val id_usuario: Int,
    val id_ejercicio: Int,
    val id_rutina: Int,
    val peso_kg: Double,
    val repeticiones: Int,
    val series: Int,
    val duracion_minutos: Int = 0 // Defaults to 0 if the user does not track session duration
)

data class HistorialDetalleResponse(
    val id_registro: Int,
    val id_ejercicio: Int,
    val id_rutina: Int,
    val nombre_ejercicio: String, // Joined from the Ejercicio table by the API — not stored in history
    val peso_kg: Double,
    val repeticiones: Int,
    val series: Int,
    val duracion_minutos: Int,
    val fecha: String   // ISO 8601 (International Standard) format: "2025-01-01T00:00:00"
)

// ── UI MODELS ─────────────────────────────────
/**
 * Local UI model used to display exercises within a routine.
 * Not sent to or received from the API — only used internally by the app.
 * Combines data from EjercicioResponse and RutinaEjercicioResponse into a single object.
 */
data class EjercicioRutina(
    val id: Int,
    val nombre: String,
    val series: Int = 3,
    val repeticiones: Int = 10,
    val imagenUrl: String = ""
)
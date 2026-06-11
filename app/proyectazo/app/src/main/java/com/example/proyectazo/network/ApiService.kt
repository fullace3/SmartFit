package com.example.proyectazo.network

import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface that defines all HTTP endpoints consumed by the app.
 * Each method maps to a FastAPI route on the EC2 server.
 * All methods are suspend functions so they run on a coroutine (never on the main thread).
 */

interface ApiService {

    // ── USUARIOS ──────────────────────────────
    @POST("usuarios/registro")
    suspend fun registro(@Body datos: UsuarioRequest): Response<UsuarioResponse>

    // Sends credentials in the request body and receives a JWT token in return.
    // @Body serializes the LoginRequest object to JSON automatically.
    @POST("usuarios/login")
    suspend fun login(@Body datos: LoginRequest): Response<TokenResponse>

    // Fetches a user's profile by their ID.
    // @Path("id") replaces the {id} placeholder in the URL with the actual value.
    // Example: getUsuario(3) → GET usuarios/3
    @GET("usuarios/{id}")
    suspend fun getUsuario(@Path("id") id: Int): Response<UsuarioResponse>

    // Updates a user's profile combining both @Path and @Body in the same call.
    // @Path("id") identifies WHICH user to update in the URL.
    // @Body carries the NEW data to replace the existing profile.
    // Example: actualizarUsuario(3, datos) → PUT usuarios/3 + JSON body
    @PUT("usuarios/{id}")
    suspend fun actualizarUsuario(
        @Path("id") id: Int,
        @Body datos: UsuarioRequest
    ): Response<UsuarioResponse>

    @DELETE("usuarios/{id}")
    suspend fun eliminarUsuario(@Path("id") id: Int): Response<Unit> // Returns no body on success (204)

    // ── EJERCICIOS ────────────────────────────
    @GET("ejercicios")
    suspend fun getEjercicios(): Response<List<EjercicioResponse>>

    @GET("ejercicios/grupo/{grupo}")
    suspend fun getEjerciciosPorGrupo(
        @Path("grupo") grupo: String // Used by the filter panel in the exercise list screen
    ): Response<List<EjercicioResponse>>

    // ── RUTINAS ───────────────────────────────
    @GET("rutinas/usuario/{id}")
    suspend fun getRutinas(@Path("id") userId: Int): Response<List<RutinaResponse>>

    @POST("rutinas")
    suspend fun crearRutina(@Body datos: RutinaRequest): Response<RutinaResponse>

    @PUT("rutinas/{id}")
    suspend fun editarRutina(
        @Path("id") id: Int,
        @Body datos: RutinaRequest
    ): Response<RutinaResponse>

    @DELETE("rutinas/{id}")
    suspend fun borrarRutina(@Path("id") id: Int): Response<Unit> // Composite key — both IDs required to identify the relation

    // ── RUTINA-EJERCICIO ──────────────────────
    @GET("rutinas/{id}/ejercicios")
    suspend fun getEjerciciosDeRutina(
        @Path("id") rutinaId: Int
    ): Response<List<RutinaEjercicioResponse>>

    @POST("rutinas/ejercicios")
    suspend fun añadirEjercicioARutina(
        @Body datos: RutinaEjercicioRequest
    ): Response<RutinaEjercicioResponse>

    @DELETE("rutinas/{rutinaId}/ejercicios/{ejercicioId}")
    suspend fun quitarEjercicioDeRutina(
        @Path("rutinaId") rutinaId: Int,
        @Path("ejercicioId") ejercicioId: Int // Composite key — both IDs required to identify the relation
    ): Response<Unit>

    // ── HISTORIAL ─────────────────────────────
    @POST("historial")
    suspend fun registrarHistorial(@Body datos: HistorialRequest): Response<HistorialDetalleResponse>

    @GET("historial/usuario/{id}")
    suspend fun getHistorial(@Path("id") userId: Int): Response<List<HistorialDetalleResponse>>

    // ── DIETA ─────────────────────────────────
    @GET("dietas/usuario/{id}/actual")
    suspend fun getDietaActual(@Path("id") userId: Int): Response<DietaResponse> // Most recently created diet

    @GET("dietas/usuario/{id}/activa")
    suspend fun getDietaActiva(@Path("id") userId: Int): Response<DietaResponse> // Diet explicitly marked as active

    @GET("dietas/usuario/{id}")
    suspend fun getDietasUsuario(@Path("id") userId: Int): Response<List<DietaResponse>>

    @POST("dietas")
    suspend fun crearDieta(@Body datos: DietaRequest): Response<DietaResponse>

    @DELETE("dietas/{id}")
    suspend fun borrarDieta(@Path("id") dietaId: Int): Response<Unit>

    @PUT("dietas/{id}")
    suspend fun actualizarDieta(@Path("id") dietaId: Int, @Body datos: DietaRequest): Response<DietaResponse>
    @PUT("dietas/{id}/activar")
    suspend fun activarDieta(@Path("id") dietaId: Int): Response<DietaResponse> // Deactivates all others automatically

    // ── COMIDAS ──────────────────────────────
    @GET("comidas")
    suspend fun getComidas(): Response<List<ComidaResponse>>

    @GET("comidas/{id}")
    suspend fun getComida(@Path("id") comidaId: Int): Response<ComidaResponse>

    // ── DIETA-COMIDA ────────────────────────
    @GET("dieta-comida/dieta/{id}")
    suspend fun getComidasDeDieta(@Path("id") dietaId: Int): Response<List<DietaComidaResponse>>

    @POST("dieta-comida/")
    suspend fun añadirComidaADieta(@Body datos: DietaComidaRequest): Response<DietaComidaResponse>

    @DELETE("dieta-comida/{id}")
    suspend fun eliminarComidaDeDieta(@Path("id") id: Int): Response<Unit>

    // ── MEDIDAS CORPORALES ────────────────────
    @POST("medidas")
    suspend fun registrarMedida(@Body datos: MedidaRequest): Response<MedidaResponse>

    @GET("medidas/usuario/{id}")
    suspend fun getMedidas(@Path("id") userId: Int): Response<List<MedidaResponse>>
}
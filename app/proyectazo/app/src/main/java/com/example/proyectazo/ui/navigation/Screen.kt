package com.example.proyectazo.navigation

/**
 * Defines ALL app screens as sealed objects.
 * To add a new screen, just add a new object here.
 *
 * - No arguments  → object
 * - With arguments → data class with {arg} in the route
 */
sealed class Screen(val route: String) {

    // ── AUTH
    object Login    : Screen("login")
    object Register : Screen("register")

    // ── MAIN
    object Home     : Screen("home")
    object Rutinas  : Screen("rutinas")
    object Dieta    : Screen("dieta")
    object Progreso : Screen("progreso")
    object Perfil   : Screen("perfil")

    // ── RUTINAS FLOW
    object CrearRutina : Screen("crear_rutina")

    // ── WITH ARGUMENT
    // Use createRoute() to build the URL instead of string concatenation —
    // avoids typos and keeps argument names consistent with the NavGraph declaration.
    // Example: navController.navigate(Screen.DetalleRutina.createRoute(123))
    //          → navigates to "detalle_rutina/123"
    object DetalleRutina : Screen("detalle_rutina/{rutinaId}") {
        fun createRoute(rutinaId: Int) = "detalle_rutina/$rutinaId"
        const val ARG = "rutinaId" // Referenced in NavGraph to extract the argument safely
    }
}
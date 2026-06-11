package com.example.proyectazo.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.proyectazo.ui.screens.RutinasYEjercicio.AñadirEjercicioScreen
import com.example.proyectazo.ui.screens.ProgresoYRegistros.AñadirRegistroScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.CrearRutinaScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.DetalleEjercicioScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.DetallesRutinaScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.EditarRutinaScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.EntrenarScreen
import com.example.proyectazo.ui.screens.RutinasYEjercicio.FinalizarEntrenamientoScreen
import com.example.proyectazo.ui.screens.DietasYComidas.CrearDietaScreen
import com.example.proyectazo.ui.screens.DietasYComidas.DietaScreen
import com.example.proyectazo.ui.screens.DietasYComidas.ListaComidasScreen
import com.example.proyectazo.ui.screens.DietasYComidas.DetalleComidaScreen
import com.example.proyectazo.ui.screens.PerfilYAjustes.EditarPerfilScreen
import com.example.proyectazo.ui.screens.PerfilYAjustes.PantallaPerfil
import com.example.proyectazo.ui.screens.PerfilYAjustes.PreferenciasScreen
import com.example.proyectazo.ui.screens.PerfilYAjustes.TerminosCondicionesScreen
import com.example.proyectazo.ui.screens.ProgresoYRegistros.PantallaProgreso
import com.example.proyectazo.ui.screens.RutinasYEjercicio.ResultadoEntrenamiento
import com.example.proyectazo.ui.screens.Sesion.PantallaIncioSesion
import com.example.proyectazo.ui.screens.PantallaInicio
import com.example.proyectazo.ui.screens.Sesion.PantallaRegistro
import com.example.proyectazo.ui.screens.RutinasYEjercicio.PantallaRutinas
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.RutinaConEjercicios
import androidx.compose.runtime.collectAsState
import com.example.proyectazo.network.SessionManager
import com.example.proyectazo.ui.screens.Sesion.ConfiguracionInicialScreen
import com.example.proyectazo.ui.screens.PerfilYAjustes.HoraEntrenamientoScreen


/**
 * Main navigation graph for the SmartFit app.
 * Defines all screens and their routes, arguments and transitions.
 *
 * rutinaSeleccionada and resultadoEntrenamiento are held in NavGraph memory
 * instead of passed as route arguments because they are complex objects
 * that cannot be serialized into a URL string.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    userId: Int = 0,
    modifier: Modifier = Modifier
) {
    // Shared state passed between screens as in-memory objects instead of route arguments
    var rutinaSeleccionada by remember { mutableStateOf<RutinaConEjercicios?>(null) }
    var resultadoEntrenamiento by remember { mutableStateOf<ResultadoEntrenamiento?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // ── LOGIN
        composable(Screen.Login.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            PantallaIncioSesion(
                onLoginExitoso = {
                    val userId = com.example.proyectazo.network.SessionManager(context).getUserId()
                    // Check SharedPreferences to decide if the user has completed initial setup.
                    // This flag is stored separately from the session so it survives logout.
                    val configurado = context
                        .getSharedPreferences("smartfit_config", android.content.Context.MODE_PRIVATE)
                        .getBoolean("configuracion_completada_$userId", false)
                    android.util.Log.d("CONFIG_CHECK", "userId=$userId, configurado=$configurado")
                    if (configurado) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true } // Remove Login from back stack
                        }
                    } else {
                        navController.navigate("configuracion_inicial") {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }
        // ── REGISTER
        composable(Screen.Register.route) {
            PantallaRegistro(
                onRegistroExitoso = {
                    // After registering, go back to Login instead of Home —
                    // the user still needs to complete initial setup on first login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        // Shown only once after the first login — onBack is disabled intentionally
        // so the user cannot skip the setup flow
        composable("configuracion_inicial") {
            ConfiguracionInicialScreen(
                onBack = { /* Back disabled — setup must be completed */ },
                onConfigurado = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true } // Clear the entire back stack
                    }
                }
            )
        }

        // ── HOME
        composable(Screen.Home.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val sessionManager = remember { com.example.proyectazo.network.SessionManager(context) }
            PantallaInicio(usuario = sessionManager.getUserNombre())
        }

        // ── RUTINAS
        composable(Screen.Rutinas.route) {
            PantallaRutinas(
                onCrearRutina = { navController.navigate(Screen.CrearRutina.route) },
                onVerDetalles = { rutina ->
                    // Store the selected routine in memory before navigating —
                    // complex objects cannot be passed as route arguments
                    rutinaSeleccionada = rutina
                    navController.navigate("detalles_rutina")
                }
            )
        }

        // ── DETALLES RUTINA
        composable("detalles_rutina") {
            rutinaSeleccionada?.let { rutina ->
                DetallesRutinaScreen(
                    rutinaConEjercicios = rutina,
                    onBack = { navController.popBackStack() },
                    onEditar = { navController.navigate("editar_rutina/${rutina.rutina.id_rutina}") },
                    onEmpezar = { navController.navigate("entrenar") }
                )
            }
        }

        // ── ENTRENAR
        composable("entrenar") {
            rutinaSeleccionada?.let { rutina ->
                EntrenarScreen(
                    rutinaConEjercicios = rutina,
                    onTerminar = { resultado ->
                        // Remove the training screen from back stack so the user
                        // cannot go back to an in-progress workout after finishing
                        resultadoEntrenamiento = resultado
                        navController.navigate("finalizar_entrenamiento") {
                            popUpTo("entrenar") { inclusive = true }
                        }
                    }
                )
            }
        }

        // ── FINALIZAR ENTRENAMIENTO
        composable("finalizar_entrenamiento") {
            resultadoEntrenamiento?.let { resultado ->
                FinalizarEntrenamientoScreen(
                    resultado = resultado,
                    onGuardado = {
                        // Clear the entire back stack — user should not go back to the workout
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onEliminar = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ── CREAR RUTINA
        composable("crear_rutina") {
            val context = androidx.compose.ui.platform.LocalContext.current
            val sessionManager = remember { com.example.proyectazo.network.SessionManager(context) }
            CrearRutinaScreen(
                userId = sessionManager.getUserId(),
                onNavigateBack = { navController.popBackStack() },
                onAnadirEjercicio = { rutinaId ->
                    navController.navigate("seleccionar_ejercicio/$rutinaId")
                }
            )
        }

        // ── EDITAR RUTINA - rutinaId passed as a route argument (Int, safe to serialize)
        composable(
            route = "editar_rutina/{rutinaId}",
            arguments = listOf(navArgument("rutinaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val rutinaId = backStackEntry.arguments?.getInt("rutinaId") ?: 0
            EditarRutinaScreen(
                rutinaId = rutinaId,
                onNavigateBack = { navController.popBackStack() },
                onAnadirEjercicio = { id -> navController.navigate("seleccionar_ejercicio/$id") }
            )
        }

        // ── SELECCIONAR EJERCICIO
        composable(
            route = "seleccionar_ejercicio/{rutinaId}",
            arguments = listOf(navArgument("rutinaId") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            val rutinaId = backStackEntry.arguments?.getInt("rutinaId") ?: 0
            AñadirEjercicioScreen(
                rutinaId = rutinaId,
                onBack = { navController.popBackStack() }
            )
        }

        // ── DIETA
        composable(Screen.Dieta.route) { backStackEntry ->
            // savedStateHandle is used to trigger a reload when returning from CrearDietaScreen
            val recargar = backStackEntry.savedStateHandle.get<Boolean>("recargar") ?: false
            val context = androidx.compose.ui.platform.LocalContext.current
            val dietaViewModel: com.example.proyectazo.ui.viewmodel.DietaYComida.DietaViewModel = viewModel(
                factory = com.example.proyectazo.ui.viewmodel.DietaYComida.DietaViewModel.Factory(context)
            )

            LaunchedEffect(recargar) {
                if (recargar) {
                    dietaViewModel.cargar()
                    backStackEntry.savedStateHandle.remove<Boolean>("recargar")
                }
            }

            DietaScreen(
                onCrearDieta = { navController.navigate("crear_dieta") }
            )
        }

        // ── CREAR DIETA
        // Food data is passed back from DetalleComidaScreen via savedStateHandle —
        // this avoids having to pass complex objects as route arguments
        composable("crear_dieta") { backStackEntry ->
            val savedState = backStackEntry.savedStateHandle

            // Observe each field as a StateFlow so the screen recomposes when a food is selected
            val comidaId by savedState.getStateFlow<Int?>("comida_id", null).collectAsState()
            val comidaNombre by savedState.getStateFlow<String?>("comida_nombre", null).collectAsState()
            val comidaCalorias by savedState.getStateFlow<Int?>("comida_calorias", null).collectAsState()
            val comidaProteinas by savedState.getStateFlow<Int?>("comida_proteinas", null).collectAsState()
            val comidaCarbos by savedState.getStateFlow<Int?>("comida_carbos", null).collectAsState()
            val comidaGrasas by savedState.getStateFlow<Int?>("comida_grasas", null).collectAsState()
            val comidaTipo by savedState.getStateFlow<String?>("comida_tipo", null).collectAsState()
            val comidaDia by savedState.getStateFlow<String?>("comida_dia", null).collectAsState()

            val currentComidaId = comidaId
            val currentComidaNombre = comidaNombre

            CrearDietaScreen(
                onBack = { navController.popBackStack() },
                onGuardadoExitoso = {
                    // Signal the Dieta screen to reload its list before popping back
                    navController.previousBackStackEntry?.savedStateHandle?.set("recargar", true)
                    navController.popBackStack()
                },
                onAñadirAlimento = { tipo, dia ->
                    savedState["comida_tipo"] = tipo
                    savedState["comida_dia"] = dia
                    navController.navigate("lista_comidas")
                },
                comidaAñadida = if (currentComidaId != null && currentComidaNombre != null) {
                    Triple(currentComidaId, currentComidaNombre, comidaCalorias ?: 0)
                } else null,
                comidaAñadidaMacros = if (currentComidaId != null) {
                    Triple(comidaProteinas ?: 0, comidaCarbos ?: 0, comidaGrasas ?: 0)
                } else null,
                comidaTipo = comidaTipo,
                comidaDia = comidaDia,
                onComidaConsumida = {
                    // Clear all food data from savedStateHandle after it has been processed
                    // to prevent the same item from being added again on recomposition
                    savedState.remove<Int>("comida_id")
                    savedState.remove<String>("comida_nombre")
                    savedState.remove<Int>("comida_calorias")
                    savedState.remove<Int>("comida_proteinas")
                    savedState.remove<Int>("comida_carbos")
                    savedState.remove<Int>("comida_grasas")
                    savedState.remove<String>("comida_tipo")
                    savedState.remove<String>("comida_dia")
                }
            )
        }

        // ── LISTA COMIDAS
        // Acts as a relay: forwards the selected food data to CrearDietaScreen
        // via the previous back stack entry's savedStateHandle, then pops itself
        composable("lista_comidas") { backStackEntry ->
            val comidaId = backStackEntry.savedStateHandle.get<Int>("comida_id")

            LaunchedEffect(comidaId) {
                if (comidaId != null) {
                    // Forward all food data to the CrearDieta entry before popping
                    val prev = navController.previousBackStackEntry
                    prev?.savedStateHandle?.set("comida_id", comidaId)
                    prev?.savedStateHandle?.set("comida_nombre", backStackEntry.savedStateHandle.get<String>("comida_nombre"))
                    prev?.savedStateHandle?.set("comida_calorias", backStackEntry.savedStateHandle.get<Int>("comida_calorias"))
                    prev?.savedStateHandle?.set("comida_proteinas", backStackEntry.savedStateHandle.get<Int>("comida_proteinas"))
                    prev?.savedStateHandle?.set("comida_carbos", backStackEntry.savedStateHandle.get<Int>("comida_carbos"))
                    prev?.savedStateHandle?.set("comida_grasas", backStackEntry.savedStateHandle.get<Int>("comida_grasas"))
                    backStackEntry.savedStateHandle.remove<Int>("comida_id")
                    navController.popBackStack()
                }
            }
            // Only render the list if no food has been selected yet
            if (comidaId == null) {
                ListaComidasScreen(
                    onBack = { navController.popBackStack() },
                    onComidaSeleccionada = { id -> navController.navigate("detalle_comida/$id") }
                )
            }
        }

        // ── DETALLE COMIDA
        // Passes the selected food back to ListaComidasScreen via savedStateHandle
        composable(
            "detalle_comida/{comidaId}",
            arguments = listOf(navArgument("comidaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val comidaId = backStackEntry.arguments?.getInt("comidaId") ?: 0
            DetalleComidaScreen(
                comidaId = comidaId,
                onBack = { navController.popBackStack() },
                onAñadir = { id, nombre, calorias, proteinas, carbos, grasas ->
                    // Write food data into the previous screen's savedStateHandle before popping
                    navController.previousBackStackEntry?.savedStateHandle?.let { handle ->
                        handle["comida_id"] = id
                        handle["comida_nombre"] = nombre
                        handle["comida_calorias"] = calorias
                        handle["comida_proteinas"] = proteinas
                        handle["comida_carbos"] = carbos
                        handle["comida_grasas"] = grasas
                    }
                    navController.popBackStack()
                }
            )
        }

        // ── PROGRESO
        composable(Screen.Progreso.route) {
            PantallaProgreso(
                onAñadirRegistro = { navController.navigate("añadir_registro") }
            )
        }

        // ── AÑADIR REGISTRO
        composable("añadir_registro") {
            AñadirRegistroScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── PERFIL
        // The ViewModel is scoped to the Perfil back stack entry so it survives
        // navigation to EditarPerfil and back without being recreated
        composable(Screen.Perfil.route) { navBackStackEntry ->
            val context = androidx.compose.ui.platform.LocalContext.current
            val perfilEntry = remember(navBackStackEntry) {
                navController.getBackStackEntry(Screen.Perfil.route)
            }
            val perfilViewModel: com.example.proyectazo.ui.viewmodel.PerfilYAjustes.PerfilViewModel =
                viewModel(perfilEntry, factory = com.example.proyectazo.ui.viewmodel.PerfilYAjustes.PerfilViewModel.Factory(context))

            PantallaPerfil(
                viewModel = perfilViewModel,
                onEditarPerfil = { navController.navigate("editar_perfil") },
                onPreferencias = { navController.navigate("preferencias") },
                onCerrarSesion = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Clear everything — session is over
                    }
                }
            )
        }

        // ── EDITAR PERFIL
        // Shares the same ViewModel instance as Perfil so changes reflect immediately on return
        composable("editar_perfil") { navBackStackEntry ->
            val context = androidx.compose.ui.platform.LocalContext.current
            val perfilEntry = remember(navBackStackEntry) {
                navController.getBackStackEntry(Screen.Perfil.route)
            }
            val perfilViewModel: com.example.proyectazo.ui.viewmodel.PerfilYAjustes.PerfilViewModel =
                viewModel(perfilEntry, factory = com.example.proyectazo.ui.viewmodel.PerfilYAjustes.PerfilViewModel.Factory(context))

            EditarPerfilScreen(
                onBack = { navController.popBackStack() },
                onGuardadoExitoso = {
                    perfilViewModel.recargar() // Refresh profile data in the shared ViewModel before going back
                    navController.popBackStack()
                }
            )
        }

        // ── PREFERENCIAS
        composable("preferencias") {
            PreferenciasScreen(
                onBack = { navController.popBackStack() },
                onTerminos = { navController.navigate("terminos_condiciones") },
                onHoraEntrenamiento = { navController.navigate("hora_entrenamiento") },
                onEliminarCuenta = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("hora_entrenamiento") {
            HoraEntrenamientoScreen(onBack = { navController.popBackStack() })
        }

        // ── TÉRMINOS Y CONDICIONES
        composable("terminos_condiciones") {
            TerminosCondicionesScreen(onBack = { navController.popBackStack() })
        }

        // ── DETALLE RUTINA
        composable(
            route = Screen.DetalleRutina.route,
            arguments = listOf(navArgument(Screen.DetalleRutina.ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val rutinaId = backStackEntry.arguments?.getInt(Screen.DetalleRutina.ARG) ?: 0
            PlaceholderScreen(nombre = "Detalle Rutina $rutinaId")
        }
    }
}

// Temporary placeholder used for routes not yet implemented
@Composable
fun PlaceholderScreen(nombre: String) {
    Text(text = nombre)
}
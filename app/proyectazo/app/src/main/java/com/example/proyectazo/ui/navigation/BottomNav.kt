package com.example.proyectazo.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

// Screens where the bottom navigation bar should be hidden.
// These are full-screen flows (workout, diet creation, auth) that don't need tab navigation.
val screensWithoutNav = listOf(
    Screen.Login.route,
    Screen.Register.route,
    "crear_rutina",
    "seleccionar_ejercicio/{rutinaId}",
    "editar_rutina/{rutinaId}",
    "detalles_rutina",
    "entrenar",
    "finalizar_entrenamiento",
    "preferencias",
    "terminos_condiciones",
    "crear_dieta",
    "preferencias",
    "terminos_condiciones",
    "configuracion_inicial"
)

/**
 * Represents a single item in the bottom navigation bar.
 * Uses separate filled/outlined icons to indicate selected vs unselected state.
 */
data class NavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector, // Filled icon — shown when this tab is active
    val unselectedIcon: ImageVector // Outlined icon — shown when this tab is inactive
)

// The five main sections of the app accessible from the bottom bar
val bottomNavItems = listOf(
    NavItem("Inicio",     Screen.Home.route,    Icons.Filled.Home,         Icons.Outlined.Home),
    NavItem("Ejercicios", Screen.Rutinas.route, Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    NavItem("Dieta",      Screen.Dieta.route,   Icons.Filled.Restaurant,   Icons.Outlined.Restaurant),
    NavItem("Progreso", Screen.Progreso.route, Icons.Filled.BarChart, Icons.Outlined.BarChart),
    NavItem("Perfil",     Screen.Perfil.route,  Icons.Filled.Person,       Icons.Outlined.Person),
)

/**
 * Material 3 bottom navigation bar for the SmartFit app.
 * Highlights the current tab and handles navigation with back stack management.
 */
@Composable
fun SmartFitNavigationBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop back to the start destination to avoid building up a large back stack
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true // Remember the state of the tab we're leaving
                        }
                        launchSingleTop = true // Avoid creating a duplicate screen if already on this tab
                        restoreState    = true  // Restore the previous state when returning to a tab
                    }
                },
                icon = {
                    // Swap between filled and outlined icon based on selection state
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
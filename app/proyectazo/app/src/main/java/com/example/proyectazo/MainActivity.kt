package com.example.proyectazo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyectazo.navigation.NavGraph
import com.example.proyectazo.navigation.Screen
import com.example.proyectazo.navigation.SmartFitNavigationBar
import com.example.proyectazo.navigation.screensWithoutNav
import com.example.proyectazo.network.SessionManager
import com.example.proyectazo.ui.theme.ProyectazoTheme
import com.example.proyectazo.notificaciones.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Request POST_NOTIFICATIONS permission on Android 13+ (API 33 / TIRAMISU)
        // On older versions the permission is granted automatically at install time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Extends the app layout behind system bars (status bar, navigation bar)
        NotificationHelper.crearCanal(this) // Must be created before any notification is shown
        val sessionManager = SessionManager(this)
        setContent {
            ProyectazoTheme {
                // Decide the start screen based on whether a valid session token exists
                // Logged in → Home, not logged in Login
                SmartFitApp(
                    startDestination = if (sessionManager.isLoggedIn())
                        Screen.Home.route
                    else
                        Screen.Login.route,
                    userId = sessionManager.getUserId()
                )
            }
        }
    }
}

/**
 * Root composable of the app.
 * Sets up the NavController, the Scaffold structure and the animated bottom navigation bar.
 *
 * @param startDestination First screen shown — either Login or Home depending on session state.
 * @param userId The logged-in user's ID passed down to screens that need it.
 */
@Composable
fun SmartFitApp(
    startDestination: String,
    userId: Int = -1 // -1 means no active session
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // Hide the bottom bar on screens like Login, Register and initial setup
    val showBottomBar = currentRoute !in screensWithoutNav

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Animate the bottom bar sliding in/out instead of appearing abruptly
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(tween(300)),
                exit  = slideOutVertically(targetOffsetY  = { it }, animationSpec = tween(300)) + fadeOut(tween(300))
            ) {
                SmartFitNavigationBar(navController, currentRoute)
            }
        }
    ) { innerPadding ->
        // innerPadding prevents content from being hidden behind the bottom bar
        NavGraph(
            navController = navController,
            startDestination = startDestination,
            userId = userId,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
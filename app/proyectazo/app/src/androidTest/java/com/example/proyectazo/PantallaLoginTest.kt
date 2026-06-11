package com.example.proyectazo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyectazo.ui.screens.Sesion.PantallaIncioSesion
import com.example.proyectazo.ui.theme.ProyectazoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [PantallaIncioSesion].
 * These tests run on a device or emulator and verify the
 * UI tree structure and component behavior.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setupScreen() {
        composeTestRule.setContent {
            ProyectazoTheme {
                PantallaIncioSesion(
                    onLoginExitoso = {},
                    onRegisterClick = {}
                )
            }
        }
    }


    @Test
    fun loginScreen_camposVacios_muestraError() {
        setupScreen()
        composeTestRule
            .onAllNodes(hasSetTextAction())
            .fetchSemanticsNodes()
            .let { nodes ->
                assert(nodes.size >= 2) { "Se esperaban al menos 2 campos de texto, encontrados: ${nodes.size}" }
            }
    }

    @Test
    fun loginScreen_botonRegistro_esClickable() {
        var registroClicked = false
        composeTestRule.setContent {
            ProyectazoTheme {
                PantallaIncioSesion(
                    onLoginExitoso = {},
                    onRegisterClick = { registroClicked = true }
                )
            }
        }
        composeTestRule
            .onNodeWithText("Regístrate")
            .performClick()
        assert(registroClicked) { "El botón Regístrate no llamó a onRegisterClick" }
    }
}
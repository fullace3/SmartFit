package com.example.proyectazo.ui.screens.PerfilYAjustes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectazo.ui.components.SmartFitTopBar

/**
 * Displays legal attributions for third-party resources used in the app:
 * icon authors (Flaticon) and nutritional data source (USDA FoodData Central).
 */
@Composable
fun TerminosCondicionesScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SmartFitTopBar(titulo = "Términos y condiciones", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                """
https://www.flaticon.es/autores/cube29
Icono realizado por Cube29 de www.flaticon.com

https://www.flaticon.es/autores/freepik
Icono realizado por Freepik de www.flaticon.com

Datos nutricionales proporcionados por el USDA, FoodData Central.
                """.trimIndent(),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
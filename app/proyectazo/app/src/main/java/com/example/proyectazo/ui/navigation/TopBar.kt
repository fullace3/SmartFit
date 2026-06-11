package com.example.proyectazo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Reusable top app bar used across all secondary screens in SmartFit.
 * Displays a centered title and a back button on the left.
 * A divider is added below to visually separate the bar from the screen content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartFitTopBar(
    titulo: String,
    onBack: () -> Unit
) {
    Column {
        CenterAlignedTopAppBar(
            title = {
                // Uses the typography token instead of a hardcoded font size
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    // AutoMirrored flips the arrow automatically in right-to-left languages
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface // No hardcoded colors
            )
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}
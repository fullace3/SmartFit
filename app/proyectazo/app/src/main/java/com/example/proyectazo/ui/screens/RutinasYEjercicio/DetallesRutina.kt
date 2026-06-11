package com.example.proyectazo.ui.screens.RutinasYEjercicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.viewmodel.RutinaYEjercicio.RutinaConEjercicios

/**
 * Shows the exercises in a routine before the user starts training.
 * Two actions are pinned to the bottom bar: edit the routine or start the workout.
 */
@Composable
fun DetallesRutinaScreen(
    rutinaConEjercicios: RutinaConEjercicios,  // Passed in-memory from NavGraph — not a route argument
    onBack: () -> Unit,
    onEditar: () -> Unit,
    onEmpezar: () -> Unit
) {
    Scaffold(
        topBar = { SmartFitTopBar(titulo = "Detalles rutina", onBack = onBack) },
        bottomBar = {
            // Both actions share equal space via weight(1f)
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditar,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Editar entrenamiento", style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = onEmpezar,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Empezar a entrenar", style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = rutinaConEjercicios.rutina.nombre,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold, fontSize = 22.sp
                    )
                )
                Spacer(Modifier.height(8.dp))
            }

            // itemsIndexed used instead of items to have the index available if needed later
            itemsIndexed(
                items = rutinaConEjercicios.ejercicios,
                key = { _, ej -> ej.id_ejercicio }  // Stable key prevents unnecessary recompositions
            ) { _, ejercicio ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AsyncImage(
                        model = ejercicio.imagen,
                        contentDescription = ejercicio.nombre,
                        contentScale = ContentScale.Crop,  // Fills the thumbnail without distortion
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ejercicio.nombre,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                        // TODO: Replace hardcoded values with actual series/reps from RutinaEjercicio
                        Text(
                            text = "3 series x 10 repeticiones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
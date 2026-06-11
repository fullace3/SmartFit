package com.example.proyectazo.ui.screens.DietasYComidas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key.Companion.Three
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.viewmodel.DietaYComida.DietaListItem

/**
 * Shows all diets created by the user.
 * Can be reached from DietaScreen when the user wants to switch diets,
 * or directly when no active diet exists yet.
 * @param mostrarBack Controls whether a back button and TopBar are shown — hidden when there is no active diet to return to.
 */
@Composable
fun TodasLasDietasScreen(
    dietas: List<DietaListItem> = emptyList(),
    isLoading: Boolean = false,
    onCrearDieta: () -> Unit,
    onSeleccionarDieta: (Int) -> Unit,
    onBorrarDieta: (Int) -> Unit = {},
    mostrarBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            // TopBar only shown when navigating from an active diet — not on first access
            if (mostrarBack) {
                SmartFitTopBar(titulo = "Todas las dietas", onBack = onBack)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrearDieta,
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Crear nueva dieta")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Title rendered as a list item so it scrolls with the content
            if (!mostrarBack) {
                item {
                    Text(
                        "Todas las dietas",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (isLoading) {
                // Empty state — guides the user to create their first diet
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            } else if (dietas.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No hay dietas creadas",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Crea una nueva para empezar",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(dietas) { dieta ->
                    ItemDietaCard(
                        dieta = dieta,
                        onSelect = { onSeleccionarDieta(dieta.id) },
                        onBorrar = { onBorrarDieta(dieta.id) }
                    )
                }
            }
            // Extra padding so the FAB does not overlap the last card
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/**
 * Card representing a single diet in the list.
 * Shows an "ACTIVA" badge if this is the currently active diet.
 * Inactive diets show a "Seleccionar" button — active ones do not,
 * since selecting an already active diet would have no effect.
 */
@Composable
private fun ItemDietaCard(
    dieta: DietaListItem,
    onSelect: () -> Unit,
    onBorrar: () -> Unit
) {
    // Controls the visibility of the three-dot dropdown menu
    var menuExpandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dieta.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (dieta.activo) {
                        // Small badge to visually identify the active diet at a glance
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "ACTIVA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Menú desplegable en lugar del botón simple
                // delete is inside a dropdown to prevent accidental taps
                Box {
                    IconButton(
                        onClick = { menuExpandido = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "Opciones",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpandido,
                        onDismissRequest = { menuExpandido = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Borrar", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpandido = false
                                onBorrar()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error // Red signals a destructive action
                                )
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            // Macro summary: proteins | carbs | fats in a single compact row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Calorías",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${dieta.calorias} kcal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    "${dieta.proteinas.toInt()}g | ${dieta.carbohidratos.toInt()}g | ${dieta.grasas.toInt()}g",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // "Seleccionar" button only shown for inactive diets
            if (!dieta.activo) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Seleccionar", fontSize = 14.sp) }
            }
        }
    }
}
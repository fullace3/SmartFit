package com.example.proyectazo.ui.screens.DietasYComidas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.ui.viewmodel.DietaYComida.CrearDietaViewModel
import com.example.proyectazo.ui.viewmodel.DietaYComida.AlimentoItem
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

@Composable
fun CrearDietaScreen(
    onBack: () -> Unit,
    onGuardadoExitoso: () -> Unit,
    onAñadirAlimento: (String, String) -> Unit = { _, _ -> },
    comidaAñadida: Triple<Int, String, Int>? = null, // Food item returned from DetalleComidaScreen
    comidaAñadidaMacros: Triple<Int, Int, Int>? = null, // Macros (protein, carbs, fat) of the added food
    comidaTipo: String? = null, // Meal type selected before navigating to food picker
    comidaDia: String? = null, // Day selected before navigating to food picker
    onComidaConsumida: () -> Unit = {}, // Clears the pending food after it has been processed
    dietaId: Int? = null // Null when creating a new diet, non-null when editing an existing one
) {
    val context = LocalContext.current
    val viewModel: CrearDietaViewModel = viewModel(
        factory = CrearDietaViewModel.Factory(context, dietaId)
    )
    val uiState by viewModel.uiState.collectAsState()
    var diaSeleccionado by remember { mutableStateOf(comidaDia ?: "Lun") }
    // rememberSaveable not needed here — editandoNombre resets intentionally on recomposition
    var editandoNombre by remember { mutableStateOf(false) }

    // Triggered when the user returns from DetalleComidaScreen with a selected food item.
    // LaunchedEffect re-runs only when comidaAñadida changes, avoiding duplicate additions.
    LaunchedEffect(comidaAñadida) {
        if (comidaAñadida != null && comidaAñadidaMacros != null) {
            val (id, nombre, calorias) = comidaAñadida
            val (proteinas, carbos, grasas) = comidaAñadidaMacros
            viewModel.agregarAlimento(
                AlimentoItem(
                    id = id,
                    nombre = nombre,
                    calorias = calorias,
                    proteinas = proteinas,
                    carbohidratos = carbos,
                    grasas = grasas,
                    dia = comidaDia ?: diaSeleccionado,
                    tipo = comidaTipo ?: "Desayuno"
                )
            )
            onComidaConsumida() // Reset so the same food is not added again on recomposition
        }
    }

    val opcionesObjetivo = listOf(
        "Volumen limpio", "Definición", "Pérdida de peso",
        "Mantenimiento", "Fuerza", "Resistencia"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header X cerrar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Card principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Nombre centrado + editar
                if (editandoNombre) {
                    OutlinedTextField(
                        value = uiState.nombreDieta,
                        onValueChange = { viewModel.onNombreDietaChange(it) },
                        placeholder = { Text("Nombre de la dieta") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            uiState.nombreDieta.ifEmpty { "Nueva dieta" },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { editandoNombre = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Editar nombre",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // ── Dropdown objetivo
                Text(
                    "Editar objetivos",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                DropdownCampo(
                    valor = uiState.objetivo,
                    placeholder = "Seleccionar Objetivo",
                    opciones = opcionesObjetivo,
                    onSeleccion = viewModel::onObjetivoChange
                )

                Divider(modifier = Modifier.fillMaxWidth())

                // ── Macros
                MacroRow("Proteína", "${uiState.proteinas}g")
                MacroRow("Carbohidratos", "${uiState.carbohidratos}g")
                MacroRow("Grasas", "${uiState.grasas}g")
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Registro semanal
        Text(
            "Registro semanal",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(12.dp))

        // ── Card única: días + Desayuno + Comida + Cena
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ── Días
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val dias = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
                    dias.forEach { dia ->
                        val seleccionado = dia == diaSeleccionado
                        Button(
                            onClick = { diaSeleccionado = dia },
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (seleccionado)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                dia,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (seleccionado)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // ── Desayuno
                SeccionComidaInline(
                    titulo = "Desayuno",
                    alimentos = uiState.alimentos.filter { it.tipo == "Desayuno" && (it.dia == null || it.dia == diaSeleccionado) },
                    inicialmenteExpandido = true,
                    onAgregarAlimento = { onAñadirAlimento("Desayuno", diaSeleccionado) },
                    onEliminarAlimento = { uid -> viewModel.eliminarAlimento(uid) }
                )

                // ── Comida
                SeccionComidaInline(
                    titulo = "Comida",
                    alimentos = uiState.alimentos.filter { it.tipo == "Comida" && (it.dia == null || it.dia == diaSeleccionado) },
                    inicialmenteExpandido = false,
                    onAgregarAlimento = { onAñadirAlimento("Comida", diaSeleccionado) },
                    onEliminarAlimento = { uid -> viewModel.eliminarAlimento(uid) }
                )

                // ── Cena
                SeccionComidaInline(
                    titulo = "Cena",
                    alimentos = uiState.alimentos.filter { it.tipo == "Cena" && (it.dia == null || it.dia == diaSeleccionado) },
                    inicialmenteExpandido = false,
                    onAgregarAlimento = { onAñadirAlimento("Cena", diaSeleccionado) },
                    onEliminarAlimento = { uid -> viewModel.eliminarAlimento(uid) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Botón guardar
        Button(
            onClick = { viewModel.guardar(onGuardadoExitoso) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Guardar dieta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Sección inline dentro de la card
@Composable
private fun SeccionComidaInline(
    titulo: String,
    alimentos: List<AlimentoItem>,
    inicialmenteExpandido: Boolean,
    onAgregarAlimento: () -> Unit,
    onEliminarAlimento: (Long) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(inicialmenteExpandido) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Divider(color = MaterialTheme.colorScheme.outlineVariant)
        // Tapping the header toggles the section open or closed
        Surface(
            onClick = { expanded = !expanded },
            color = MaterialTheme.colorScheme.background
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(if (expanded) "▲" else "▼", fontSize = 12.sp)
            }
        }
        // Spring animation gives the expand/collapse a natural bouncy feel
        // DampingRatioMediumBouncy and StiffnessLow produce a smooth, physical response
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )),
            exit = shrinkVertically(animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                alimentos.forEach { alimento ->
                    AlimentoItemRow(alimento, onEliminar = { onEliminarAlimento(alimento.uid) })
                }
                Button(
                    onClick = onAgregarAlimento,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    // Subtle tinted background to distinguish from primary action buttons
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Text("Añadir alimento", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// Displays a single macronutrient label and its value in a horizontal row
@Composable
private fun MacroRow(label: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(valor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// Displays a food item row with its calorie count and a delete button
@Composable
private fun AlimentoItemRow(alimento: AlimentoItem, onEliminar: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            alimento.nombre,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)  // Takes remaining space, pushing calories and button to the right
        )
        Text(
            "${alimento.calorias} Kcal",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(
            onClick = onEliminar,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Eliminar",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error // Red tint signals a destructive action
            )
        }
    }
}

// ── Dropdown genérico
/**
 * Generic reusable dropdown field built with ExposedDropdownMenuBox.
 * readOnly = true prevents the keyboard from opening — selection only via the dropdown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownCampo(
    valor: String,
    placeholder: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,  // Prevents keyboard — user selects from the dropdown only
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(), // Required by M3 to anchor the dropdown to this field
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false // Close the dropdown after selection
                    }
                )
            }
        }
    }
}
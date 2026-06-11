package com.example.proyectazo.ui.screens.DietasYComidas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.proyectazo.ui.viewmodel.DietaYComida.DietaListItem
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

/**
 * Shows the currently active diet with a calorie ring, macro progress bars
 * and a daily meal checklist.
 *
 * FAB expands to reveal two options: create a new diet or switch to another existing one.
 * Meal check states and FAB state survive device rotation via rememberSaveable.
 */
@Composable
fun DietaActivaScreen(
    dieta: DietaListItem,
    onNueva: () -> Unit,
    onCambiar: () -> Unit
) {
    val context = LocalContext.current
    // Uses smartfit_session (not smartfit_config) because this data is session-dependent
    val prefs = remember { context.getSharedPreferences("smartfit_session", android.content.Context.MODE_PRIVATE) }

    // rememberSaveable persists these states across recompositions and device rotation
    var fabExpanded by rememberSaveable { mutableStateOf(false) }
    var desayunoCheck by rememberSaveable { mutableStateOf(false) }
    var comidaCheck by rememberSaveable { mutableStateOf(false) }
    var cenaCheck by rememberSaveable { mutableStateOf(false) }


    // TODO: Load real food data from DIETA_COMIDA endpoint
    // All values are hardcoded to 0 until the endpoint integration is implemented
    val calConsumidas = 0
    val calRestantes = dieta.calorias
    val progreso = 0f
    val protActual = 0
    val carbActual = 0
    val grasActual = 0

    // Write diet data to SharedPreferences so the Home screen can read
    // the progress ring without making an extra API call
    LaunchedEffect(progreso) {
        prefs.edit()
            .putFloat("dieta_progreso", progreso)
            .putString("dieta_nombre", dieta.nombre)
            .putInt("dieta_calorias", dieta.calorias)
            .putInt("dieta_proteinas", dieta.proteinas.toInt())
            .putInt("dieta_carbos", dieta.carbohidratos.toInt())
            .putInt("dieta_grasas", dieta.grasas.toInt())
            .apply()
    }

    Scaffold(
        floatingActionButton = {
            // Secondary FABs slide in when the main FAB is tapped
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(visible = fabExpanded) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = { fabExpanded = false; onNueva() },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                                Text("Nuevo", fontSize = 12.sp)
                            }
                        }
                        SmallFloatingActionButton(
                            onClick = { fabExpanded = false; onCambiar() },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                                Text("Cambiar", fontSize = 12.sp)
                            }
                        }
                    }
                }
                // Main FAB — toggles the expanded menu
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Opciones")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Card principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        dieta.nombre,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(24.dp))
                    // Two arcs: a static track and a filled arc proportional to progress
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh

                        Canvas(modifier = Modifier.size(140.dp)) {
                            // Background track — always full 360°
                            drawArc(
                                color = trackColor, startAngle = -90f, sweepAngle = 360f,
                                useCenter = false, style = Stroke(width = 14f, cap = StrokeCap.Round)
                            )
                            // Progress arc — only drawn if there is actual progress
                            if (progreso > 0f) {
                                drawArc(
                                    color = primaryColor, startAngle = -90f,
                                    sweepAngle = 360f * progreso,
                                    useCenter = false, style = Stroke(width = 14f, cap = StrokeCap.Round)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$calRestantes", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("cal restantes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    MacroBarRow("Proteína", protActual, dieta.proteinas.toInt())
                    Spacer(Modifier.height(8.dp))
                    MacroBarRow("Carbohidratos", carbActual, dieta.carbohidratos.toInt())
                    Spacer(Modifier.height(8.dp))
                    MacroBarRow("Grasas", grasActual, dieta.grasas.toInt())
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Registro Diario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        "Registro Diario",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))

                    // Each meal section has an independent check and expand state
                    SeccionRegistro(
                        titulo = "Desayuno",
                        checked = desayunoCheck,
                        onCheckedChange = { desayunoCheck = it }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    SeccionRegistro(
                        titulo = "Comida",
                        checked = comidaCheck,
                        onCheckedChange = { comidaCheck = it }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    SeccionRegistro(
                        titulo = "Cena",
                        checked = cenaCheck,
                        onCheckedChange = { cenaCheck = it }
                    )
                }
            }
            // Extra bottom padding so the FAB does not overlap the last card
            Spacer(Modifier.height(80.dp))
        }
    }
}

/**
 * Displays a macro label, current vs target values and a linear progress bar.
 * Progress is clamped to [0, 1] to prevent the bar from overflowing.
 */
@Composable
private fun MacroBarRow(label: String, actual: Int, meta: Int) {
    val progreso = if (meta > 0) (actual.toFloat() / meta).coerceIn(0f, 1f) else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("$actual / ${meta}g", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progreso },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Expandable meal section with an independent circular check button.
 * Both expanded and checked states use rememberSaveable to survive rotation.
 * Spring animation gives the expand/collapse a natural bouncy feel.
 */
@Composable
private fun SeccionRegistro(
    titulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Tick circular
        // Circular check button — color changes when the meal is marked as done
        Surface(
            onClick = { onCheckedChange(!checked) },
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = if (checked) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp
        ) {
            if (checked) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Completado",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        // ── Título
        // Tapping the title row toggles the expanded state independently of the check
        Surface(
            onClick = { expanded = !expanded },
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(if (expanded) "▲" else "▼", fontSize = 12.sp)
            }
        }
    }
    // Spring animation — same DampingRatioMediumBouncy used across the app for consistency
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
        Text(
            "Sin alimentos registrados",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 40.dp, bottom = 8.dp)
        )
    }
}
package com.example.proyectazo.ui.screens.DietasYComidas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectazo.ui.viewmodel.DietaYComida.DietaActivaViewModel

@Composable
fun DietaActivaScreen(
    onNueva: () -> Unit,
    onCambiar: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DietaActivaViewModel = viewModel(
        factory = DietaActivaViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    var fabExpanded by rememberSaveable { mutableStateOf(false) }
    var desayunoCheck by rememberSaveable { mutableStateOf(false) }
    var comidaCheck by rememberSaveable { mutableStateOf(false) }
    var cenaCheck by rememberSaveable { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val dieta = uiState.dieta ?: return

    val caloriasObj  = dieta.objetivo_calorico
    val calConsumidas = uiState.caloriasConsumidas
    val calRestantes  = (caloriasObj - calConsumidas).coerceAtLeast(0)
    val progreso      = if (caloriasObj > 0) (calConsumidas.toFloat() / caloriasObj).coerceIn(0f, 1f) else 0f

    Scaffold(
        floatingActionButton = {
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
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val trackColor   = MaterialTheme.colorScheme.surfaceContainerHigh

                        Canvas(modifier = Modifier.size(140.dp)) {
                            drawArc(
                                color = trackColor, startAngle = -90f, sweepAngle = 360f,
                                useCenter = false, style = Stroke(width = 14f, cap = StrokeCap.Round)
                            )
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
                    MacroBarRow("Proteína",      uiState.proteinasConsumidas.toInt(), dieta.proteinas_g.toInt())
                    Spacer(Modifier.height(8.dp))
                    MacroBarRow("Carbohidratos", uiState.carbsConsumidos.toInt(),     dieta.carbohidratos_g.toInt())
                    Spacer(Modifier.height(8.dp))
                    MacroBarRow("Grasas",        uiState.grasasConsumidas.toInt(),    dieta.grasas_g.toInt())
                }
            }

            Spacer(Modifier.height(24.dp))

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
                    SeccionRegistro("Desayuno", desayunoCheck) { desayunoCheck = it }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SeccionRegistro("Comida",   comidaCheck)   { comidaCheck   = it }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SeccionRegistro("Cena",     cenaCheck)     { cenaCheck     = it }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

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

@Composable
private fun SeccionRegistro(
    titulo: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(if (expanded) "▲" else "▼", fontSize = 12.sp)
            }
        }
    }
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

package com.example.proyectazo.ui.screens.RutinasYEjercicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyectazo.data.local.entity.EjercicioEntity
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.util.rememberDrawableId
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.proyectazo.ui.util.rememberDrawableId

@Composable
fun DetalleEjercicioScreen(
    ejercicio: EjercicioEntity,
    onBack: () -> Unit,
    onAnadir: (EjercicioEntity) -> Unit
) {
    Scaffold(
        topBar = { SmartFitTopBar(titulo = ejercicio.nombre, onBack = onBack) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { onAnadir(ejercicio) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Añadir ejercicio", style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val resId = rememberDrawableId(ejercicio.imagen)
                if (resId != null) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = ejercicio.nombre,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = ejercicio.nombre.take(1).uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ejercicio.grupo_muscular?.let {
                    InfoChip(label = "Músculo", valor = it, modifier = Modifier.weight(1f))
                }
                ejercicio.equipamiento?.let {
                    InfoChip(label = "Equipamiento", valor = it, modifier = Modifier.weight(1f))
                }
            }

            ejercicio.descripcion?.let { desc ->
                Spacer(Modifier.height(20.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoChip(label: String, valor: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(text = valor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary)
    }
}

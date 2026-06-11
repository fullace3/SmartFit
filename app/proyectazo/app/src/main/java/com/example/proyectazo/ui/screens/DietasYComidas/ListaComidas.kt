package com.example.proyectazo.ui.screens.DietasYComidas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.proyectazo.ui.components.SmartFitTopBar
import com.example.proyectazo.ui.viewmodel.DietaYComida.ComidaListItem
import com.example.proyectazo.ui.viewmodel.DietaYComida.ListaComidasViewModel

/**
 * Displays a searchable, filterable list of food items.
 * The user arrives here from CrearDietaScreen to pick a food to add to their diet.
 * Selection navigates to DetalleComidaScreen where the food can be confirmed.
 */
@Composable
fun ListaComidasScreen(
    onBack: () -> Unit,
    onComidaSeleccionada: (Int) -> Unit // Passes the selected food ID to the NavGraph
) {
    val context = LocalContext.current
    val viewModel: ListaComidasViewModel = viewModel(
        factory = ListaComidasViewModel.Factory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    val filtros = listOf("Proteinas", "Carbohidratos", "Grasas saludables")

    Scaffold(
        topBar = { SmartFitTopBar(titulo = "Añadir alimento", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Buscador
            // triggers filtering in the ViewModel on every keystroke
            OutlinedTextField(
                value = uiState.busqueda,
                onValueChange = viewModel::onBusquedaChange,
                placeholder = { Text("Buscar alimento") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            // ── Chips de filtro
            // only one can be active at a time, handled by the ViewModel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtros.forEach { filtro ->
                    val seleccionado = uiState.filtroActivo == filtro
                    FilterChip(
                        selected = seleccionado,
                        onClick = { viewModel.onFiltroChange(filtro) },
                        label = { Text(filtro, fontSize = 12.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // ── Lista de comidas
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // LazyColumn only renders visible items — efficient for long food lists
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.comidasFiltradas) { comida ->
                        ComidaRow(
                            comida = comida,
                            onClick = { onComidaSeleccionada(comida.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComidaRow(comida: ComidaListItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Imagen
        // Build the full S3 URL if the stored value is just a filename
        if (!comida.imagen.isNullOrEmpty()) {
            val url = if (comida.imagen.startsWith("http")) comida.imagen
            else "https://smartfit-imagenes-dam.s3.us-east-1.amazonaws.com/${comida.imagen}"

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = comida.nombre,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop // Fills the circle without distorting the image
            )
        } else {
            // Fallback placeholder shown when the food has no image
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🍽️", fontSize = 24.sp)
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // ── Nombre y calorías
        Column {
            Text(
                comida.nombre,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${comida.calorias100g} kcal / 100g",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
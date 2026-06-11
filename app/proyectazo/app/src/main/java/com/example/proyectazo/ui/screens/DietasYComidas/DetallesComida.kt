package com.example.proyectazo.ui.screens.DietasYComidas

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.proyectazo.ui.viewmodel.DietaYComida.DetalleComidaViewModel

/**
 * Shows the nutritional details of a food item and allows the user to add it to their diet.
 * Navigates back to CrearDietaScreen passing the food data via savedStateHandle.
 */
@Composable
fun DetalleComidaScreen(
    comidaId: Int,
    onBack: () -> Unit,
    // All nutritional values passed back at once to avoid multiple savedStateHandle writes
    onAñadir: (id: Int, nombre: String, calorias: Int, proteinas: Int, carbos: Int, grasas: Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel: DetalleComidaViewModel = viewModel(
        factory = DetalleComidaViewModel.Factory(context, comidaId)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { SmartFitTopBar(titulo = "Añadir alimento", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Prevents content from rendering behind the TopBar
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                Spacer(Modifier.height(16.dp))

                // ── Nombre
                Text(
                    uiState.nombre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(16.dp))

                // ── Imagen
                val imagenUrl = uiState.imagen
                if (!imagenUrl.isNullOrEmpty()) {
                    // Build the full S3 URL if the stored value is just a filename
                    val url = if (imagenUrl.startsWith("http")) imagenUrl
                    else "https://smartfit-imagenes-dam.s3.us-east-1.amazonaws.com/$imagenUrl"

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(url)
                            .crossfade(true) // Smooth fade-in while the image loads from the network
                            .build(),
                        contentDescription = uiState.nombre,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop // Fills the bounds without distorting the image
                    )

                    Spacer(Modifier.height(24.dp))
                }

                // ── Card valor nutricional
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
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Valor nutricional por cada 100 g",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        // Each nutrient row is separated by a divider for readability
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        NutrienteRow("Kcal", "${uiState.calorias100g} Kcal")

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        NutrienteRow("Proteínas", "${uiState.proteinas100g} g")

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        NutrienteRow("Grasas totales", "${uiState.grasas100g} g")

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        NutrienteRow("Hidratos de carbono", "${uiState.carbohidratos100g} g")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Botón añadir 
                Button(
                    onClick = {
                        onAñadir(
                            comidaId,
                            uiState.nombre,
                            uiState.calorias100g,
                            uiState.proteinas100g,
                            uiState.carbohidratos100g,
                            uiState.grasas100g
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Añadir alimento", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// Displays a single nutrient label and its value in a horizontal row
@Composable
private fun NutrienteRow(label: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(valor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
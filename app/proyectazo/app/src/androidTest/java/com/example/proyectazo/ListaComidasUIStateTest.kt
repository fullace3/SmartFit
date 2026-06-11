package com.example.proyectazo

import com.example.proyectazo.ui.viewmodel.DietaYComida.ComidaListItem
import com.example.proyectazo.ui.viewmodel.DietaYComida.ListaComidasUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [ListaComidasUiState.comidasFiltradas].
 * These tests run on the JVM without a device or emulator.
 * They validate the filtering and sorting business logic.
 */
class ListaComidasUiStateTest {

    // ── Test data ──────────────────────────────────────────────────────────
    private val pollo = ComidaListItem(
        id = 1, nombre = "Pollo a la plancha",
        calorias100g = 165, proteinas100g = 31,
        carbohidratos100g = 0, grasas100g = 4
    )
    private val arroz = ComidaListItem(
        id = 2, nombre = "Arroz blanco",
        calorias100g = 130, proteinas100g = 3,
        carbohidratos100g = 28, grasas100g = 1
    )
    private val salmon = ComidaListItem(
        id = 3, nombre = "Salmón al horno",
        calorias100g = 208, proteinas100g = 20,
        carbohidratos100g = 0, grasas100g = 13
    )
    private val alimentos = listOf(pollo, arroz, salmon)


    @Test
    fun busquedaPorNombreFiltraCorrectamente() {
        val state = ListaComidasUiState(comidas = alimentos, busqueda = "pollo")
        assertEquals(1, state.comidasFiltradas.size)
        assertEquals("Pollo a la plancha", state.comidasFiltradas[0].nombre)
    }

    @Test
    fun busquedaInsensibleAMayusculas() {
        val state = ListaComidasUiState(comidas = alimentos, busqueda = "ARROZ")
        assertEquals(1, state.comidasFiltradas.size)
        assertEquals("Arroz blanco", state.comidasFiltradas[0].nombre)
    }

    @Test
    fun filtroProteinasOrdenaDeMayorAMenor() {
        val state = ListaComidasUiState(comidas = alimentos, filtroActivo = "Proteinas")
        val resultado = state.comidasFiltradas
        assertEquals("Pollo a la plancha", resultado[0].nombre)
        assertEquals("Salmón al horno", resultado[1].nombre)
        assertEquals("Arroz blanco", resultado[2].nombre)
    }

    @Test
    fun sinFiltroActivoNoOrdena() {
        val state = ListaComidasUiState(comidas = alimentos, filtroActivo = null)
        assertEquals(alimentos, state.comidasFiltradas)
    }
}
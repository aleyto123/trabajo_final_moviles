package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import com.tecsup.agendacitasdeportivas.ui.state.ReservationUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(navController: NavController, viewModel: ReservationViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas de Reservas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is ReservationUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is ReservationUiState.Error -> Text("Error: ${state.message}")
                is ReservationUiState.Success -> {
                    val reservations = state.reservations
                    val totalReservations = reservations.size
                    val totalIncome = reservations.sumOf { it.hourlyPrice }
                    val mostReservedCancha = reservations.groupBy { it.canchaType }
                        .maxByOrNull { it.value.size }?.key ?: "Ninguna"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resumen General", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Total de Reservas: $totalReservations")
                            Text("Ingresos Totales: S/. $totalIncome")
                            Text("Cancha más popular: $mostReservedCancha")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Detalle por Deporte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn {
                        val grouped = reservations.groupBy { it.canchaType }
                        val keysList = grouped.keys.toList()
                        items(keysList) { type ->
                            val count = grouped[type]?.size ?: 0
                            val income = grouped[type]?.sumOf { it.hourlyPrice } ?: 0.0
                            ListItem(
                                headlineContent = { Text(type) },
                                supportingContent = { Text("$count reservas") },
                                trailingContent = { Text("S/. $income") }
                            )
                        }
                    }
                }
            }
        }
    }
}

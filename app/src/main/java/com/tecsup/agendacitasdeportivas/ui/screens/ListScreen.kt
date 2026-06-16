package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import com.tecsup.agendacitasdeportivas.ui.state.ReservationUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(navController: NavController, viewModel: ReservationViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Reservas") },
                actions = {
                    IconButton(onClick = { navController.navigate("api_screen") }) {
                        Icon(Icons.Default.Info, contentDescription = "Clima e IA")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("form_screen") }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Reserva")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is ReservationUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Text("Cargando reservas...")
                        }
                    }
                }
                is ReservationUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is ReservationUiState.Success -> {
                    if (state.reservations.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay reservas. Toca + para añadir.")
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.reservations) { reserva ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable { navController.navigate("detail_screen/${reserva.id}") }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "Cancha: ${reserva.canchaType}", style = MaterialTheme.typography.titleMedium)
                                        Text(text = "Cliente: ${reserva.customerName}")
                                        Text(text = "Fecha: ${reserva.reservationDate}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
fun ListScreen(navController: NavController, viewModel: ReservationViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Reservas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("statistics_screen") }) {
                        Icon(Icons.Default.Info, contentDescription = "Estadísticas")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ReservationUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is ReservationUiState.Error -> Text("Error: ${state.message}", Modifier.align(Alignment.Center))
                is ReservationUiState.Success -> {
                    if (state.reservations.isEmpty()) {
                        Text("No hay reservas registradas.", Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.reservations) { reservation ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable { navController.navigate("detail_screen/${reservation.id}") }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = reservation.canchaType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(text = "Cliente: ${reservation.customerName}")
                                        Text(text = "Fecha: ${reservation.reservationDate}")
                                        Text(text = "Horas: ${reservation.reservationTime}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ChatBotBubble(navController)
        }
    }
}

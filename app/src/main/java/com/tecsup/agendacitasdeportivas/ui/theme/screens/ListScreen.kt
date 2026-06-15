package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel

@Composable
fun ListScreen(navController: NavController, viewModel: ReservationViewModel) {
    val reservations by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { ExperimentalMaterial3Api::class; TopAppBar(title = { Text("Mis Reservas") }) }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            items(reservations) { reserva ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { navController.navigate("detail_screen/${reserva.id}") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Cancha: ${reserva.nombreCancha}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Fecha: ${reserva.fecha} - Hora: ${reserva.hora}")
                    }
                }
            }
        }
    }
}
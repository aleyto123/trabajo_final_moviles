package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel

@Composable
fun DetailScreen(navController: NavController, viewModel: ReservationViewModel, reservaId: Int) {
    val reservations by viewModel.uiState.collectAsState()
    val reserva = reservations.find { it.id == reservaId }

    Scaffold(
        topBar = { ExperimentalMaterial3Api::class; TopAppBar(title = { Text("Detalle de Reserva") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (reserva != null) {
                Text("ID Reserva: ${reserva.id}", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cancha: ${reserva.nombreCancha}")
                Text("Fecha programada: ${reserva.fecha}")
                Text("Hora de inicio: ${reserva.hora}")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Volver")
                }
            } else {
                Text("Reserva no encontrada")
            }
        }
    }
}
package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, viewModel: ReservationViewModel, reservaId: String) {
    var reserva by remember { mutableStateOf<CanchaReservationEntity?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reservaId) {
        scope.launch {
            reserva = viewModel.getReservationById(reservaId)
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Detalle de Reserva") })
        }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        } else {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                reserva?.let { r ->
                    Text("ID Reserva: ${r.id}", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cancha: ${r.canchaType}", style = MaterialTheme.typography.headlineMedium)
                    Text("Cliente: ${r.customerName}")
                    Text("Fecha: ${r.reservationDate}")
                    Text("Hora: ${r.reservationTime}")
                    Text("Precio: $${r.hourlyPrice}")
                    Text("Estado de Pago: ${r.paymentStatus}")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Normativas:", style = MaterialTheme.typography.titleMedium)
                    Text("- No se permite calzado inadecuado.")
                    Text("- Cancelaciones con 24h de anticipación.")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Volver")
                    }
                } ?: Text("Reserva no encontrada")
            }
        }
    }
}
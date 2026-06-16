package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavController, viewModel: ReservationViewModel) {
    var customerName by remember { mutableStateOf("") }
    var canchaType by remember { mutableStateOf("") }
    var reservationDate by remember { mutableStateOf("") }
    var reservationTime by remember { mutableStateOf("") }
    var hourlyPrice by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nueva Reserva") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Nombre del Cliente") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = canchaType,
                onValueChange = { canchaType = it },
                label = { Text("Tipo de Cancha") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = reservationDate,
                onValueChange = { reservationDate = it },
                label = { Text("Fecha (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = reservationTime,
                onValueChange = { reservationTime = it },
                label = { Text("Hora (HH:mm)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = hourlyPrice,
                onValueChange = { hourlyPrice = it },
                label = { Text("Precio por Hora") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (customerName.isBlank() || canchaType.isBlank() || 
                        reservationDate.isBlank() || reservationTime.isBlank() || hourlyPrice.isBlank()) {
                        errorMessage = "Todos los campos son obligatorios."
                    } else {
                        // Validación básica de fecha pasada (ejemplo simplificado)
                        try {
                            val price = hourlyPrice.toDouble()
                            viewModel.insert(
                                CanchaReservationEntity(
                                    userId = "USER_DEFAULT",
                                    canchaType = canchaType,
                                    customerName = customerName,
                                    reservationDate = reservationDate,
                                    reservationTime = reservationTime,
                                    hourlyPrice = price,
                                    paymentStatus = "Pendiente"
                                )
                            )
                            navController.popBackStack()
                        } catch (e: Exception) {
                            errorMessage = "Precio inválido o error en datos."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Reserva")
            }
        }
    }
}
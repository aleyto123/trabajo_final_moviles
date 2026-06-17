package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.data.model.CanchaProvider
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import com.tecsup.agendacitasdeportivas.ui.viewmodel.DetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController, 
    viewModel: ReservationViewModel, 
    id: String, 
    isCancha: Boolean
) {
    if (isCancha) {
        CanchaDetailContent(navController, id)
    } else {
        ReservationDetailContent(navController, viewModel, id)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanchaDetailContent(navController: NavController, canchaId: String) {
    val cancha = CanchaProvider.allCanchas.find { it.id == canchaId } ?: return
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de la Cancha") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero section (Simulated image)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp))
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(cancha.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(cancha.type, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(cancha.address, style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Descripción", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(cancha.description, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Precio por hora", style = MaterialTheme.typography.labelLarge)
                            Text("S/. ${cancha.pricePerHour}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = { navController.navigate("form_screen/${cancha.id}") }) {
                            Text("Hacer Reserva")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("Normativas:", style = MaterialTheme.typography.titleMedium)
                Text("• No se permite el ingreso con calzado de calle.")
                Text("• El tiempo de tolerancia es de 10 minutos.")
                Text("• Cancelaciones con 24 horas de anticipación.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailContent(navController: NavController, viewModel: ReservationViewModel, id: String) {
    val detailState by viewModel.detailState.collectAsState()

    LaunchedEffect(id) {
        viewModel.loadReservation(id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Reserva") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = detailState) {
                is DetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is DetailUiState.Error -> Text("Error: ${state.message}", Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                is DetailUiState.Success -> {
                    val r = state.reservation
                    Column(modifier = Modifier.padding(16.dp)) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Reserva Confirmada", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Cancha: ${r.canchaType}")
                                Text("Cliente: ${r.customerName}")
                                Text("Fecha: ${r.reservationDate}")
                                Text("Hora: ${r.reservationTime}")
                                Text("Total Pagado: S/. ${r.hourlyPrice}")
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = { /* Lógica para cancelar si se desea */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancelar Reserva")
                        }
                    }
                }
            }
        }
    }
}
package com.tecsup.agendacitasdeportivas.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.data.model.CanchaProvider
import com.tecsup.agendacitasdeportivas.ui.components.MercadoPagoPayButton
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import com.tecsup.agendacitasdeportivas.ui.state.DetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailContent(navController: NavController, viewModel: ReservationViewModel, id: String) {
    val detailState by viewModel.detailState.collectAsState()
    val context = LocalContext.current
    val paymentUrl by viewModel.paymentUrl.collectAsState()
    val isProcessingPayment by viewModel.isProcessingPayment.collectAsState()

    // Mostrar Toast si hay error de pago
    LaunchedEffect(viewModel.paymentErrorMessage) {
        viewModel.paymentErrorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(id) {
        viewModel.loadReservation(id)
    }

    // Abrir el link de pago si se genera desde esta pantalla
    LaunchedEffect(paymentUrl) {
        paymentUrl?.let { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            
            // Simulamos éxito para el historial
            (detailState as? DetailUiState.Success)?.reservation?.let {
                viewModel.onPaymentSuccess(it)
            }
            viewModel.clearPaymentLink()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Reserva", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = detailState) {
            is DetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is DetailUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message) }
            is DetailUiState.Success -> {
                val res = state.reservation
                val cancha = CanchaProvider.allCanchas.find { it.name == res.canchaType }

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (cancha != null) {
                        Image(
                            painter = painterResource(id = cancha.imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = res.canchaType,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "A nombre de: ${res.customerName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Surface(
                                color = if (res.paymentStatus == "Pagado") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (res.paymentStatus == "Pagado") Color(0xFF4CAF50) else Color(0xFFFF9800))
                            ) {
                                Text(
                                    text = res.paymentStatus.uppercase(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.paymentStatus == "Pagado") Color(0xFF2E7D32) else Color(0xFFEF6C00)
                                )
                            }
                        }

                        HorizontalDivider(Modifier.padding(vertical = 24.dp))

                        DetailRow(icon = Icons.Rounded.Event, label = "Fecha", value = res.reservationDate)
                        DetailRow(icon = Icons.Rounded.Schedule, label = "Horario", value = res.reservationTime)
                        DetailRow(icon = Icons.Rounded.Payments, label = "Monto Total", value = "S/. ${String.format("%.2f", res.hourlyPrice)}")

                        if (res.paymentStatus == "Pendiente") {
                            Spacer(Modifier.height(32.dp))
                            
                            // AVISO DE CANCELACIÓN (REQUERIDO)
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "AVISO: Si no realizas el pago en las próximas 2 horas, tu reserva será cancelada automáticamente.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            if (viewModel.paymentErrorMessage != null) {
                                Text(
                                    text = viewModel.paymentErrorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            MercadoPagoPayButton(
                                isLoading = isProcessingPayment,
                                amount = res.hourlyPrice,
                                onClick = { 
                                    viewModel.startPaymentNotificationListener(res.id)
                                    viewModel.generatePaymentLink(res) 
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = { viewModel.delete(res); navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Delete, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CANCELAR RESERVA")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanchaDetailContent(navController: NavController, id: String) {
    val cancha = CanchaProvider.allCanchas.find { it.id == id }
    
    if (cancha == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Cancha no encontrada") }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Cancha", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 16.dp) {
                Button(
                    onClick = { navController.navigate("form_screen/${cancha.id}") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("RESERVAR AHORA", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            Image(
                painter = painterResource(id = cancha.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = cancha.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                Text(text = cancha.type, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DetailRow(icon = Icons.Rounded.LocationOn, label = "Dirección", value = cancha.address)
                    }
                    IconButton(
                        onClick = { navController.navigate("map_screen/${cancha.id}") },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Rounded.Map,
                            contentDescription = "Ver en mapa",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                DetailRow(icon = Icons.Rounded.Payments, label = "Precio por hora", value = "S/. ${cancha.pricePerHour}")
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(text = "Sobre esta cancha", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Cancha profesional con iluminación LED, vestuarios y estacionamiento gratuito. Ideal para torneos y pichangas con amigos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

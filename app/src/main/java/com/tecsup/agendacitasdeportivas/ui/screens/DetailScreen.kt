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

    LaunchedEffect(id) { viewModel.loadReservation(id) }

    LaunchedEffect(paymentUrl) {
        paymentUrl?.let { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            viewModel.clearPaymentLink()
        }
    }

    Scaffold(
        containerColor = Color(0xFF0D0B1A),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle de Reserva", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        when (val state = detailState) {
            is DetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            is DetailUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message, color = Color.White) }
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
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(16.dp),
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Image(
                                painter = painterResource(id = cancha.imageRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = res.canchaType, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text(text = "Reserva de ${res.customerName}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            
                            Surface(
                                color = if (res.estado.lowercase() == "pagado") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color(0xFFFFB74D).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (res.estado.lowercase() == "pagado") MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color(0xFFFFB74D).copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = res.estado.uppercase(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (res.estado.lowercase() == "pagado") MaterialTheme.colorScheme.primary else Color(0xFFFFB74D)
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // Tarjeta de Información Glassmorphism
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White.copy(alpha = 0.03f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                DetailRowElite(icon = Icons.Rounded.Event, label = "FECHA DEL ENCUENTRO", value = res.reservationDate)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
                                DetailRowElite(icon = Icons.Rounded.Schedule, label = "HORARIO RESERVADO", value = res.reservationTime)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
                                DetailRowElite(icon = Icons.Rounded.Payments, label = "INVERSIÓN TOTAL", value = "S/. ${String.format("%.2f", res.hourlyPrice)}")
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        if (res.estado.lowercase() != "pagado") {
                            MercadoPagoPayButton(
                                isLoading = isProcessingPayment,
                                amount = res.hourlyPrice,
                                onClick = { viewModel.generatePaymentLink(res) }
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        Button(
                            onClick = { navController.navigate("edit_screen/${res.id}") },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EDITAR RESERVA", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = { viewModel.delete(res); navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350))
                        ) {
                            Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ELIMINAR MI RESERVA", fontWeight = FontWeight.SemiBold)
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
    val cancha = CanchaProvider.allCanchas.find { it.id == id } ?: return

    Scaffold(
        containerColor = Color(0xFF0D0B1A),
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Cancha", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White.copy(alpha = 0.03f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Button(
                    onClick = { navController.navigate("form_screen/${cancha.id}") },
                    modifier = Modifier.fillMaxWidth().padding(24.dp).height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                ) {
                    Text("RESERVAR AHORA", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(300.dp).padding(16.dp),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Image(
                    painter = painterResource(id = cancha.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = cancha.name, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Text(text = cancha.type.uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                DetailRowElite(icon = Icons.Rounded.LocationOn, label = "UBICACIÓN", value = cancha.address)
                            }
                            IconButton(
                                onClick = { navController.navigate("map_screen/${cancha.id}") },
                                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(44.dp)
                            ) {
                                Icon(Icons.Rounded.Map, "Mapa", tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
                        DetailRowElite(icon = Icons.Rounded.Payments, label = "PRECIO POR HORA", value = "S/. ${String.format("%.2f", cancha.pricePerHour)}")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Text(text = "Sobre esta sede", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Disfruta de una experiencia deportiva de primer nivel. Nuestras canchas cuentan con gras sintético certificado, iluminación LED de alta potencia para tus partidos nocturnos, vestuarios modernos y seguridad permanente.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DetailRowElite(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp), color = Color.White.copy(alpha = 0.4f))
            Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
    }
}

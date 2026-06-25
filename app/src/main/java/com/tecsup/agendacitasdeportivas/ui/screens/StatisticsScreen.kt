package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.data.model.CanchaProvider
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import com.tecsup.agendacitasdeportivas.ui.state.ReservationUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(navController: NavController, viewModel: ReservationViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Análisis de Reservas", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    )
                )
        ) {
            when (val state = uiState) {
                is ReservationUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ReservationUiState.Error -> Text("Error: ${state.message}", Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                is ReservationUiState.Success -> {
                    val reservations = state.reservations
                    val totalReservations = reservations.size
                    val totalIncome = reservations.sumOf { it.hourlyPrice }
                    val mostReservedCancha = reservations.groupBy { it.canchaType }
                        .maxByOrNull { it.value.size }?.key ?: "Ninguna"

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            StatSummaryCard(
                                totalReservations = totalReservations,
                                totalIncome = totalIncome,
                                popular = mostReservedCancha
                            )
                        }

                        item {
                            Text(
                                "Distribución por Deporte", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        val grouped = reservations.groupBy { it.canchaType }
                        val keysList = grouped.keys.toList()
                        items(keysList) { type ->
                            val count = grouped[type]?.size ?: 0
                            val income = grouped[type]?.sumOf { it.hourlyPrice } ?: 0.0
                            val cancha = CanchaProvider.allCanchas.find { it.name == type }
                            
                            StatListItem(
                                title = type,
                                subtitle = "$count reservas realizadas",
                                value = "S/. $income",
                                iconRes = cancha?.imageRes
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatSummaryCard(totalReservations: Int, totalIncome: Double, popular: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Resumen de Rendimiento", 
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                StatMiniBlock(
                    modifier = Modifier.weight(1f),
                    label = "Reservas",
                    value = totalReservations.toString(),
                    icon = Icons.Rounded.Star
                )
                StatMiniBlock(
                    modifier = Modifier.weight(1f),
                    label = "Ingresos",
                    value = "S/. ${totalIncome.toInt()}",
                    icon = Icons.Rounded.AccountBalanceWallet
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Cancha más popular", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(popular, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
fun StatMiniBlock(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
        }
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun StatListItem(title: String, subtitle: String, value: String, iconRes: Int? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (iconRes != null) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Rounded.Analytics, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Text(
                value, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

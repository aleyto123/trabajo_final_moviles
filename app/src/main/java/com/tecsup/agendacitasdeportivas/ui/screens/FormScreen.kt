package com.tecsup.agendacitasdeportivas.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.data.model.CanchaProvider
import com.tecsup.agendacitasdeportivas.ui.state.ReservationUiState
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavController, viewModel: ReservationViewModel, canchaId: String) {
    val cancha = CanchaProvider.allCanchas.find { it.id == canchaId } ?: return
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.canchaType = cancha.name
        viewModel.hourlyPrice = cancha.pricePerHour
    }

    val reservationsState by viewModel.uiState.collectAsState()
    val existingReservations = (reservationsState as? ReservationUiState.Success)?.reservations ?: emptyList()

    // DatePickerDialog Logic
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            viewModel.reservationDate = sdf.format(selectedDate.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1000
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reserva: ${cancha.name}") },
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
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Complete los datos para su cita", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.customerName,
                onValueChange = { viewModel.customerName = it },
                label = { Text("Nombre del Cliente") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Campo de Fecha con InteractionSource para abrir Picker al tocar
            val dateInteractionSource = remember { MutableInteractionSource() }
            LaunchedEffect(dateInteractionSource) {
                dateInteractionSource.interactions.collect {
                    if (it is PressInteraction.Release) {
                        datePickerDialog.show()
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.reservationDate,
                onValueChange = { },
                readOnly = true,
                label = { Text("Fecha de Reserva") },
                placeholder = { Text("Toque para elegir fecha") },
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                interactionSource = dateInteractionSource,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Seleccione las Horas (Puede marcar varias)", fontWeight = FontWeight.Bold)
            Text("Total horas: ${viewModel.selectedTimes.size}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            val timeSlots = listOf("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00")
            
            // Usamos un Box con altura fija para evitar conflictos de scroll
            Box(modifier = Modifier.height(250.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(timeSlots, key = { it }) { time ->
                        val isOccupied = existingReservations.any { 
                            it.reservationDate == viewModel.reservationDate && 
                            it.reservationTime.contains(time) && 
                            it.canchaType == viewModel.canchaType
                        }
                        val isSelected = viewModel.selectedTimes.contains(time)
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { if (!isOccupied) viewModel.toggleTimeSlot(time) },
                            label = { Text(time) },
                            enabled = !isOccupied,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                disabledContainerColor = Color.Red.copy(alpha = 0.1f),
                                disabledLabelColor = Color.Red
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            val totalPrice = cancha.pricePerHour * viewModel.selectedTimes.size
            
            Text("Resumen de Pago", fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total (${viewModel.selectedTimes.size} h):")
                    Text("S/. $totalPrice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (viewModel.isFormValid) {
                        viewModel.insert()
                        navController.navigate("list_screen") {
                            popUpTo("list_screen") { inclusive = true }
                        }
                    } else {
                        errorMessage = "Complete nombre, fecha y al menos una hora."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar Reserva")
            }
        }
    }
}
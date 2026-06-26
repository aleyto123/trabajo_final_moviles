package com.tecsup.agendacitasdeportivas.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.data.model.CanchaProvider
import com.tecsup.agendacitasdeportivas.ui.state.ReservationUiState
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    navController: NavController, 
    viewModel: ReservationViewModel, 
    canchaId: String,
    editId: String? = null
) {
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val reservationsState by viewModel.uiState.collectAsState()
    val existingReservations = (reservationsState as? ReservationUiState.Success)?.reservations ?: emptyList()

    LaunchedEffect(viewModel.lastSavedId) {
        viewModel.lastSavedId?.let { id ->
            navController.navigate("detail_screen/$id") {
                popUpTo("cancha_list") { inclusive = false }
            }
            viewModel.lastSavedId = null
        }
    }

    LaunchedEffect(editId, canchaId) {
        if (editId != null) {
            val reservation = existingReservations.find { it.id == editId }
            if (reservation != null) {
                val cancha = CanchaProvider.allCanchas.find { it.name == reservation.canchaType }
                viewModel.prepareFormForEdit(reservation, cancha?.pricePerHour ?: 0.0)
            }
        } else {
            val cancha = CanchaProvider.allCanchas.find { it.id == canchaId }
            if (cancha != null) {
                viewModel.clearForm()
                viewModel.canchaType = cancha.name
                viewModel.hourlyPrice = cancha.pricePerHour
            }
        }
    }

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
        containerColor = Color(0xFF0D0B1A),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (editId == null) "Nueva Reserva" else "Editar Reserva",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Sección 1: Datos Personales
                FormSectionHeaderElite(icon = Icons.Rounded.Person, title = "Información del Cliente")
                OutlinedTextField(
                    value = viewModel.customerName,
                    onValueChange = { viewModel.customerName = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Sección 2: Fecha
                FormSectionHeaderElite(icon = Icons.Rounded.CalendarMonth, title = "Fecha Seleccionada")
                val dateInteractionSource = remember { MutableInteractionSource() }
                LaunchedEffect(dateInteractionSource) {
                    dateInteractionSource.interactions.collect {
                        if (it is PressInteraction.Release) datePickerDialog.show()
                    }
                }

                OutlinedTextField(
                    value = viewModel.reservationDate,
                    onValueChange = { },
                    readOnly = true,
                    placeholder = { Text("¿Cuándo jugarás?", color = Color.White.copy(alpha = 0.4f)) },
                    interactionSource = dateInteractionSource,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Sección 3: Horarios
                FormSectionHeaderElite(icon = Icons.Rounded.Schedule, title = "Horarios Disponibles")
                val timeSlots = listOf("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00")
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(85.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(timeSlots) { time ->
                        val isOccupied = existingReservations.any { 
                            it.id != editId && 
                            it.reservationDate == viewModel.reservationDate && 
                            it.reservationTime.contains(time) && 
                            it.canchaType == viewModel.canchaType
                        }
                        val isSelected = viewModel.selectedTimes.contains(time)
                        
                        TimeSlotItemElite(
                            time = time,
                            isSelected = isSelected,
                            isOccupied = isOccupied,
                            onToggle = { viewModel.toggleTimeSlot(time) }
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = "⚠️ $errorMessage", 
                        color = Color(0xFFEF5350), 
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }

            // PIE DE PÁGINA ELITE (Glassmorphism Footer)
            Surface(
                color = Color.White.copy(alpha = 0.03f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    val totalPrice = viewModel.hourlyPrice * viewModel.selectedTimes.size
                    val hoursCount = viewModel.selectedTimes.size

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Monto total (${hoursCount}h)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "S/. ${String.format("%.2f", totalPrice)}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        
                        Button(
                            onClick = {
                                if (viewModel.isFormValid) {
                                    viewModel.save()
                                } else {
                                    errorMessage = "Por favor, elige al menos una hora."
                                }
                            },
                            modifier = Modifier.height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Rounded.CheckCircle, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSlotItemElite(time: String, isSelected: Boolean, isOccupied: Boolean, onToggle: () -> Unit) {
    Surface(
        onClick = { if (!isOccupied) onToggle() },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = when {
            isOccupied -> Color.White.copy(alpha = 0.02f)
            isSelected -> MaterialTheme.colorScheme.primary
            else -> Color.White.copy(alpha = 0.05f)
        },
        contentColor = when {
            isOccupied -> Color.White.copy(alpha = 0.1f)
            isSelected -> Color.Black
            else -> Color.White
        },
        border = if (!isSelected && !isOccupied) BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null
    ) {
        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun FormSectionHeaderElite(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

package com.tecsup.agendacitasdeportivas.ui.screens

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.Check
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
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
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

    // Observar éxito de guardado para navegar al detalle
    LaunchedEffect(viewModel.lastSavedId) {
        viewModel.lastSavedId?.let { id ->
            navController.navigate("detail_screen/$id") {
                // Limpiar el historial del formulario para que no regrese al volver atrás
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
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (editId == null) "Confirmar Reserva" else "Editar Reserva",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Sección 1: Datos Personales
                FormSectionHeader(icon = Icons.Rounded.Person, title = "Datos del Cliente")
                OutlinedTextField(
                    value = viewModel.customerName,
                    onValueChange = { viewModel.customerName = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp)) {
                            Icon(Icons.Rounded.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(Modifier.width(8.dp))
                            VerticalDivider(modifier = Modifier.height(20.dp).padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sección 2: Fecha
                FormSectionHeader(icon = Icons.Rounded.CalendarMonth, title = "Fecha de Reserva")
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
                    placeholder = { Text("Seleccione el día") },
                    leadingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp)) {
                            Icon(Icons.Rounded.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(Modifier.width(8.dp))
                            VerticalDivider(modifier = Modifier.height(20.dp).padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    },
                    interactionSource = dateInteractionSource,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sección 3: Horarios
                FormSectionHeader(icon = Icons.Rounded.Schedule, title = "Horas Disponibles")
                val timeSlots = listOf("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00")
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(85.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(timeSlots) { time ->
                        val isOccupied = existingReservations.any { 
                            it.id != editId && 
                            it.reservationDate == viewModel.reservationDate && 
                            it.reservationTime.contains(time) && 
                            it.canchaType == viewModel.canchaType
                        }
                        val isSelected = viewModel.selectedTimes.contains(time)
                        
                        TimeSlotItem(
                            time = time,
                            isSelected = isSelected,
                            isOccupied = isOccupied,
                            onToggle = { viewModel.toggleTimeSlot(time) }
                        )
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("⚠️ $errorMessage", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            // SECCIÓN FINAL UNIFICADA
            Box(contentAlignment = Alignment.TopEnd) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
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
                                    if (hoursCount > 0) "Duración Total: $hoursCount h" else "Sin horas seleccionadas",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (hoursCount > 0) "S/. ${String.format("%.2f", totalPrice)}" else "Seleccione horas",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Text(
                                "Monto a Pagar", // Cambiado de TOTAL A PAGAR para diferenciar
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (viewModel.isFormValid) {
                                    viewModel.save()
                                } else {
                                    errorMessage = "Complete todos los campos y elija al menos una hora."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                if (editId == null) "CONFIRMAR RESERVA" else "GUARDAR CAMBIOS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        if (viewModel.isFormValid) {
                            viewModel.save()
                        } else {
                            errorMessage = "Complete todos los campos y elija al menos una hora."
                        }
                    },
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .offset(y = (-28).dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = "Confirmar")
                }
            }
        }
    }
}

@Composable
fun TimeSlotItem(
    time: String,
    isSelected: Boolean,
    isOccupied: Boolean,
    onToggle: () -> Unit
) {
    val backgroundColor = when {
        isOccupied -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = when {
        isOccupied -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val borderColor = when {
        isOccupied -> Color.Transparent
        isSelected -> Color.Transparent
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        onClick = { if (!isOccupied) onToggle() },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        contentColor = contentColor,
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textDecoration = if (isOccupied) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
            )
        }
    }
}

@Composable
fun FormSectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

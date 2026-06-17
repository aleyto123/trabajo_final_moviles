package com.tecsup.agendacitasdeportivas.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.data.repository.CanchaReservationRepository
import com.tecsup.agendacitasdeportivas.ui.state.ReservationUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val reservation: CanchaReservationEntity) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class ReservationViewModel(
    private val repository: CanchaReservationRepository
) : ViewModel() {

    // Estado de la UI para la lista (Consumo de Room en tiempo real)
    val uiState: StateFlow<ReservationUiState> = repository.allReservations
        .map<List<CanchaReservationEntity>, ReservationUiState> { reservations ->
            ReservationUiState.Success(reservations)
        }
        .onStart { emit(ReservationUiState.Loading) }
        .catch { e -> emit(ReservationUiState.Error(e.message ?: "Error al cargar reservas")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReservationUiState.Loading
        )

    // Estado de la UI para el detalle
    private val _detailState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailState: StateFlow<DetailUiState> = _detailState.asStateFlow()

    // Estados para el Formulario (Control Bidireccional)
    var customerName by mutableStateOf("")
    var canchaType by mutableStateOf("")
    var reservationDate by mutableStateOf("")
    var selectedTimes by mutableStateOf(setOf<String>())
    var hourlyPrice by mutableDoubleStateOf(0.0)
    var paymentStatus by mutableStateOf("Pendiente")

    // Validación básica
    val isFormValid: Boolean
        get() = customerName.isNotBlank() && 
                canchaType.isNotBlank() && 
                reservationDate.isNotBlank() && 
                selectedTimes.isNotEmpty()

    fun toggleTimeSlot(time: String) {
        selectedTimes = if (selectedTimes.contains(time)) {
            selectedTimes - time
        } else {
            selectedTimes + time
        }
    }

    fun clearForm() {
        customerName = ""
        canchaType = ""
        reservationDate = ""
        selectedTimes = emptySet()
        hourlyPrice = 0.0
        paymentStatus = "Pendiente"
    }

    fun loadReservation(id: String) {
        viewModelScope.launch {
            try {
                _detailState.value = DetailUiState.Loading
                val reservation = repository.getReservationById(id)
                if (reservation != null) {
                    _detailState.value = DetailUiState.Success(reservation)
                } else {
                    _detailState.value = DetailUiState.Error("Reserva no encontrada")
                }
            } catch (e: Exception) {
                _detailState.value = DetailUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun insert() {
        if (!isFormValid) return
        viewModelScope.launch {
            try {
                val reservation = CanchaReservationEntity(
                    userId = "USER_DEFAULT",
                    canchaType = canchaType,
                    customerName = customerName,
                    reservationDate = reservationDate,
                    reservationTime = selectedTimes.toList().sorted().joinToString(", "),
                    hourlyPrice = hourlyPrice * selectedTimes.size,
                    paymentStatus = paymentStatus
                )
                repository.insertReservation(reservation)
                clearForm()
            } catch (e: Exception) {
                // Error handled by repository abstraction ideally
            }
        }
    }

    fun update(reservation: CanchaReservationEntity) {
        viewModelScope.launch {
            try {
                repository.updateReservation(reservation)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun delete(reservation: CanchaReservationEntity) {
        viewModelScope.launch {
            try {
                repository.deleteReservation(reservation)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}
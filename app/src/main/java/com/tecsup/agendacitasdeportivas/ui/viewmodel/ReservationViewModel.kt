package com.tecsup.agendacitasdeportivas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.data.repository.CanchaReservationRepository
import com.tecsup.agendacitasdeportivas.ui.state.ReservationUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReservationViewModel(
    private val repository: CanchaReservationRepository
) : ViewModel() {

    // Estado de la UI expuesto mediante StateFlow y clase sellada
    val uiState: StateFlow<ReservationUiState> = repository.allReservations
        .map<List<CanchaReservationEntity>, ReservationUiState> { reservations ->
            ReservationUiState.Success(reservations)
        }
        .onStart { emit(ReservationUiState.Loading) }
        .catch { e -> emit(ReservationUiState.Error(e.message ?: "Error desconocido")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReservationUiState.Loading
        )

    fun insert(reservation: CanchaReservationEntity) {
        viewModelScope.launch {
            repository.insertReservation(reservation)
        }
    }

    fun update(reservation: CanchaReservationEntity) {
        viewModelScope.launch {
            repository.updateReservation(reservation)
        }
    }

    fun delete(reservation: CanchaReservationEntity) {
        viewModelScope.launch {
            repository.deleteReservation(reservation)
        }
    }
    
    suspend fun getReservationById(id: String): CanchaReservationEntity? {
        return repository.getReservationById(id)
    }
}
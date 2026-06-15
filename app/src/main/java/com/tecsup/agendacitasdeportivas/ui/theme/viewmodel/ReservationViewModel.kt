package com.tecsup.agendacitasdeportivas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity
import com.tecsup.agendacitasdeportivas.data.repository.CanchaReservationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReservationViewModel(
    private val repository: CanchaReservationRepository
) : ViewModel() {

    // Estado de la UI para la base de datos local (Room) usando StateFlow
    val uiState: StateFlow<List<CanchaReservationEntity>> = repository.allReservations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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
}
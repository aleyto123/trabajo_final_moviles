package com.tecsup.agendacitasdeportivas.ui.state

import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity

sealed interface ReservationUiState {
    object Loading : ReservationUiState
    data class Success(val reservations: List<CanchaReservationEntity>) : ReservationUiState
    data class Error(val message: String) : ReservationUiState
}
package com.tecsup.agendacitasdeportivas.ui.state

import com.tecsup.agendacitasdeportivas.data.local.CanchaReservationEntity

sealed interface ReservationUiState {
    object Loading : ReservationUiState
    data class Success(val reservations: List<CanchaReservationEntity>) : ReservationUiState
    data class Error(val message: String) : ReservationUiState
}

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val reservation: CanchaReservationEntity) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

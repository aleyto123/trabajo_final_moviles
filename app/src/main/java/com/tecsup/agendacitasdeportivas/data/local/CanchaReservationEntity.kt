package com.tecsup.agendacitasdeportivas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "table_cancha_reservations")
data class CanchaReservationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val canchaType: String,
    val customerName: String,
    val reservationDate: String,
    val reservationTime: String,
    val hourlyPrice: Double,
    val paymentStatus: String,
    val isSynced: Boolean = false
)
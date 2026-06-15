package com.tecsup.agendacitasdeportivas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cancha_reservations")
data class CanchaReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombreCancha: String,
    val fecha: String,
    val hora: String
)
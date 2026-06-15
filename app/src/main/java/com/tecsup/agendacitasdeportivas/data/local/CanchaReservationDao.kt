package com.tecsup.agendacitasdeportivas.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CanchaReservationDao {
    @Query("SELECT * FROM cancha_reservations")
    fun getAllReservations(): Flow<List<CanchaReservationEntity>>

    @Insert
    suspend fun insert(reservation: CanchaReservationEntity)

    @Update
    suspend fun update(reservation: CanchaReservationEntity)

    @Delete
    suspend fun delete(reservation: CanchaReservationEntity)
}
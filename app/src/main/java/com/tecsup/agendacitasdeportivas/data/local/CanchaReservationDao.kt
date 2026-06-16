package com.tecsup.agendacitasdeportivas.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CanchaReservationDao {
    @Query("SELECT * FROM table_cancha_reservations")
    fun getAllReservations(): Flow<List<CanchaReservationEntity>>

    @Query("SELECT * FROM table_cancha_reservations WHERE id = :id")
    suspend fun getReservationById(id: String): CanchaReservationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reservation: CanchaReservationEntity)

    @Update
    suspend fun update(reservation: CanchaReservationEntity)

    @Delete
    suspend fun delete(reservation: CanchaReservationEntity)
}
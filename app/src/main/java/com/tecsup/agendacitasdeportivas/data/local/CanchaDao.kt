package com.tecsup.agendacitasdeportivas.data.local

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface CanchaDao {
    @Insert
    suspend fun insert(cancha: CanchaEntity)
}
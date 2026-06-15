package com.tecsup.agendacitasdeportivas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CanchaReservationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun canchaReservationDao(): CanchaReservationDao
}
package com.tecsup.agendacitasdeportivas.data.repository

import com.tecsup.agendacitasdeportivas.data.local.CanchaDao
import kotlinx.coroutines.flow.Flow

class CanchasRepository(private val canchaDao: CanchaDao) {

    val allCanchas: Flow<List<CanchaEntity>> = canchaDao.getAllCanchas()

    suspend fun insertarCancha(cancha: CanchaEntity) {
        canchaDao.insert(cancha)
    }
}
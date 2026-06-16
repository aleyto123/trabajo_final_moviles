package com.tecsup.agendacitasdeportivas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tecsup.agendacitasdeportivas.data.local.AppDatabase
import com.tecsup.agendacitasdeportivas.data.repository.CanchaReservationRepository
import com.tecsup.agendacitasdeportivas.ui.navigation.AppNavigation
import com.tecsup.agendacitasdeportivas.ui.theme.AgendaCitasDeportivasTheme
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ApiViewModel
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // 1. Inicializar la Base de Datos (Singleton)
            val database = AppDatabase.getDatabase(applicationContext)

            // 2. Inicializar el Repositorio
            val repository = CanchaReservationRepository(database.canchaReservationDao())

            // 3. Factory para ViewModels
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ReservationViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ReservationViewModel(repository) as T
                    }
                    if (modelClass.isAssignableFrom(ApiViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ApiViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Clase ViewModel desconocida")
                }
            }

            val reservationViewModel = ViewModelProvider(this, factory)[ReservationViewModel::class.java]
            val apiViewModel = ViewModelProvider(this, factory)[ApiViewModel::class.java]

            setContent {
                Box(modifier = Modifier.fillMaxSize().background(Color.Blue), contentAlignment = Alignment.Center) {
                    Text("DEBUG: Si ves esto en azul, Compose funciona", color = Color.White)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error en onCreate", e)
            setContent {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color.Red) {
                        Text("ERROR CRÍTICO: ${e.message}")
                    }
                }
            }
        }
    }
}
package com.tecsup.agendacitasdeportivas

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
                    return when {
                        modelClass.isAssignableFrom(ReservationViewModel::class.java) -> 
                            ReservationViewModel(repository) as T
                        modelClass.isAssignableFrom(ApiViewModel::class.java) -> 
                            ApiViewModel(repository) as T
                        else -> throw IllegalArgumentException("Clase ViewModel desconocida")
                    }
                }
            }

            val reservationViewModel = ViewModelProvider(this, factory)[ReservationViewModel::class.java]
            val apiViewModel = ViewModelProvider(this, factory)[ApiViewModel::class.java]

            setContent {
                AgendaCitasDeportivasTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(reservationViewModel, apiViewModel)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error en onCreate", e)
            setContent {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Red) {
                    Text("ERROR CRÍTICO: ${e.message}\nConsulte Logcat para más detalles.")
                }
            }
        }
    }
}
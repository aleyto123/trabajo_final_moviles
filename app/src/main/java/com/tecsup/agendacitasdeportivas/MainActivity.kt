package com.tecsup.agendacitasdeportivas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.google.firebase.messaging.FirebaseMessaging
import com.tecsup.agendacitasdeportivas.data.local.AppDatabase
import com.tecsup.agendacitasdeportivas.data.repository.AuthRepositoryImpl
import com.tecsup.agendacitasdeportivas.data.repository.CanchaReservationRepository
import com.tecsup.agendacitasdeportivas.ui.navigation.AppNavigation
import com.tecsup.agendacitasdeportivas.ui.theme.AgendaCitasDeportivasTheme
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ApiViewModel
import com.tecsup.agendacitasdeportivas.ui.viewmodel.AuthViewModel
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Permiso de notificaciones concedido")
        } else {
            Log.w("FCM", "Permiso de notificaciones denegado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Firebase primero
        try {
            Firebase.initialize(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al inicializar Firebase", e)
        }

        // Pedir permiso de notificaciones (Android 13+)
        askNotificationPermission()
        
        // Suscribirse al tema de reservas para notificaciones Push del servidor
        FirebaseMessaging.getInstance().subscribeToTopic("reservas")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) Log.d("FCM", "Suscrito al tema: reservas")
            }

        try {
            // 1. Inicializar la Base de Datos (Singleton)
            val database = AppDatabase.getDatabase(applicationContext)

            // 2. Inicializar el Repositorio (Pasamos context para notificaciones)
            val repository = CanchaReservationRepository(
                database.canchaReservationDao(),
                applicationContext
            )
            val authRepository = AuthRepositoryImpl()

            // 3. Factory para ViewModels
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return when {
                        modelClass.isAssignableFrom(ReservationViewModel::class.java) -> 
                            ReservationViewModel(repository) as T
                        modelClass.isAssignableFrom(ApiViewModel::class.java) -> 
                            ApiViewModel(repository) as T
                        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                            AuthViewModel(authRepository) as T
                        else -> throw IllegalArgumentException("Clase ViewModel desconocida")
                    }
                }
            }

            val reservationViewModel = ViewModelProvider(this, factory)[ReservationViewModel::class.java]
            val apiViewModel = ViewModelProvider(this, factory)[ApiViewModel::class.java]
            val authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

            setContent {
                AgendaCitasDeportivasTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(reservationViewModel, apiViewModel, authViewModel)
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

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
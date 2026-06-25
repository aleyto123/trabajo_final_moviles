package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.data.model.CanchaProvider
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, initialCanchaId: String? = null) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val initialCancha = remember(initialCanchaId) {
        CanchaProvider.allCanchas.find { it.id == initialCanchaId }
    }

    // Configuración obligatoria de OsmDroid para evitar bloqueos de servidores de teselas
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Instancia de MapView recordada para manejar su ciclo de vida
    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
        }
    }

    // Manejo del ciclo de vida nativo de OsmDroid dentro de Compose
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Geolocalización (OpenStreetMap)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = {
                mapView.apply {
                    controller.setZoom(16.0)
                    val startPoint = if (initialCancha != null) {
                        GeoPoint(initialCancha.latitude, initialCancha.longitude)
                    } else {
                        GeoPoint(-12.0673, -77.0337) // Estadio Nacional de Lima
                    }
                    controller.setCenter(startPoint)
                }
            },
            update = { mv ->
                // Limpieza y redibujado de marcadores para evitar duplicados
                mv.overlays.clear()
                
                CanchaProvider.allCanchas.forEach { cancha ->
                    val marker = Marker(mv)
                    marker.position = GeoPoint(cancha.latitude, cancha.longitude)
                    marker.title = cancha.name
                    marker.snippet = "${cancha.type} - S/. ${cancha.pricePerHour}"
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mv.overlays.add(marker)
                }
                
                mv.invalidate() // Forzar refresco visual
            }
        )
    }
}

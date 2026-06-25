package com.tecsup.agendacitasdeportivas.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavController, initialCanchaId: String? = null) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val initialCancha = remember(initialCanchaId) {
        CanchaProvider.allCanchas.find { it.id == initialCanchaId }
    }

    // Instancia de MapView
    val mapView = remember {
        MapView(context).apply {
            setMultiTouchControls(true)
        }
    }

    // Overlay para la ubicación del usuario
    val myLocationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            disableMyLocation() // Se habilita solo con permisos
        }
    }

    // Launcher para pedir permisos
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            myLocationOverlay.enableMyLocation()
        }
    }

    // Configuración inicial
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Ciclo de vida
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    // Re-habilitar solo si ya tenía permiso (opcional, mejor dejarlo al launcher)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    myLocationOverlay.disableMyLocation()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Geolocalización", fontWeight = FontWeight.Bold) },
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
                        GeoPoint(-12.0673, -77.0337)
                    }
                    controller.setCenter(startPoint)
                    
                    // Aseguramos que el overlay de ubicación esté presente
                    if (!overlays.contains(myLocationOverlay)) {
                        overlays.add(myLocationOverlay)
                    }
                }
            },
            update = { mv ->
                // Actualizar marcadores de canchas
                val markersToRemove = mv.overlays.filterIsInstance<Marker>()
                mv.overlays.removeAll(markersToRemove)
                
                CanchaProvider.allCanchas.forEach { cancha ->
                    val marker = Marker(mv)
                    marker.position = GeoPoint(cancha.latitude, cancha.longitude)
                    marker.title = cancha.name
                    marker.snippet = "${cancha.type} - S/. ${cancha.pricePerHour}"
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mv.overlays.add(marker)
                }
                
                mv.invalidate()
            }
        )
    }
}

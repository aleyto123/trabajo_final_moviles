package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.ui.state.ApiUiState
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ApiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiScreen(navController: NavController, viewModel: ApiViewModel) {
    val weatherState by viewModel.weatherState.collectAsState()
    val geminiState by viewModel.geminiState.collectAsState()
    var prompt by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchWeather(-12.04, -77.03)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Clima y Asistente") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Estado del Clima (Lima)", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = weatherState) {
                is ApiUiState.Loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ApiUiState.Success -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Temperatura Actual: ${state.data.current_weather.temperature}°C", style = MaterialTheme.typography.headlineSmall)
                            Text("Código de Clima: ${state.data.current_weather.weathercode}")
                        }
                    }
                }
                is ApiUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.fetchWeather(-12.04, -77.03) }) {
                            Text("Reintentar")
                        }
                    }
                }
                is ApiUiState.Idle -> {}
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Asistente IA", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Duda sobre reservas...") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.askGemini("LLAVE KEY DE LA API DEL BOT", prompt)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Consultar IA")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = geminiState) {
                is ApiUiState.Loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ApiUiState.Success -> {
                    val respuesta = state.data.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Sin respuesta."
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text(
                            text = respuesta,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is ApiUiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
                else -> Text("Esperando consulta...", color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
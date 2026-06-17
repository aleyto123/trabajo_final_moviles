package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.ui.state.ApiUiState
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ApiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiScreen(navController: NavController, viewModel: ApiViewModel) {
    val weatherState by viewModel.weatherState.collectAsState()
    val groqState by viewModel.groqState.collectAsState()
    var prompt by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.fetchWeather(-12.04, -77.03) // Lima
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistente ChatBot (Groq)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchWeather(-12.04, -77.03) }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Sección del Clima
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estado del Clima (Lima)", style = MaterialTheme.typography.labelLarge)
                    when (val state = weatherState) {
                        is ApiUiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        is ApiUiState.Success -> {
                            Text(
                                text = "${state.data.current_weather.temperature}°C",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Ideal para agendar tus citas hoy.")
                        }
                        is ApiUiState.Error -> {
                            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.fetchWeather(-12.04, -77.03) }) {
                                Text("Reintentar")
                            }
                        }
                        else -> {}
                    }
                }
            }

            // Sección del ChatBot
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                Text("ChatBot - Consultas", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                when (val state = groqState) {
                    is ApiUiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    is ApiUiState.Success -> {
                        val response = state.data.choices.firstOrNull()?.message?.content ?: "Sin respuesta"
                        Card(
                            shape = RoundedCornerShape(8.dp, 16.dp, 16.dp, 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
                        ) {
                            Text(response, modifier = Modifier.padding(12.dp))
                        }
                    }
                    is ApiUiState.Error -> {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    else -> {
                        Text("Hola, soy tu asesor. Pregúntame sobre canchas, precios o recomendaciones según el clima.", color = Color.Gray)
                    }
                }
            }

            // Input de Chat
            Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("Escribe aquí...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (prompt.isNotBlank()) {
                                viewModel.askChatBot(prompt)
                                prompt = ""
                            }
                        },
                        enabled = prompt.isNotBlank() && groqState !is ApiUiState.Loading
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}

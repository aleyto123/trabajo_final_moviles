package com.tecsup.agendacitasdeportivas.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    LaunchedEffect(Unit) { viewModel.fetchWeather(-12.04, -77.03) }

    Scaffold(
        containerColor = Color(0xFF0D0B1A),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Asistente IA", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 8.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchWeather(-12.04, -77.03) }) {
                        Icon(Icons.Rounded.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- CLIMA ELITE ---
            Box(modifier = Modifier.padding(16.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CLIMA ACTUAL", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = Color.White.copy(alpha = 0.4f))
                            when (val state = weatherState) {
                                is ApiUiState.Success -> {
                                    Text(text = "${state.data.current_weather.temperature}°C", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    Text("Ideal para un partido", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                is ApiUiState.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                                else -> Text("--°C", style = MaterialTheme.typography.displayMedium, color = Color.White)
                            }
                        }
                        Surface(
                            modifier = Modifier.size(64.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.WbSunny, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // --- CHATBOT AREA ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedContent(targetState = groqState, label = "chat") { state ->
                    when (state) {
                        is ApiUiState.Success -> ChatBubbleElite(state.data.choices.firstOrNull()?.message?.content ?: "", isBot = true)
                        is ApiUiState.Loading -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape), color = MaterialTheme.colorScheme.primary)
                        }
                        is ApiUiState.Error -> ChatErrorElite(state.message)
                        else -> ChatBubbleElite("Hola, ¿en qué puedo ayudarte con tus reservas hoy?", isBot = true)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- INPUT PANEL ELITE ---
            Surface(
                color = Color.White.copy(alpha = 0.03f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Row(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("Mensaje...", color = Color.White.copy(alpha = 0.4f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledIconButton(
                        onClick = { if (prompt.isNotBlank()) { viewModel.askChatBot(prompt); prompt = "" } },
                        enabled = prompt.isNotBlank() && groqState !is ApiUiState.Loading,
                        modifier = Modifier.size(52.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, null)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleElite(text: String, isBot: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = if (isBot) Alignment.Start else Alignment.End) {
        Surface(
            color = if (isBot) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(topStart = if (isBot) 0.dp else 24.dp, topEnd = if (isBot) 24.dp else 0.dp, bottomEnd = 24.dp, bottomStart = 24.dp),
            border = if (isBot) BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isBot) Color.White else Color.Black
            )
        }
        Text(
            text = if (isBot) "ASISTENTE" else "TÚ",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun ChatErrorElite(message: String) {
    Surface(
        color = Color(0xFFC62828).copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text(text = "⚠️ $message", modifier = Modifier.padding(16.dp), color = Color(0xFFEF5350), style = MaterialTheme.typography.bodySmall)
    }
}

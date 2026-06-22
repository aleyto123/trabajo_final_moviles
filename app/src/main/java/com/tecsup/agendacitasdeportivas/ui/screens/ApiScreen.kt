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

    LaunchedEffect(Unit) {
        viewModel.fetchWeather(-12.04, -77.03)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Asistente IA", 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchWeather(-12.04, -77.03) }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            // --- SECCIÓN CLIMA ---
            Box(modifier = Modifier.padding(16.dp)) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
                                    )
                                )
                            )
                            .padding(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.WbSunny, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "CLIMA EN LIMA", 
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        letterSpacing = 1.sp
                                    )
                                }
                                when (val state = weatherState) {
                                    is ApiUiState.Success -> {
                                        Text(
                                            text = "${state.data.current_weather.temperature}°C",
                                            style = MaterialTheme.typography.displayMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            "¡El día está perfecto para jugar!", 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    is ApiUiState.Loading -> CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp).padding(top = 16.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    else -> Text("--°C", style = MaterialTheme.typography.displayMedium)
                                }
                            }
                            Surface(
                                modifier = Modifier.size(80.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (weatherState is ApiUiState.Success) Icons.Rounded.WbSunny else Icons.Rounded.Refresh, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(48.dp),
                                        tint = Color(0xFFFFD54F)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- SECCIÓN CHATBOT ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
                ) {
                    Icon(
                        Icons.Rounded.SmartToy, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Tu asesor personal", 
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedContent(
                    targetState = groqState, 
                    label = "chat",
                    transitionSpec = {
                        fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically()
                    }
                ) { state ->
                    when (state) {
                        is ApiUiState.Success -> {
                            val response = state.data.choices.firstOrNull()?.message?.content ?: ""
                            ChatBubble(text = response, isBot = true)
                        }
                        is ApiUiState.Loading -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(CircleShape))
                        }
                        is ApiUiState.Error -> ChatError(state.message)
                        else -> ChatBubble(text = "Hola, ¿en qué puedo ayudarte con tus reservas hoy?", isBot = true)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- INPUT DE CHAT ---
            Surface(
                tonalElevation = 12.dp,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("Pregúntame algo...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledIconButton(
                        onClick = {
                            if (prompt.isNotBlank()) {
                                viewModel.askChatBot(prompt)
                                prompt = ""
                            }
                        },
                        enabled = prompt.isNotBlank() && groqState !is ApiUiState.Loading,
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isBot: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = if (isBot) Alignment.Start else Alignment.End
    ) {
        Surface(
            color = if (isBot) Color(0xFF311B92).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(
                topStart = if (isBot) 0.dp else 24.dp, 
                topEnd = if (isBot) 24.dp else 0.dp, 
                bottomEnd = 24.dp, 
                bottomStart = 24.dp
            ),
            border = if (isBot) BorderStroke(1.dp, Color(0xFF311B92).copy(alpha = 0.2f)) else null,
            tonalElevation = if (isBot) 2.dp else 0.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                color = if (isBot) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = if (isBot) "Asistente IA" else "Tú",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ChatError(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⚠️ $message",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

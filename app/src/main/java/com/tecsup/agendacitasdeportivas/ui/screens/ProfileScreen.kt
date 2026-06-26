package com.tecsup.agendacitasdeportivas.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tecsup.agendacitasdeportivas.ui.state.ReservationUiState
import com.tecsup.agendacitasdeportivas.ui.viewmodel.AuthViewModel
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import kotlin.math.cos
import kotlin.math.sin

// FORMA HEXAGONAL PARA EL AVATAR (ADIÓS A LOS CÍRCULOS SIMPLES)
val HexagonShape = GenericShape { size: Size, _ ->
    val radius = size.width / 2f
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val angle = 2.0 * Math.PI / 6
    moveTo(centerX + radius * cos(0.0).toFloat(), centerY + radius * sin(0.0).toFloat())
    for (i in 1..5) {
        lineTo(centerX + radius * cos(angle * i).toFloat(), centerY + radius * sin(angle * i).toFloat())
    }
    close()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController, 
    authViewModel: AuthViewModel,
    reservationViewModel: ReservationViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val reservationState by reservationViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val reservationCount = if (reservationState is ReservationUiState.Success) {
        (reservationState as ReservationUiState.Success).reservations.size
    } else 0
    val points = reservationCount * 50

    var showEditNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(currentUser?.displayName ?: "Usuario") }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Personalizar Identidad", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Tu nombre de guerrero") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            authViewModel.updateName(newName)
                            showEditNameDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Actualizar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0D0B1A) // Fondo ultra oscuro premium
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            
            // ELEMENTOS DE FONDO ARTÍSTICOS (Abstractos, no circulares)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2C254A), Color(0xFF0D0B1A))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TOP BAR PERSONALIZADA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Mi Perfil", 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // AVATAR HEXAGONAL DE ALTA TECNOLOGÍA
                Box(contentAlignment = Alignment.Center) {
                    // Brillo exterior hexagonal
                    Surface(
                        modifier = Modifier.size(160.dp),
                        shape = HexagonShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {}
                    
                    // Contenedor principal hexagonal
                    Surface(
                        modifier = Modifier.size(140.dp),
                        shape = HexagonShape,
                        color = Color(0xFF1A1633),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        shadowElevation = 24.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // LA PERSONITA: Rediseñada con look de ÍCONO DE JUGADOR
                            Icon(
                                imageVector = Icons.Rounded.Person4, 
                                contentDescription = null,
                                modifier = Modifier.size(85.dp),
                                tint = Color.White
                            )
                        }
                    }
                    
                    // Badge de Edición (Cuchilla lateral)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-10).dp, y = (-10).dp)
                            .size(44.dp)
                            .clickable { showEditNameDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 8.dp
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit, 
                            contentDescription = "Editar",
                            modifier = Modifier.padding(10.dp),
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // NOMBRE Y RANGO
                Text(
                    text = currentUser?.displayName ?: "Usuario",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Miembro CanchaLibre",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // DASHBOARD DE ESTADÍSTICAS (Diseño Unificado Moderno)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItemElite("RESERVAS", reservationCount.toString(), Icons.Rounded.Event)
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)))
                        StatItemElite("TROFEOS", points.toString(), Icons.Rounded.EmojiEvents)
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)))
                        StatItemElite("RANGO", if (points >= 500) "PRO" else "AMTR", Icons.Rounded.Star)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // SECCIONES DE INFORMACIÓN (Glassmorphism sutil)
                InfoSectionElite(
                    title = "Información Personal",
                    items = listOf(
                        InfoRowData(Icons.Rounded.Email, "Correo electrónico", currentUser?.email ?: "usuario@ejemplo.com"),
                        InfoRowData(Icons.Rounded.Badge, "ID de usuario", currentUser?.uid?.take(12) ?: "********"),
                        InfoRowData(Icons.Rounded.Phone, "Teléfono", "+51 987 654 321")
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                InfoSectionElite(
                    title = "Ajustes de Cuenta",
                    items = listOf(
                        InfoRowData(Icons.Rounded.Notifications, "Notificaciones", "Activado", true),
                        InfoRowData(Icons.Rounded.Security, "Privacidad", null, true),
                        InfoRowData(Icons.Rounded.QuestionMark, "Centro de ayuda", null, true)
                    ),
                    onItemClick = { title ->
                        Toast.makeText(context, "Abriendo $title...", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(48.dp))

                // BOTÓN DE DESCONEXIÓN
                Button(
                    onClick = {
                        authViewModel.signOut(context)
                        navController.navigate("login") { popUpTo(0) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828).copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.3f))
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = Color(0xFFEF5350))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Cerrar Sesión", 
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFEF5350)
                    )
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun StatItemElite(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
    }
}

data class InfoRowData(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val value: String?, val clickable: Boolean = false)

@Composable
fun InfoSectionElite(
    title: String, 
    items: List<InfoRowData>,
    onItemClick: (String) -> Unit = {}
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.02f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = item.clickable) { onItemClick(item.label) }
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                            item.value?.let {
                                Text(it, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                        }
                        if (item.clickable) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White.copy(alpha = 0.2f))
                        }
                    }
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            thickness = 0.5.dp,
                            color = Color.White.copy(alpha = 0.05f)
                        )
                    }
                }
            }
        }
    }
}

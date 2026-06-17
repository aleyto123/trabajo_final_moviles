package com.tecsup.agendacitasdeportivas.data.model
import com.tecsup.agendacitasdeportivas.R

data class Cancha(
    val id: String,
    val name: String,
    val type: String, // Futbol, Tenis, Badminton, etc.
    val address: String,
    val pricePerHour: Double,
    val description: String,
    val imageRes: Int
)

object CanchaProvider {
    val allCanchas = listOf(
        Cancha(
            "1", "Estadio Nacional - F5", "Futbol", 
            "Calle Jose Diaz s/n, Lima", 50.0, 
            "Cancha de gras sintético profesional con iluminación LED.",
            R.drawable.futbol_1
        ),
        Cancha(
            "2", "Club Lawn Tennis - T1", "Tenis", 
            "Av. Salaverry 1210, Jesus Maria", 40.0, 
            "Cancha de arcilla reglamentaria para torneos.",
            R.drawable.tenis_1
        ),
        Cancha(
            "3", "Polideportivo Limatambo - B1", "Badminton", 
            "Calle Malachowski 560, San Borja", 30.0, 
            "Cancha techada con piso especial para bádminton.",
            R.drawable.badminton_1
        ),
        Cancha(
            "4", "Gras Sintético 'El Golazo'", "Futbol", 
            "Av. Universitaria 1200, San Miguel", 45.0, 
            "Excelente ubicación y amplios vestuarios.",
            R.drawable.futbol_2
        )
    )
}
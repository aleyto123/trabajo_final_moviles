package com.tecsup.agendacitasdeportivas.data.model

import com.tecsup.agendacitasdeportivas.R

data class Cancha(
    val id: String,
    val name: String,
    val type: String, // Futbol, Tenis, Badminton, etc.
    val address: String,
    val pricePerHour: Double,
    val description: String,
    val imageRes: Int,
    val latitude: Double,
    val longitude: Double
)

object CanchaProvider {
    val allCanchas = listOf(
        Cancha(
            "1", "Estadio Nacional - F5", "Futbol",
            "Calle Jose Diaz s/n, Lima", 50.0,
            "Cancha de gras sintético profesional con iluminación LED.",
            R.drawable.futbol_1,
            -12.0673, -77.0337
        ),
        Cancha(
            "2", "Club Lawn Tennis - T1", "Tenis",
            "Av. Salaverry 1210, Jesus Maria", 40.0,
            "Cancha de arcilla reglamentaria para torneos.",
            R.drawable.tenis_1,
            -12.0694, -77.0428
        ),
        Cancha(
            "3", "Polideportivo Limatambo - B1", "Badminton",
            "Calle Malachowski 560, San Borja", 30.0,
            "Cancha techada con piso especial para bádminton.",
            R.drawable.badminton_1,
            -12.1054, -77.0055
        ),
        Cancha(
            "4", "Gras Sintético 'El Golazo'", "Futbol",
            "Av. Universitaria 1200, San Miguel", 45.0,
            "Excelente ubicación y amplios vestuarios.",
            R.drawable.futbol_2,
            -12.0734, -77.0815
        ),

        // --- FÚTBOL ---
        Cancha(
            "5", "La Pichanga Norteña", "Futbol",
            "Av. Carlos Izaguirre 850, Los Olivos", 45.0,
            "Cancha de grass sintético ideal para fútbol 7, incluye chalecos.",
            R.drawable.futbol_2,
            -11.9912, -77.0620
        ),
        Cancha(
            "6", "El Monumentalito", "Futbol",
            "Av. Javier Prado Este 7500, Ate", 60.0,
            "Cancha techada con graderías pequeñas y buena iluminación.",
            R.drawable.futbol_2,
            -12.0558, -76.9358
        ),
        Cancha(
            "7", "Surco FC Arena", "Futbol",
            "Av. Tomás Marsano 2800, Surco", 55.0,
            "Instalaciones modernas con estacionamiento vigilado.",
            R.drawable.futbol_2,
            -12.1278, -77.0044
        ),
        Cancha(
            "8", "ChutaGol San Juan", "Futbol",
            "Av. Próceres de la Independencia 1200, SJL", 35.0,
            "Cancha económica al aire libre, ambiente familiar.",
            R.drawable.futbol_2,
            -12.0084, -77.0071
        ),
        Cancha(
            "9", "La Bombonera de Miraflores", "Futbol",
            "Calle Berlín 500, Miraflores", 80.0,
            "Grass premium, vestuarios con duchas de agua caliente.",
            R.drawable.futbol_2,
            -12.1194, -77.0348
        ),
        Cancha(
            "10", "DeporPlaza San Miguel", "Futbol",
            "Av. La Marina 2500, San Miguel", 50.0,
            "Complejo deportivo con cafetería y zona de parrillas.",
            R.drawable.futbol_2,
            -12.0747, -77.0905
        ),
        Cancha(
            "11", "El Diez de Magdalena", "Futbol",
            "Jr. Castilla 700, Magdalena del Mar", 45.0,
            "Cancha de fútbol 5 con mantenimiento reciente.",
            R.drawable.futbol_2,
            -12.0911, -77.0691
        ),
        Cancha(
            "12", "Canchas La Doce", "Futbol",
            "Av. Túpac Amaru 3000, Comas", 40.0,
            "Medidas oficiales de minifútbol, abierto 24 horas.",
            R.drawable.futbol_2,
            -11.9364, -77.0583
        ),
        Cancha(
            "13", "San Luis Arena", "Futbol",
            "Av. Rosa Toro 1400, San Luis", 55.0,
            "Excelente iluminación LED y malla de contención alta.",
            R.drawable.futbol_2,
            -12.0784, -76.9945
        ),
        Cancha(
            "14", "Golazo del Sur", "Futbol",
            "Av. Defensores del Morro 1500, Chorrillos", 50.0,
            "Grass de última generación, ideal para torneos locales.",
            R.drawable.futbol_2,
            -12.1742, -77.0252
        ),

        // --- BÁSQUET ---
        Cancha(
            "15", "Coliseo Dibós - Anexo", "Basquet",
            "Av. Angamos Este 2681, San Borja", 60.0,
            "Piso de madera flotante, aros profesionales.",
            R.drawable.im1,
            -12.1105, -77.0021
        ),
        Cancha(
            "16", "Hoop Surquillo", "Basquet",
            "Av. Tomás Marsano 500, Surquillo", 40.0,
            "Cancha de cemento pulido bajo techo.",
            R.drawable.im2,
            -12.1122, -77.0205
        ),
        Cancha(
            "17", "Parque Zonal Huiracocha", "Basquet",
            "Av. Próceres de la Independencia, SJL", 20.0,
            "Cancha municipal al aire libre, aros de metal resistente.",
            R.drawable.im3,
            -11.9961, -77.0012
        ),
        Cancha(
            "18", "El Salto Deportivo", "Basquet",
            "Av. La Fontana 1200, La Molina", 55.0,
            "Tableros de acrílico and piso sintético antideslizante.",
            R.drawable.futbol_2,
            -12.0697, -76.9455
        ),
        Cancha(
            "19", "Canchas IPD Lince", "Basquet",
            "Jirón Cápac Yupanqui, Lince", 30.0,
            "Espacio céntrico, ideal para entrenamientos rápidos.",
            R.drawable.futbol_2,
            -12.0831, -77.0392
        ),
        Cancha(
            "20", "Rey de la Cancha", "Basquet",
            "Av. Perú 2500, San Martín de Porres", 35.0,
            "Cancha de asfalto iluminada, zona segura.",
            R.drawable.futbol_2,
            -12.0289, -77.0789
        ),
        Cancha(
            "21", "Basket Norte Centro", "Basquet",
            "Av. Palmeras 1000, Los Olivos", 40.0,
            "Cancha techada, incluye préstamo de balones.",
            R.drawable.futbol_2,
            -11.9845, -77.0705
        ),
        Cancha(
            "22", "Zona 3 Puntos", "Basquet",
            "Av. Mariano Pastor Sevilla, Villa El Salvador", 25.0,
            "Complejo polideportivo amplio con tribunas.",
            R.drawable.futbol_2,
            -12.2145, -76.9389
        ),
        Cancha(
            "23", "Triple C Breña", "Basquet",
            "Av. Arica 800, Breña", 45.0,
            "Piso de poliuretano, excelente agarre para zapatillas.",
            R.drawable.futbol_2,
            -12.0589, -77.0489
        ),
        Cancha(
            "24", "Dunk Club Callao", "Basquet",
            "Av. Sáenz Peña 1400, Callao", 35.0,
            "Tableros profesionales, ideal para competencias.",
            R.drawable.futbol_2,
            -12.0592, -77.1189
        ),

        // --- VÓLEY ---
        Cancha(
            "25", "Voley Plaza Sur", "Voley",
            "Av. Pedro de Osma 400, Barranco", 45.0,
            "Cancha techada con piso oficial y malla regulable.",
            R.drawable.im4,
            -12.1492, -77.0215
        ),
        Cancha(
            "26", "Red Central Lince", "Voley",
            "Av. Arenales 1800, Lince", 40.0,
            "Ambiente cerrado, postes acolchados y buena luz.",
            R.drawable.im5,
            -12.0815, -77.0355
        ),
        Cancha(
            "27", "Mate Fuerte", "Voley",
            "Av. Gran Chimú 1500, SJL", 30.0,
            "Losa deportiva de cemento alisado, demarcación clara.",
            R.drawable.futbol_2,
            -12.0215, -76.9955
        ),
        Cancha(
            "28", "Las Voleibolistas", "Voley",
            "Av. Sucre 1200, Pueblo Libre", 50.0,
            "Cancha de taraflex profesional, ideal para academias.",
            R.drawable.futbol_2,
            -12.0785, -77.0645
        ),
        Cancha(
            "29", "Coliseo Jesús María", "Voley",
            "Av. Salaverry 1500, Jesús María", 55.0,
            "Ubicación céntrica, graderías y vestuarios limpios.",
            R.drawable.futbol_2,
            -12.0755, -77.0485
        ),
        Cancha(
            "30", "Bloqueo Alto", "Voley",
            "Carretera Central Km 6.5, Ate", 35.0,
            "Cancha ventilada al aire libre, ambiente tranquilo.",
            R.drawable.futbol_2,
            -12.0455, -76.9555
        ),
        Cancha(
            "31", "Complejo San Borja", "Voley",
            "Av. San Borja Norte 800, San Borja", 60.0,
            "Piso sintético, excelente iluminación para noche.",
            R.drawable.futbol_2,
            -12.1015, -76.9985
        ),
        Cancha(
            "32", "Saque As", "Voley",
            "Av. Alcázar 700, Rímac", 25.0,
            "Cancha municipal económica, malla incluida.",
            R.drawable.futbol_2,
            -12.0255, -77.0285
        ),
        Cancha(
            "33", "Arena Ciudad", "Voley",
            "Av. Pachacútec, Villa María del Triunfo", 30.0,
            "Cancha acondicionada para vóley playa con arena fina.",
            R.drawable.futbol_2,
            -12.1585, -76.9455
        ),
        Cancha(
            "34", "Voley Sur Lurín", "Voley",
            "Antigua Panamericana Sur Km 35, Lurín", 40.0,
            "Espacio campestre, ideal para torneos de fin de semana.",
            R.drawable.futbol_2,
            -12.2715, -76.8715
        ),

        // --- TENIS ---
        Cancha(
            "35", "Rinconada Tenis", "Tenis",
            "Calle La Rinconada 200, La Molina", 90.0,
            "Cancha de arcilla de primer nivel, mantenimiento diario.",
            R.drawable.im6,
            -12.0792, -76.9189
        ),
        Cancha(
            "36", "Terrazas Club - T2", "Tenis",
            "Malecón 28 de Julio 390, Miraflores", 100.0,
            "Vista al mar, superficie dura (cemento), iluminación LED.",
            R.drawable.im9,
            -12.1292, -77.0315
        ),
        Cancha(
            "37", "Arcilla Pro Surco", "Tenis",
            "Av. Encalada 1400, Surco", 75.0,
            "Polvo de ladrillo reglamentario, incluye recoge bolas.",
            R.drawable.futbol_2,
            -12.1125, -76.9785
        ),
        Cancha(
            "38", "Raqueta Dorada", "Tenis",
            "Av. Coronel Portillo 500, San Isidro", 120.0,
            "Exclusiva cancha techada, máxima privacidad.",
            R.drawable.im5,
            -12.0995, -77.0425
        ),
        Cancha(
            "39", "Match Point Barranco", "Tenis",
            "Av. El Sol Este 200, Barranco", 65.0,
            "Cancha rápida de asfalto pintado, ambiente bohemio.",
            R.drawable.im4,
            -12.1425, -77.0185
        ),
        Cancha(
            "40", "Aces Lima", "Tenis",
            "Av. Patriotas 300, San Miguel", 70.0,
            "Dos canchas de arcilla, zona de hidratación y baños.",
            R.drawable.futbol_2,
            -12.0815, -77.0915
        ),
        Cancha(
            "41", "Tenis Norte", "Tenis",
            "Av. Universitaria 4500, Los Olivos", 55.0,
            "Cancha de superficie dura, excelente para principiantes.",
            R.drawable.futbol_2,
            -11.9615, -77.0715
        ),
        Cancha(
            "42", "Set Point", "Tenis",
            "Jr. Trujillo 400, Magdalena del Mar", 60.0,
            "Arcilla tradicional, disponible desde las 6:00 AM.",
            R.drawable.futbol_2,
            -12.0945, -77.0745
        ),
        Cancha(
            "43", "El Golf Court", "Tenis",
            "Calle Los Eucaliptos 600, San Isidro", 110.0,
            "Mantenimiento premium, alquiler de raquetas disponible.",
            R.drawable.futbol_2,
            -12.0985, -77.0515
        ),
        Cancha(
            "44", "El Saque Chorrillos", "Tenis",
            "Alameda Sur 800, Chorrillos", 50.0,
            "Canchas públicas remodeladas de cemento poroso.",
            R.drawable.futbol_2,
            -12.1985, -77.0115
        ),

        // --- BÁDMINTON ---
        Cancha(
            "45", "Legado Panamericanos VES", "Badminton",
            "Av. Mariano Pastor Sevilla s/n, Villa El Salvador", 35.0,
            "Sede oficial con tapiz sintético profesional y techo alto.",
            R.drawable.im3,
            -12.2355, -76.9485
        ),
        Cancha(
            "46", "Club AELU - Pabellón B", "Badminton",
            "Jr. Paracas 565, Pueblo Libre", 60.0,
            "Excelente iluminación, piso de madera flotante antideslizante.",
            R.drawable.im1,
            -12.0745, -77.0685
        ),
        Cancha(
            "47", "Villa Deportiva del Callao", "Badminton",
            "Av. Guardia Chalaca 2199, Bellavista", 30.0,
            "Espacio amplio, ideal para entrenamientos de nivel intermedio.",
            R.drawable.im5,
            -12.0585, -77.1245
        ),
        Cancha(
            "48", "Plaza Bádminton Surco", "Badminton",
            "Av. La Encalada 800, Surco", 55.0,
            "Cancha techada, ambiente sin corrientes de aire para juego preciso.",
            R.drawable.im9,
            -12.1095, -76.9815
        ),
        Cancha(
            "49", "Polideportivo Costa Verde", "Badminton",
            "Circuito de Playas s/n, San Miguel", 40.0,
            "Instalaciones modernas frente al mar, mallas en perfecto estado.",
            R.drawable.im2,
            -12.0915, -77.0985
        ),
        Cancha(
            "50", "Palacio de la Juventud", "Badminton",
            "Av. Universitaria 2086, Los Olivos", 25.0,
            "Cancha municipal de cemento pulido, muy accesible.",
            R.drawable.im2,
            -11.9745, -77.0815
        ),
        Cancha(
            "51", "Coliseo Lince", "Badminton",
            "Av. Prolongación Iquitos 2000, Lince", 45.0,
            "Ubicación céntrica, incluye alquiler de raquetas y plumillas.",
            R.drawable.futbol_2,
            -12.0865, -77.0315
        ),
        Cancha(
            "52", "Smash Center Miraflores", "Badminton",
            "Av. Paseo de la República 4500, Miraflores", 70.0,
            "Cancha premium con piso de goma especializado y vestuarios.",
            R.drawable.futbol_2,
            -12.1155, -77.0265
        ),
        Cancha(
            "53", "Complejo San Juan", "Badminton",
            "Av. Los Héroes 1000, San Juan de Miraflores", 30.0,
            "Cancha techada multiusos adaptada para bádminton.",
            R.drawable.futbol_2,
            -12.1525, -76.9685
        ),
        Cancha(
            "54", "La Rinconada Indoor", "Badminton",
            "Av. La Molina 300, La Molina", 65.0,
            "Zona exclusiva, máxima privacidad y sin reflejos de luz.",
            R.drawable.futbol_2,
            -12.0855, -76.9215
        )
    )
}

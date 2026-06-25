package com.tecsup.agendacitasdeportivas.ui.navigation

import androidx.compose.runtime.Composable //dibuja la interfaz visual
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType // indica que tipo de dato se pasan entre pantallas
import androidx.navigation.compose.NavHost // gestiona el intercambio de pantallas cuando ocurre una acción
import androidx.navigation.compose.composable //define las rutas
import androidx.navigation.compose.rememberNavController // maneja el historial para volver atras
import androidx.navigation.navArgument // crea y configura los argumentos que viajaran atraves de la ruta

import com.tecsup.agendacitasdeportivas.ui.screens.* //trae todas las pantallas
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel // importa el cerebro que maneja el CRUD con room
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ApiViewModel // importa el cerebro de las apis
import com.tecsup.agendacitasdeportivas.ui.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    reservationViewModel: ReservationViewModel,
    apiViewModel: ApiViewModel,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController() // inicia y me lleva a otras pantallas y recuerda el camino
    val currentUser by authViewModel.currentUser.collectAsState()

    // Si el usuario ya está logueado, empezamos en la lista de canchas, si no, en login.
    val startDest = if (currentUser != null) "cancha_list" else "login"

    NavHost(navController = navController, startDestination = startDest) {
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }
        composable("profile") {
            ProfileScreen(navController, authViewModel)
        }
        composable("cancha_list") {
            CanchaListScreen(navController, authViewModel)
        }
        composable("list_screen") {
            ListScreen(navController, reservationViewModel)
        }
        composable(
            route = "form_screen/{canchaId}",
            arguments = listOf(navArgument("canchaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val canchaId = backStackEntry.arguments?.getString("canchaId") ?: ""
            FormScreen(navController, reservationViewModel, canchaId = canchaId, editId = null)
        }
        composable(
            route = "edit_screen/{reservationId}",
            arguments = listOf(navArgument("reservationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val resId = backStackEntry.arguments?.getString("reservationId") ?: ""
            FormScreen(navController, reservationViewModel, canchaId = "", editId = resId)
        }
        composable("api_screen") {
            ApiScreen(navController, apiViewModel)
        }
        composable(
            route = "detail_screen/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            ReservationDetailContent(navController, reservationViewModel, id = id)
        }
        composable(
            route = "cancha_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            CanchaDetailContent(navController, id)
        }
        composable("statistics_screen") {
            StatisticsScreen(navController, reservationViewModel)
        }
        composable(
            route = "map_screen/{canchaId}",
            arguments = listOf(navArgument("canchaId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val canchaId = backStackEntry.arguments?.getString("canchaId")
            MapScreen(navController, canchaId)
        }
    }
}

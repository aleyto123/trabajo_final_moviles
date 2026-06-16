package com.tecsup.agendacitasdeportivas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.agendacitasdeportivas.ui.screens.DetailScreen
import com.tecsup.agendacitasdeportivas.ui.screens.FormScreen
import com.tecsup.agendacitasdeportivas.ui.screens.ApiScreen
import com.tecsup.agendacitasdeportivas.ui.screens.ListScreen
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ApiViewModel

@Composable
fun AppNavigation(reservationViewModel: ReservationViewModel, apiViewModel: ApiViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list_screen") {
        composable("list_screen") {
            ListScreen(navController, reservationViewModel)
        }
        composable("form_screen") {
            FormScreen(navController, reservationViewModel)
        }
        composable("api_screen") {
            ApiScreen(navController, apiViewModel)
        }
        composable(
            route = "detail_screen/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DetailScreen(navController, reservationViewModel, reservaId = id)
        }
    }
}
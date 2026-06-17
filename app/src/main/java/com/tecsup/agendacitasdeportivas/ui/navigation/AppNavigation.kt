package com.tecsup.agendacitasdeportivas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.agendacitasdeportivas.ui.screens.*
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ApiViewModel

@Composable
fun AppNavigation(reservationViewModel: ReservationViewModel, apiViewModel: ApiViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "cancha_list") {
        composable("cancha_list") {
            CanchaListScreen(navController)
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
    }
}

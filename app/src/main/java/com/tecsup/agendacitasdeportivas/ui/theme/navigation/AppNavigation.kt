package com.tecsup.agendacitasdeportivas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.agendacitasdeportivas.ui.screens.DetailScreen
import com.tecsup.agendacitasdeportivas.ui.screens.ListScreen
import com.tecsup.agendacitasdeportivas.ui.viewmodel.ReservationViewModel

@Composable
fun AppNavigation(viewModel: ReservationViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list_screen") {
        composable("list_screen") {
            ListScreen(navController, viewModel)
        }
        composable(
            route = "detail_screen/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            DetailScreen(navController, viewModel, reservaId = id)
        }
    }
}
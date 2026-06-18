package com.hritik.lifetrackertimeline.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hritik.lifetrackertimeline.presentation.auth.AuthViewModel
import com.hritik.lifetrackertimeline.presentation.auth.LoginScreen
import com.hritik.lifetrackertimeline.presentation.auth.SplashScreen
import com.hritik.lifetrackertimeline.presentation.home.AddEditTaskScreen
import com.hritik.lifetrackertimeline.presentation.main.MainScreen
import com.hritik.lifetrackertimeline.presentation.components.InterstitialAdHandler
import com.hritik.lifetrackertimeline.presentation.premium.PremiumScreen

@Composable
fun LifeTrackerNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    
    // Manage interstitial ads globally across the main app screens
    InterstitialAdHandler()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                authViewModel = authViewModel,
                rootNavController = navController
            )
        }

        composable(Screen.Premium.route) {
            PremiumScreen(navController = navController)
        }

        composable(
            route = Screen.AddEditTask.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType },
                navArgument("taskName") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("timeSlot") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("date") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
            val taskName = backStackEntry.arguments?.getString("taskName")
            val timeSlot = backStackEntry.arguments?.getString("timeSlot")
            val date = backStackEntry.arguments?.getString("date")
            AddEditTaskScreen(
                taskId = taskId,
                taskName = taskName,
                timeSlot = timeSlot,
                date = date,
                navController = navController
            )
        }
    }
}

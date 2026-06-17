package com.hritik.lifetrackertimeline.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hritik.lifetrackertimeline.navigation.BottomBarScreen
import com.hritik.lifetrackertimeline.navigation.Screen
import com.hritik.lifetrackertimeline.presentation.auth.AuthViewModel
import com.hritik.lifetrackertimeline.presentation.components.AdBanner
import com.hritik.lifetrackertimeline.presentation.home.AnalyticsScreen
import com.hritik.lifetrackertimeline.presentation.home.CalendarScreen
import com.hritik.lifetrackertimeline.presentation.home.TaskListScreen
import com.hritik.lifetrackertimeline.presentation.home.TimelineScreen
import com.hritik.lifetrackertimeline.presentation.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    rootNavController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isPremium by viewModel.premiumManager.isPremium.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.weight(1f),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("LifeTracker Timeline", style = MaterialTheme.typography.titleLarge) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
            },
            bottomBar = {
                BottomBar(navController = navController)
            },
            floatingActionButton = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                if (currentRoute == BottomBarScreen.Timeline.route || currentRoute == BottomBarScreen.Tasks.route) {
                    FloatingActionButton(
                        onClick = { rootNavController.navigate(Screen.AddEditTask.createRoute(-1)) },
                        containerColor = Color(0xFF0047AB),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomBarScreen.Timeline.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BottomBarScreen.Timeline.route) {
                    TimelineScreen()
                }
                composable(BottomBarScreen.Analytics.route) {
                    AnalyticsScreen()
                }
                composable(BottomBarScreen.Calendar.route) {
                    CalendarScreen()
                }
                composable(BottomBarScreen.Tasks.route) {
                    TaskListScreen(authViewModel, rootNavController)
                }
                composable(BottomBarScreen.Profile.route) {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        userRepository = viewModel.userRepository,
                        premiumManager = viewModel.premiumManager,
                        billingRepository = viewModel.billingRepository
                    )
                }
            }
        }
        
        // Banner Ad below bottom navigation if not premium
        if (!isPremium) {
            AdBanner()
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Timeline,
        BottomBarScreen.Tasks,
        BottomBarScreen.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Color(0xFFF8F9FE),
        tonalElevation = 0.dp
    ) {
        screens.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                label = { Text(text = screen.title) },
                icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF673AB7),
                    selectedTextColor = Color(0xFF673AB7),
                    indicatorColor = Color(0xFFEDE7F6),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

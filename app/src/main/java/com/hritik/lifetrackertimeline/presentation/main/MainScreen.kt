package com.hritik.lifetrackertimeline.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hritik.lifetrackertimeline.navigation.BottomBarScreen
import com.hritik.lifetrackertimeline.navigation.Screen
import com.hritik.lifetrackertimeline.presentation.auth.AuthState
import com.hritik.lifetrackertimeline.presentation.auth.AuthViewModel
import com.hritik.lifetrackertimeline.presentation.components.AdBanner
import com.hritik.lifetrackertimeline.presentation.home.*
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
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            rootNavController.navigate(Screen.Login.route) {
                popUpTo(Screen.Main.route) { inclusive = true }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check rootNavController's current entry for signals from AddEditTask
    val rootBackStackEntry by rootNavController.currentBackStackEntryAsState()
    val taskCreatedByAddEdit = rootBackStackEntry?.savedStateHandle?.get<Boolean>("task_created") ?: false
    
    if (taskCreatedByAddEdit) {
        rootBackStackEntry?.savedStateHandle?.remove<Boolean>("task_created")
        LaunchedEffect(Unit) {
            // If we are on TaskSelection, pop it to go back to Timeline
            if (navController.currentDestination?.route?.startsWith("task_selection") == true) {
                navController.popBackStack()
            }
        }
    }

    // Define top-level destinations where bottom bar should be shown
    val topLevelRoutes = listOf(
        BottomBarScreen.Timeline.route,
        BottomBarScreen.Analytics.route,
        BottomBarScreen.Calendar.route,
        BottomBarScreen.Tasks.route,
        BottomBarScreen.Profile.route
    )

    // Only show bottom bar for top-level routes
    val showBottomBar = currentRoute in topLevelRoutes
    // Show back icon only when not on a top-level screen and we can go back
    val canPop = navController.previousBackStackEntry != null && currentRoute !in topLevelRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("LifeTracker Timeline", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    if (canPop) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            if (showBottomBar) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FE))
                ) {
                    BottomBar(navController = navController)
                    
                    if (!isPremium) {
                        AdBanner()
                    }
                    
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        },
        floatingActionButton = {
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
            modifier = Modifier.padding(if (showBottomBar) innerPadding else PaddingValues(0.dp))
        ) {
            composable(BottomBarScreen.Timeline.route) {
                TimelineScreen(
                    onNavigateToTaskSelection = { timeSlot, date ->
                        navController.navigate(Screen.TaskSelection.createRoute(timeSlot, date))
                    }
                )
            }
            composable(
                route = Screen.TaskSelection.route,
                arguments = listOf(
                    navArgument("timeSlot") { type = NavType.StringType },
                    navArgument("date") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val timeSlot = backStackEntry.arguments?.getString("timeSlot") ?: ""
                val date = backStackEntry.arguments?.getString("date") ?: ""
                TaskSelectionScreen(
                    timeSlot = timeSlot,
                    date = date,
                    onNavigateBack = { navController.popBackStack() },
                    onTaskSelected = { taskId ->
                        navController.popBackStack()
                    },
                    onAddNewTask = { taskName ->
                        rootNavController.navigate(
                            Screen.AddEditTask.createRoute(
                                taskId = -1, 
                                taskName = taskName,
                                timeSlot = timeSlot,
                                date = date
                            )
                        )
                    }
                )
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
                    mainViewModel = viewModel,
                    onNavigateToPremium = { rootNavController.navigate(Screen.Premium.route) }
                )
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Timeline,
//        BottomBarScreen.Tasks,
        BottomBarScreen.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = Modifier.height(72.dp),
        containerColor = Color(0xFFF8F9FE),
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0.dp)
    ) {
        screens.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                label = { Text(text = screen.title, style = MaterialTheme.typography.labelSmall) },
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

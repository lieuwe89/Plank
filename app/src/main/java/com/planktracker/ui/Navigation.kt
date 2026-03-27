package com.planktracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.planktracker.viewmodel.PlankViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Timer : Screen("timer", "Timer", Icons.Default.Home)
}

@Composable
fun PlankNavigation(openTimerOnStart: Boolean = false) {
    val vm: PlankViewModel = viewModel()
    val navController = rememberNavController()

    val bottomNavItems = listOf(Screen.Home, Screen.History, Screen.Settings)

    LaunchedEffect(openTimerOnStart) {
        if (openTimerOnStart) {
            navController.navigate(Screen.Timer.route)
        }
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = currentDestination?.route != Screen.Timer.route

            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(vm = vm, onStartPlank = { navController.navigate(Screen.Timer.route) })
            }
            composable(Screen.Timer.route) {
                TimerScreen(vm = vm, onBack = { navController.popBackStack() })
            }
            composable(Screen.History.route) {
                HistoryScreen(vm = vm)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(vm = vm)
            }
        }
    }
}

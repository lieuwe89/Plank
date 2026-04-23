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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                Column {
                    // Top border for the tab bar
                    HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurface)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(bottom = 8.dp)
                    ) {
                        bottomNavItems.forEach { screen ->
                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            
                            val iconColor = if (selected) MaterialTheme.colorScheme.primary else com.planktracker.ui.theme.PaperInk3
                            val fontWeig = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                // Active indicator line
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(2.dp)
                                        .background(if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Icon(
                                    imageVector = screen.icon, 
                                    contentDescription = screen.label,
                                    tint = iconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = screen.label.uppercase(),
                                    color = iconColor,
                                    fontFamily = com.planktracker.ui.theme.PlusJakartaSans,
                                    fontSize = 9.sp,
                                    fontWeight = fontWeig,
                                    letterSpacing = 0.08.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
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

package com.emotify.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.emotify.ui.components.MiniPlayer
import com.emotify.ui.navigations.Screen

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Screen.BottomScreen.Home,
        Screen.BottomScreen.Search,
        Screen.BottomScreen.Library
    )

    Scaffold(
        bottomBar = {
            Column {
                // 1. Luôn ghim MiniPlayer ở trên Bottom Navigation Bar
                MiniPlayer(onPlayerClick = {
                    rootNavController.navigate(Screen.Player.route)
                })

                // 2. Bottom Navigation Bar
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    mainNavController.navigate(screen.route) {
                                        popUpTo(mainNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // NavHost nội bộ cho 3 màn hình chính
            NavHost(navController = mainNavController, startDestination = Screen.BottomScreen.Home.route) {
                composable(Screen.BottomScreen.Home.route) {
                    // Gọi HomeScreen của bạn ở đây
                    Text(text = "Màn hình Home (Gợi ý theo Mood)")
                }
                composable(Screen.BottomScreen.Search.route) {
                    // Gọi SearchScreen của bạn ở đây
                    Text(text = "Màn hình Tìm kiếm bài hát")
                }
                composable(Screen.BottomScreen.Library.route) {
                    // Gọi LibraryScreen của bạn ở đây
                    Text(text = "Màn hình Thư viện & Playlist")
                }
            }
        }
    }
}
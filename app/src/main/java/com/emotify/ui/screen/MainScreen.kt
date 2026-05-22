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
import com.emotify.ui.screen.library.LibraryScreen
import com.emotify.ui.screen.player.PlayerViewModel
import com.emotify.ui.screen.search.SearchScreen

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    playerViewModel: PlayerViewModel   // Nhận từ NavGraph để dùng chung với Player và MiniPlayer
) {
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
                // MiniPlayer ghim ở trên Bottom Nav — dùng chung playerViewModel
                MiniPlayer(
                    onPlayerClick = {
                        rootNavController.navigate(Screen.Player.route)
                    },
                    playerViewModel = playerViewModel
                )

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
            NavHost(
                navController = mainNavController,
                startDestination = Screen.BottomScreen.Home.route
            ) {
                composable(Screen.BottomScreen.Home.route) {
                    HomeScreen(
                        onSongClick = { song ->
                            // Khi bấm vào bài hát: phát nhạc + mở PlayerScreen
                            // Lấy toàn bộ danh sách bài từ MusicViewModel làm queue
                            playerViewModel.playSong(song)
                            rootNavController.navigate(Screen.Player.route)
                        }
                    )
                }
                composable(Screen.BottomScreen.Search.route) {
                    SearchScreen(
                        onSongClick = { song ->
                            playerViewModel.playSong(song)
                            rootNavController.navigate(Screen.Player.route)
                        },
                        playerViewModel = playerViewModel
                    )
                }
                composable(Screen.BottomScreen.Library.route) {
                    LibraryScreen(playerViewModel = playerViewModel)
                }
            }
        }
    }
}
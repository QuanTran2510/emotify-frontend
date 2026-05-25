package com.emotify.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.emotify.ui.components.MiniPlayer
import com.emotify.ui.navigations.Screen
import com.emotify.ui.screen.home.HomeScreen
import com.emotify.ui.screen.auth.AuthViewModel
import com.emotify.ui.screen.home.MusicViewModel
import com.emotify.ui.screen.library.LibraryScreen
import com.emotify.ui.screen.player.PlayerViewModel
import com.emotify.ui.screen.profile.ProfileScreen
import com.emotify.ui.screen.search.SearchScreen

@Composable
fun MainScreen(
    rootNavController: NavHostController,
    playerViewModel: PlayerViewModel,
    musicViewModel: MusicViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Screen.BottomScreen.Home,
        Screen.BottomScreen.Search,
        Screen.BottomScreen.Library,
        Screen.BottomScreen.Profile
    )

    Scaffold(
        bottomBar = {
            Column {
                MiniPlayer(
                    onPlayerClick = { rootNavController.navigate(Screen.Player.route) },
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
                                        popUpTo(mainNavController.graph.findStartDestination().id) { saveState = true }
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
                        musicViewModel = musicViewModel,
                        onCameraClick = { rootNavController.navigate(Screen.Camera.route) },
                        onSongClick = { song, queue ->
                            playerViewModel.playSong(song, queue)
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
                    LibraryScreen(
                        playerViewModel = playerViewModel,
                        onPlaylistClick = { playlistId ->
                            rootNavController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                        }
                    )
                }
                composable(Screen.BottomScreen.Profile.route) {
                    ProfileScreen(
                        onLogout = {
                            authViewModel.logout(context)
                            rootNavController.navigate(Screen.Auth.route) {
                                popUpTo(Screen.Main.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}

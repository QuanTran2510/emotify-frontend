package com.emotify.ui.navigations

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.emotify.ui.screen.MainScreen
import com.emotify.ui.screen.auth.LoginScreen
import com.emotify.ui.screen.auth.RegisterScreen
import com.emotify.ui.screen.camera.CameraScreen
import com.emotify.ui.screen.home.MusicViewModel
import com.emotify.ui.screen.library.PlaylistDetailScreen
import com.emotify.ui.screen.player.PlayerScreen
import com.emotify.ui.screen.player.PlayerViewModel

@Composable
fun SetupNavGraph(navController: NavHostController) {
    val playerViewModel: PlayerViewModel = viewModel()
    val musicViewModel: MusicViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = if (FirebaseAuth.getInstance().currentUser != null) Screen.Main.route else Screen.Auth.route,
        route = "root_graph"
    ) {
        navigation(startDestination = Screen.Login.route, route = Screen.Auth.route) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(navController = navController)
            }
        }

        composable(Screen.Main.route) {
            MainScreen(
                rootNavController = navController,
                playerViewModel = playerViewModel,
                musicViewModel = musicViewModel
            )
        }

        composable(Screen.Player.route) {
            PlayerScreen(
                onBack = { navController.popBackStack() },
                onOpenCamera = { navController.navigate(Screen.Camera.route) },
                playerViewModel = playerViewModel
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onMoodDetected = { mood ->
                    musicViewModel.setSelectedMood(mood)
                    playerViewModel.addMoodHistory(mood)
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PlaylistDetail.route) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId").orEmpty()
            PlaylistDetailScreen(
                playlistId = playlistId,
                onBack = { navController.popBackStack() },
                playerViewModel = playerViewModel
            )
        }
    }
}

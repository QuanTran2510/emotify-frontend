package com.emotify.ui.navigations

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.emotify.ui.screen.MainScreen
import com.emotify.ui.screen.auth.LoginScreen
import com.emotify.ui.screen.auth.RegisterScreen
import com.emotify.ui.screen.camera.CameraScreen
import com.emotify.ui.screen.player.PlayerScreen
import com.emotify.ui.screen.player.PlayerViewModel

@Composable
fun SetupNavGraph(navController: NavHostController) {
    // PlayerViewModel phải được khởi tạo ở cấp NavGraph này để dùng chung
    // giữa HomeScreen, MiniPlayer, PlayerScreen mà không bị tạo lại
    val playerViewModel: PlayerViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route,
        route = "root_graph"
    ) {
        // 1. LUỒNG AUTH
        navigation(startDestination = Screen.Login.route, route = Screen.Auth.route) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(navController = navController)
            }
        }

        // 2. MÀN HÌNH CHÍNH — truyền playerViewModel xuống để MiniPlayer dùng chung
        composable(Screen.Main.route) {
            MainScreen(
                rootNavController = navController,
                playerViewModel = playerViewModel
            )
        }

        // 3. PLAYER FULL-SCREEN
        composable(Screen.Player.route) {
            PlayerScreen(
                onBack = { navController.popBackStack() },
                playerViewModel = playerViewModel
            )
        }

        // 4. CAMERA QUÉT CẢM XÚC
        composable(Screen.Camera.route) {
            CameraScreen(
                onMoodDetected = { mood ->
                    // Sau khi nhận diện xong, quay về Home và truyền mood filter
                    // TODO: Bước tiếp theo có thể dùng SavedStateHandle hoặc SharedViewModel để lọc
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                    android.util.Log.d("Emotify", "Detected mood from camera: $mood")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 5. PLAYLIST DETAIL (chuẩn bị sẵn cho tính năng playlist)
        composable(Screen.PlaylistDetail.route) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")
            Text(text = "Chi tiết playlist: $playlistId")
        }
    }
}
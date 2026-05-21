package com.emotify.ui.navigations

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.emotify.ui.screen.MainScreen
import androidx.compose.foundation.clickable
import com.emotify.ui.screen.auth.LoginScreen
import com.emotify.ui.screen.auth.RegisterScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route, // Mở app lên vào luồng Auth trước
        route = "root_graph"
    ) {
        // 1. LUỒNG AUTHENTICATION
        navigation(startDestination = Screen.Login.route, route = Screen.Auth.route) {
            composable(Screen.Login.route) {
                // Gọi LoginScreen và truyền hành động chuyển màn hình vào
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true } // Xóa luồng Auth khỏi Backstack để không bấm back quay lại được nữa
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    navController = navController
                )
            }
        }

        // 2. MÀN HÌNH CHÍNH (Chứa BottomBar & MiniPlayer)
        composable(Screen.Main.route) {
            MainScreen(rootNavController = navController)
        }

        // 3. MÀN HÌNH PLAYER CHI TIẾT (Phóng to từ MiniPlayer)
        composable(Screen.Player.route) {
            Text(text = "Màn hình phát nhạc chi tiết Full Screen")
        }

        // 4. MÀN HÌNH CAMERA QUÉT KHUÔN MẶT
        composable(Screen.Camera.route) {
            Text(text = "Màn hình CameraX + ML Kit Quét mặt")
        }

        // 5. MÀN HÌNH CHI TIẾT PLAYLIST
        composable(Screen.PlaylistDetail.route) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")
            Text(text = "Chi tiết playlist có ID: $playlistId")
        }
    }
}
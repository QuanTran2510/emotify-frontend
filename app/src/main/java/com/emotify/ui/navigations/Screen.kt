package com.emotify.ui.navigations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Auth : Screen("auth_graph")
    object Login : Screen("login")
    object Register : Screen("register")

    object Main : Screen("main_screen") // Khung chứa Bottom Bar + MiniPlayer

    // Các màn hình thuộc Bottom Navigation (Cần thêm Title và Icon)
    sealed class BottomScreen(route: String, val title: String, val icon: ImageVector) : Screen(route) {
        object Home : BottomScreen("home", "Home", Icons.Default.Home)
        object Search : BottomScreen("search", "Search", Icons.Default.Search)
        object Library : BottomScreen("library", "Library", Icons.Default.List)
    }

    object Camera : Screen("camera")
    object Player : Screen("player")
    object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        fun createRoute(playlistId: String) = "playlist_detail/$playlistId"
    }
}
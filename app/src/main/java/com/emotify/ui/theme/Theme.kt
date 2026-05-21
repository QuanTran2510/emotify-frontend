package com.emotify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Cấu hình tập hợp màu cho chế độ Tối
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = TextWhite,
    onSurface = TextWhite
)

// Cấu hình tập hợp màu cho chế độ Sáng
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = TextBlack,
    onSurface = TextBlack
)

@Composable
fun EmotifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Tự động lấy cấu hình của điện thoại
    content: @Composable () -> Unit
) {
    // Ép app luôn dùng Dark Mode cho đẹp nếu bạn muốn (bằng cách sửa thành: val colorScheme = DarkColorScheme)
    // Còn hiện tại đang để tự động theo hệ thống:
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
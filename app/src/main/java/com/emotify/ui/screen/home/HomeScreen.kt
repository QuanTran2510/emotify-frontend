package com.emotify.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emotify.data.model.Song // Import data class vừa tạo ở Bước 1

// OBJECT CHỨA DỮ LIỆU NHẠC MOCK ĐỂ TEST GIAO DIỆN
object MockMusicData {
    val dummySongs = listOf(
        Song(
            id = "1",
            title = "Chúng Ta Của Tương Lai",
            artist = "Sơn Tùng M-TP",
            coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500", // Link ảnh tạm thời
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",  // Link nhạc mp3 test ngầm
            mood = "Happy"
        ),
        Song(
            id = "2",
            title = "Dâu Thiên Đường",
            artist = "Tlinh",
            coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            mood = "Chill"
        ),
        Song(
            id = "3",
            title = "Sau Lời Từ Khước",
            artist = "Phan Mạnh Quỳnh",
            coverUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            mood = "Sad"
        )
    )
}
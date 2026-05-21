package com.emotify.data.model

data class Song(
    val id: String,          // ID bài hát để sau này truyền sang màn hình phát nhạc chi tiết
    val title: String,       // Tên bài hát
    val artist: String,      // Tên nghệ sĩ/ca sĩ
    val coverUrl: String,    // Đường dẫn link hình ảnh bọc album (Cover Art)
    val streamUrl: String,   // Đường dẫn link file nhạc mp3 để phát ngầm
    val mood: String         // Tâm trạng của bài hát (Happy, Sad, Chill...) để lọc theo cảm xúc
)
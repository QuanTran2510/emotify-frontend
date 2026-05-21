package com.emotify.data.model

data class Song(
    val songId: String,       // Khớp "songId"
    val title: String,        // Khớp "title" [cite: 121]
    val artist: List<String>, // BẮT BUỘC PHẢI LÀ LIST STRING
    val cover: String,        // Khớp "cover"
    val url: String,          // Khớp "url"
    val duration: Int,        // Khớp kiểu số trong hình DB của bạn (hoặc String tùy API bọc)
    val mood: String          // Khớp "mood"
)
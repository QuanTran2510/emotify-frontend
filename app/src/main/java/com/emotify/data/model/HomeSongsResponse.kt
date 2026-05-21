package com.emotify.data.model

data class HomeSongsResponse(
    val success: Boolean,
val data: HomeMoodData,
val totalSongs: Int
)

data class HomeMoodData(
    val happy: List<Song>,
val sad: List<Song>,
val neutral: List<Song>
)
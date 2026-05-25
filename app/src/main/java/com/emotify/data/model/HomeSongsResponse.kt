package com.emotify.data.model

data class HomeSongsResponse(
    val success: Boolean,
    val data: HomeMoodData,
    val totalSongs: Int
)

data class HomeMoodData(
    val happy: List<Song> = emptyList(),
    val sad: List<Song> = emptyList(),
    val neutral: List<Song> = emptyList()
) {
    fun songsByMood(mood: String?): List<Song> {
        return when (mood?.lowercase()) {
            "happy" -> happy
            "sad" -> sad
            "neutral" -> neutral
            else -> emptyList()
        }
    }

    fun allSongs(): List<Song> = (happy + sad + neutral).distinctBy { it.songId }
}

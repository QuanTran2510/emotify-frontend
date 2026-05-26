package com.emotify.data.model

import com.google.gson.annotations.SerializedName

data class HomeSongsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: HomeMoodData = HomeMoodData(),
    @SerializedName("totalSongs") val totalSongs: Int = 0
)

data class HomeMoodData(
    @SerializedName("happy") val happy: List<Song> = emptyList(),
    @SerializedName("sad") val sad: List<Song> = emptyList(),
    @SerializedName("neutral") val neutral: List<Song> = emptyList()
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

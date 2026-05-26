package com.emotify.data.model

import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("songId") val songId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("artist") val artist: List<String> = emptyList(),
    @SerializedName("cover") val cover: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("mood") val mood: String = "neutral",
    @SerializedName("playCount") val playCount: Int = 0,
    @SerializedName("lastPlayed") val lastPlayed: String? = null,
    @SerializedName("score") val score: Int? = null
)

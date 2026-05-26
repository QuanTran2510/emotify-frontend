package com.emotify.data.model

import com.google.gson.annotations.SerializedName

data class Playlist(
    @SerializedName(value = "playlistId", alternate = ["id"])
    val id: String = "",
    @SerializedName(value = "title", alternate = ["name"])
    val name: String = "",
    @SerializedName("ownerId") val ownerId: String = "",
    @SerializedName("songs") val songs: List<Song> = emptyList(),
    @SerializedName("createdAt") val createdAt: String = ""
)

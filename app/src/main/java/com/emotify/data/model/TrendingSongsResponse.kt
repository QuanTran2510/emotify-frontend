package com.emotify.data.model

import com.google.gson.annotations.SerializedName

data class TrendingSongsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName(value = "trending", alternate = ["songs", "results", "recommended", "playlist"])
    val trending: List<Song> = emptyList()
)

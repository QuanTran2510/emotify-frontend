package com.emotify.data.model

import com.google.gson.annotations.SerializedName

data class TrendingSongsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("trending") val trending: List<Song> // Hứng mảng bài hát hot từ Node.js
)
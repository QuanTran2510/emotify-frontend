package com.emotify.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header
import com.emotify.data.model.TrendingSongsResponse
import retrofit2.http.GET
import retrofit2.http.Query
data class PlaySongRequest(
    val songId: String
)

interface SongApiService {
    @POST("api/songs/play")
    suspend fun increaseSongPlayCount(
        @Header("Authorization") token: String,
        @Body request: PlaySongRequest
    ): Response<Unit>

    @GET("api/songs/trending")
    suspend fun getTrendingSongs(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 5 // Mặc định lấy 10 bài hot nhất
    ): Response<TrendingSongsResponse>
}
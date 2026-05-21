package com.emotify.data.remote.api

import com.emotify.data.model.AuthResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Header
import com.emotify.data.model.Song
import retrofit2.http.GET
import com.emotify.data.model.HomeSongsResponse

interface AuthApiService {
    @POST("api/users/auth")
    suspend fun syncUserWithBackend( // Đổi 'async' thành 'suspend'
        @Header("Authorization") token: String
    ): Response<AuthResponse>

    @GET("api/songs/home")
    suspend fun getHomeSongs(
        @Header("Authorization") token: String
    ): Response<HomeSongsResponse>
}
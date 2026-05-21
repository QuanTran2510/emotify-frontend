package com.emotify.data.remote.api

import com.emotify.data.model.AuthResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Header

interface AuthApiService {
    @POST("api/users/auth")
    suspend fun syncUserWithBackend( // Đổi 'async' thành 'suspend'
        @Header("Authorization") token: String
    ): Response<AuthResponse>
}
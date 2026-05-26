package com.emotify.data.remote.api

import com.emotify.data.model.AuthResponse
import com.emotify.data.model.ProfileResponse
import com.emotify.data.model.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth")
    suspend fun syncUserWithBackend(
        @Header("Authorization") token: String
    ): Response<AuthResponse>

    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    @PATCH("profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<ProfileResponse>
}

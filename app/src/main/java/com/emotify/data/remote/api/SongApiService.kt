package com.emotify.data.remote.api

import com.emotify.data.model.AiSuggestResponse
import com.emotify.data.model.AllSongsResponse
import com.emotify.data.model.HomeSongsResponse
import com.emotify.data.model.NextRecommendedRequest
import com.emotify.data.model.NextRecommendedResponse
import com.emotify.data.model.RecommendedSongsResponse
import com.emotify.data.model.SearchSongsResponse
import com.emotify.data.model.SongDetailResponse
import com.emotify.data.model.TrendingSongsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SongApiService {
    @GET("api/songs/home")
    suspend fun getHomeSongs(
        @Header("Authorization") token: String
    ): Response<HomeSongsResponse>

    @GET("api/songs/ai-suggest")
    suspend fun getAiSuggest(
        @Header("Authorization") token: String,
        @Query("mood") mood: String
    ): Response<AiSuggestResponse>

    @GET("api/songs")
    suspend fun getAllSongs(
        @Header("Authorization") token: String
    ): Response<AllSongsResponse>

    @GET("api/songs/search")
    suspend fun searchSongs(
        @Header("Authorization") token: String,
        @Query("query") query: String
    ): Response<SearchSongsResponse>

    @GET("api/songs/recommended")
    suspend fun getRecommended(
        @Header("Authorization") token: String,
        @Query("mood") mood: String
    ): Response<RecommendedSongsResponse>

    @POST("api/songs/next-recommended")
    suspend fun getNextRecommended(
        @Header("Authorization") token: String,
        @Body request: NextRecommendedRequest
    ): Response<NextRecommendedResponse>

    @GET("api/songs/{songId}")
    suspend fun getSongDetail(
        @Header("Authorization") token: String,
        @Path("songId") songId: String
    ): Response<SongDetailResponse>

    @GET("api/songs/trending")
    suspend fun getTrendingSongs(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 10
    ): Response<TrendingSongsResponse>
}

package com.emotify.data.remote.api

import com.emotify.data.model.CreatePlaylistRequest
import com.emotify.data.model.CreatePlaylistResponse
import com.emotify.data.model.FavoriteRequest
import com.emotify.data.model.FavoritesResponse
import com.emotify.data.model.LibraryResponse
import com.emotify.data.model.MessageResponse
import com.emotify.data.model.PlaylistDetailResponse
import com.emotify.data.model.PlaylistSongRequest
import com.emotify.data.model.PlaylistsResponse
import com.emotify.data.model.RecentlyPlayedRequest
import com.emotify.data.model.RecentlyPlayedResponse
import com.emotify.data.model.RenamePlaylistRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface LibraryApiService {
    @GET("api/users/library")
    suspend fun getLibrary(
        @Header("Authorization") token: String
    ): Response<LibraryResponse>

    @POST("api/users/favorites")
    suspend fun addFavorite(
        @Header("Authorization") token: String,
        @Body request: FavoriteRequest
    ): Response<MessageResponse>

    @HTTP(method = "DELETE", path = "api/users/favorites", hasBody = true)
    suspend fun removeFavorite(
        @Header("Authorization") token: String,
        @Body request: FavoriteRequest
    ): Response<MessageResponse>

    @GET("api/users/favorites")
    suspend fun getFavoriteIds(
        @Header("Authorization") token: String
    ): Response<FavoritesResponse>

    @POST("api/users/recently-played")
    suspend fun addRecentlyPlayed(
        @Header("Authorization") token: String,
        @Body request: RecentlyPlayedRequest
    ): Response<MessageResponse>

    @GET("api/users/recently-played")
    suspend fun getRecentlyPlayed(
        @Header("Authorization") token: String
    ): Response<RecentlyPlayedResponse>

    @POST("api/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") token: String,
        @Body request: CreatePlaylistRequest
    ): Response<CreatePlaylistResponse>

    @GET("api/playlists")
    suspend fun getPlaylists(
        @Header("Authorization") token: String
    ): Response<PlaylistsResponse>

    @GET("api/playlists/{playlistId}")
    suspend fun getPlaylistDetail(
        @Header("Authorization") token: String,
        @Path("playlistId") playlistId: String
    ): Response<PlaylistDetailResponse>

    @PATCH("api/playlists/add-song")
    suspend fun addSongToPlaylist(
        @Header("Authorization") token: String,
        @Body request: PlaylistSongRequest
    ): Response<MessageResponse>

    @PATCH("api/playlists/remove-song")
    suspend fun removeSongFromPlaylist(
        @Header("Authorization") token: String,
        @Body request: PlaylistSongRequest
    ): Response<MessageResponse>

    @PATCH("api/playlists/rename")
    suspend fun renamePlaylist(
        @Header("Authorization") token: String,
        @Body request: RenamePlaylistRequest
    ): Response<MessageResponse>

    @DELETE("api/playlists/{playlistId}")
    suspend fun deletePlaylist(
        @Header("Authorization") token: String,
        @Path("playlistId") playlistId: String
    ): Response<MessageResponse>
}

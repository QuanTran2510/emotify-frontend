package com.emotify.data.model

import com.google.gson.annotations.SerializedName

data class MessageResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = ""
)

data class ProfileResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("user") val user: UserProfile? = null,
    @SerializedName("message") val message: String = ""
)

data class UpdateProfileRequest(
    @SerializedName("displayName") val displayName: String,
    @SerializedName("photoURL") val photoURL: String? = null
)

data class FavoriteRequest(
    @SerializedName("songId") val songId: String
)

data class FavoritesResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("favorites") val favorites: List<String> = emptyList(),
    @SerializedName("message") val message: String = ""
)

data class RecentlyPlayedRequest(
    @SerializedName("songId") val songId: String
)

data class RecentlyPlayedItem(
    @SerializedName("songId") val songId: String = "",
    @SerializedName("playedAt") val playedAt: String = ""
)

data class RecentlyPlayedResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("recentlyPlayed") val recentlyPlayed: List<RecentlyPlayedItem> = emptyList(),
    @SerializedName("message") val message: String = ""
)

data class LibraryResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("library") val library: LibraryData = LibraryData(),
    @SerializedName("message") val message: String = ""
)

data class LibraryData(
    @SerializedName("playlists") val playlists: List<Playlist> = emptyList(),
    @SerializedName("favorites") val favorites: List<Song> = emptyList(),
    @SerializedName("recentlyPlayed") val recentlyPlayed: List<Song> = emptyList(),
    @SerializedName("currentPlaying") val currentPlaying: Song? = null,
    @SerializedName("stats") val stats: LibraryStats = LibraryStats()
)

data class LibraryStats(
    @SerializedName("totalPlaylists") val totalPlaylists: Int = 0,
    @SerializedName("totalFavorites") val totalFavorites: Int = 0
)

data class CreatePlaylistRequest(
    @SerializedName("title") val title: String
)

data class CreatePlaylistResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("playlist") val playlist: Playlist? = null,
    @SerializedName("message") val message: String = ""
)

data class PlaylistsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("playlists") val playlists: List<Playlist> = emptyList(),
    @SerializedName("message") val message: String = ""
)

data class PlaylistDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("playlist") val playlist: Playlist? = null,
    @SerializedName("message") val message: String = ""
)

data class PlaylistSongRequest(
    @SerializedName("playlistId") val playlistId: String,
    @SerializedName("songId") val songId: String
)

data class RenamePlaylistRequest(
    @SerializedName("playlistId") val playlistId: String,
    @SerializedName("newTitle") val newTitle: String
)

data class AiSuggestResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("mood") val mood: String = "",
    @SerializedName("playlist") val playlist: List<Song> = emptyList(),
    @SerializedName("message") val message: String = ""
)

data class AllSongsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("songs") val songs: List<Song> = emptyList(),
    @SerializedName("message") val message: String = ""
)

data class SearchSongsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("results") val results: List<Song> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("message") val message: String = ""
)

data class RecommendedSongsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("mood") val mood: String = "",
    @SerializedName("recommended") val recommended: List<Song> = emptyList(),
    @SerializedName("count") val count: Int = 0,
    @SerializedName("source") val source: String = "",
    @SerializedName("message") val message: String = ""
)

data class NextRecommendedRequest(
    @SerializedName("currentSongId") val currentSongId: String,
    @SerializedName("mood") val mood: String
)

data class NextRecommendedResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("nextSong") val nextSong: Song? = null,
    @SerializedName("mood") val mood: String = "",
    @SerializedName("message") val message: String = ""
)

data class SongDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("song") val song: Song? = null,
    @SerializedName("relatedSongs") val relatedSongs: List<Song> = emptyList(),
    @SerializedName("message") val message: String = ""
)

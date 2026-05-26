package com.emotify.ui.screen.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.emotify.data.model.CreatePlaylistRequest
import com.emotify.data.model.FavoriteRequest
import com.emotify.data.model.Playlist
import com.emotify.data.model.PlaylistSongRequest
import com.emotify.data.model.RecentlyPlayedRequest
import com.emotify.data.model.RenamePlaylistRequest
import com.emotify.data.model.Song
import com.emotify.data.remote.api.FirebaseTokenProvider
import com.emotify.data.remote.api.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Trạng thái UI của trình phát nhạc + thư viện cá nhân lấy từ backend API.
data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleOn: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val favoriteSongs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val recentSongs: List<Song> = emptyList(),
    val moodHistory: List<String> = emptyList(),
    val isLibraryLoading: Boolean = false,
    val message: String? = null
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _uiState = MutableLiveData(PlayerUiState())
    val uiState: LiveData<PlayerUiState> = _uiState

    private val songApiService = RetrofitClient.songApiService
    private val libraryApiService = RetrofitClient.libraryApiService

    private var hasLoggedPlay = false
    private var shuffleOrder: MutableList<Int> = mutableListOf()

    init {
        refreshLibrary()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value?.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _uiState.value = _uiState.value?.copy(
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                    )
                }
                if (playbackState == Player.STATE_ENDED) {
                    skipToNext()
                }
            }
        })

        viewModelScope.launch {
            while (true) {
                val state = _uiState.value
                if (state?.isPlaying == true) {
                    val currentPos = exoPlayer.currentPosition.coerceAtLeast(0L)
                    _uiState.postValue(state.copy(currentPositionMs = currentPos))

                    if (currentPos >= 10000L && !hasLoggedPlay && state.currentSong != null) {
                        hasLoggedPlay = true
                        logRecentlyPlayed(state.currentSong.songId)
                    }
                }
                delay(500)
            }
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            val token = FirebaseTokenProvider.bearerToken() ?: return@launch
            _uiState.value = _uiState.value?.copy(isLibraryLoading = true, message = null)
            try {
                val response = libraryApiService.getLibrary(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    val library = response.body()!!.library
                    _uiState.value = _uiState.value?.copy(
                        playlists = library.playlists,
                        favoriteSongs = library.favorites,
                        recentSongs = library.recentlyPlayed,
                        isLibraryLoading = false,
                        message = null
                    )
                } else {
                    _uiState.value = _uiState.value?.copy(
                        isLibraryLoading = false,
                        message = response.body()?.message ?: "Không tải được thư viện"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(
                    isLibraryLoading = false,
                    message = e.localizedMessage ?: "Lỗi kết nối thư viện"
                )
            }
        }
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        hasLoggedPlay = false

        val cleanedQueue = (if (queue.isEmpty()) listOf(song) else queue).distinctBy { it.songId }
        val index = cleanedQueue.indexOfFirst { it.songId == song.songId }.coerceAtLeast(0)
        val oldQueueIds = _uiState.value?.queue?.map { it.songId } ?: emptyList()
        val newQueueIds = cleanedQueue.map { it.songId }

        if (oldQueueIds != newQueueIds) {
            rebuildShuffleOrder(currentIndex = index, queueSize = cleanedQueue.size)
        } else {
            shuffleOrder.remove(index)
        }

        val newRecentSongs = listOf(song) + ((_uiState.value?.recentSongs ?: emptyList()).filterNot { it.songId == song.songId })

        _uiState.value = _uiState.value?.copy(
            currentSong = song,
            queue = cleanedQueue,
            currentIndex = index,
            isPlaying = true,
            currentPositionMs = 0L,
            recentSongs = newRecentSongs.take(20)
        )

        exoPlayer.setMediaItem(MediaItem.fromUri(song.url))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun skipToNext() {
        val state = _uiState.value ?: return
        if (state.queue.isEmpty()) return

        val nextIndex = if (state.isShuffleOn) {
            nextShuffleIndex(state.currentIndex, state.queue.size)
        } else {
            (state.currentIndex + 1) % state.queue.size
        }

        state.queue.getOrNull(nextIndex)?.let { playSong(it, state.queue) }
    }

    fun skipToPrevious() {
        val state = _uiState.value ?: return
        if (state.queue.isEmpty()) return

        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = if (state.currentIndex > 0) state.currentIndex - 1 else state.queue.lastIndex
        state.queue.getOrNull(prevIndex)?.let { playSong(it, state.queue) }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _uiState.value = _uiState.value?.copy(currentPositionMs = positionMs)
    }

    fun toggleShuffle() {
        val state = _uiState.value ?: return
        val newValue = !state.isShuffleOn
        exoPlayer.shuffleModeEnabled = false
        if (newValue) rebuildShuffleOrder(state.currentIndex, state.queue.size)
        _uiState.value = state.copy(isShuffleOn = newValue)
    }

    fun toggleRepeat() {
        val current = _uiState.value?.repeatMode ?: Player.REPEAT_MODE_OFF
        val next = when (current) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer.repeatMode = next
        _uiState.value = _uiState.value?.copy(repeatMode = next)
    }

    fun isFavorite(songId: String): Boolean {
        return _uiState.value?.favoriteSongs?.any { it.songId == songId } == true
    }

    fun toggleFavorite(song: Song) {
        val state = _uiState.value ?: return
        val exists = state.favoriteSongs.any { it.songId == song.songId }

        val optimisticFavorites = if (exists) {
            state.favoriteSongs.filterNot { it.songId == song.songId }
        } else {
            listOf(song) + state.favoriteSongs
        }
        _uiState.value = state.copy(favoriteSongs = optimisticFavorites, message = null)

        viewModelScope.launch {
            val token = FirebaseTokenProvider.bearerToken() ?: return@launch
            try {
                val response = if (exists) {
                    libraryApiService.removeFavorite(token, FavoriteRequest(song.songId))
                } else {
                    libraryApiService.addFavorite(token, FavoriteRequest(song.songId))
                }
                if (!response.isSuccessful || response.body()?.success != true) {
                    refreshLibrary()
                }
            } catch (_: Exception) {
                refreshLibrary()
            }
        }
    }

    fun createPlaylist(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        viewModelScope.launch {
            val token = FirebaseTokenProvider.bearerToken() ?: return@launch
            try {
                val response = libraryApiService.createPlaylist(token, CreatePlaylistRequest(trimmedName))
                if (response.isSuccessful && response.body()?.success == true) {
                    val playlist = response.body()?.playlist
                    if (playlist != null) {
                        val state = _uiState.value ?: PlayerUiState()
                        _uiState.value = state.copy(playlists = listOf(playlist) + state.playlists)
                    } else refreshLibrary()
                } else {
                    _uiState.value = _uiState.value?.copy(message = response.body()?.message ?: "Không tạo được playlist")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(message = e.localizedMessage ?: "Lỗi tạo playlist")
            }
        }
    }

    fun addSongToPlaylist(song: Song, playlistId: String) {
        viewModelScope.launch {
            val token = FirebaseTokenProvider.bearerToken() ?: return@launch
            try {
                val response = libraryApiService.addSongToPlaylist(
                    token,
                    PlaylistSongRequest(playlistId = playlistId, songId = song.songId)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    loadPlaylistDetail(playlistId)
                } else {
                    _uiState.value = _uiState.value?.copy(message = response.body()?.message ?: "Không thêm được bài hát")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(message = e.localizedMessage ?: "Lỗi thêm bài hát")
            }
        }
    }

    fun removeSongFromPlaylist(songId: String, playlistId: String) {
        viewModelScope.launch {
            val token = FirebaseTokenProvider.bearerToken() ?: return@launch
            try {
                val response = libraryApiService.removeSongFromPlaylist(
                    token,
                    PlaylistSongRequest(playlistId = playlistId, songId = songId)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val state = _uiState.value ?: return@launch
                    val newPlaylists = state.playlists.map { playlist ->
                        if (playlist.id == playlistId) playlist.copy(songs = playlist.songs.filterNot { it.songId == songId }) else playlist
                    }
                    _uiState.value = state.copy(playlists = newPlaylists)
                } else {
                    _uiState.value = _uiState.value?.copy(message = response.body()?.message ?: "Không xoá được bài hát")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(message = e.localizedMessage ?: "Lỗi xoá bài hát")
            }
        }
    }

    fun renamePlaylist(playlistId: String, newTitle: String) {
        val title = newTitle.trim()
        if (title.isBlank()) return
        viewModelScope.launch {
            val token = FirebaseTokenProvider.bearerToken() ?: return@launch
            try {
                val response = libraryApiService.renamePlaylist(token, RenamePlaylistRequest(playlistId, title))
                if (response.isSuccessful && response.body()?.success == true) {
                    val state = _uiState.value ?: return@launch
                    _uiState.value = state.copy(
                        playlists = state.playlists.map { if (it.id == playlistId) it.copy(name = title) else it }
                    )
                } else {
                    _uiState.value = _uiState.value?.copy(message = response.body()?.message ?: "Không đổi tên được playlist")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(message = e.localizedMessage ?: "Lỗi đổi tên playlist")
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            val token = FirebaseTokenProvider.bearerToken() ?: return@launch
            try {
                val response = libraryApiService.deletePlaylist(token, playlistId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val state = _uiState.value ?: return@launch
                    _uiState.value = state.copy(playlists = state.playlists.filterNot { it.id == playlistId })
                } else {
                    _uiState.value = _uiState.value?.copy(message = response.body()?.message ?: "Không xoá được playlist")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(message = e.localizedMessage ?: "Lỗi xoá playlist")
            }
        }
    }

    fun loadPlaylistDetail(playlistId: String) {
        viewModelScope.launch {
            val token = FirebaseTokenProvider.bearerToken() ?: return@launch
            try {
                val response = libraryApiService.getPlaylistDetail(token, playlistId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val detail = response.body()?.playlist ?: return@launch
                    val state = _uiState.value ?: PlayerUiState()
                    val exists = state.playlists.any { it.id == detail.id }
                    val playlists = if (exists) {
                        state.playlists.map { if (it.id == detail.id) detail else it }
                    } else {
                        listOf(detail) + state.playlists
                    }
                    _uiState.value = state.copy(playlists = playlists)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun addMoodHistory(mood: String) {
        val label = when (mood.lowercase()) {
            "happy" -> "😊 Happy"
            "sad" -> "😔 Sad"
            else -> "😐 Neutral"
        }
        val newHistory = listOf("$label • ${System.currentTimeMillis()}") + ((_uiState.value?.moodHistory ?: emptyList()))
        _uiState.value = _uiState.value?.copy(moodHistory = newHistory.take(20))
    }

    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    private fun nextShuffleIndex(currentIndex: Int, queueSize: Int): Int {
        if (queueSize <= 1) return currentIndex
        if (shuffleOrder.isEmpty()) rebuildShuffleOrder(currentIndex, queueSize)
        return shuffleOrder.removeAt(0)
    }

    private fun rebuildShuffleOrder(currentIndex: Int, queueSize: Int) {
        shuffleOrder = (0 until queueSize)
            .filter { it != currentIndex }
            .shuffled()
            .toMutableList()
    }

    private fun logRecentlyPlayed(songId: String) {
        if (songId.isBlank()) return
        viewModelScope.launch {
            val token = FirebaseTokenProvider.bearerToken() ?: return@launch
            try {
                libraryApiService.addRecentlyPlayed(token, RecentlyPlayedRequest(songId = songId))
                refreshLibrary()
            } catch (_: Exception) {
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}

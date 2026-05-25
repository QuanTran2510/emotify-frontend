package com.emotify.ui.screen.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.emotify.data.model.Playlist
import com.emotify.data.model.Song
import com.emotify.data.remote.api.PlaySongRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

// Trạng thái UI của trình phát nhạc + thư viện cá nhân.
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
    val moodHistory: List<String> = emptyList()
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _uiState = MutableLiveData(PlayerUiState())
    val uiState: LiveData<PlayerUiState> = _uiState

    private val songApiService = com.emotify.data.remote.api.RetrofitClient.songApiService
    private val preferences = application.getSharedPreferences("emotify_library", Application.MODE_PRIVATE)
    private val gson = Gson()

    private var hasLoggedPlay = false
    private var shuffleOrder: MutableList<Int> = mutableListOf()

    init {
        restoreLibrary()

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
                        logSongPlay(state.currentSong.songId)
                    }
                }
                delay(500)
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
            recentSongs = newRecentSongs.take(30)
        )
        persistLibrary()

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
        val newFavorites = if (exists) {
            state.favoriteSongs.filterNot { it.songId == song.songId }
        } else {
            listOf(song) + state.favoriteSongs
        }
        _uiState.value = state.copy(favoriteSongs = newFavorites)
        persistLibrary()
    }

    fun createPlaylist(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return
        val state = _uiState.value ?: return
        val playlist = Playlist(id = UUID.randomUUID().toString(), name = trimmedName)
        _uiState.value = state.copy(playlists = listOf(playlist) + state.playlists)
        persistLibrary()
    }

    fun addSongToPlaylist(song: Song, playlistId: String) {
        val state = _uiState.value ?: return
        val newPlaylists = state.playlists.map { playlist ->
            if (playlist.id == playlistId && playlist.songs.none { it.songId == song.songId }) {
                playlist.copy(songs = playlist.songs + song)
            } else playlist
        }
        _uiState.value = state.copy(playlists = newPlaylists)
        persistLibrary()
    }

    fun removeSongFromPlaylist(songId: String, playlistId: String) {
        val state = _uiState.value ?: return
        val newPlaylists = state.playlists.map { playlist ->
            if (playlist.id == playlistId) playlist.copy(songs = playlist.songs.filterNot { it.songId == songId }) else playlist
        }
        _uiState.value = state.copy(playlists = newPlaylists)
        persistLibrary()
    }

    fun deletePlaylist(playlistId: String) {
        val state = _uiState.value ?: return
        _uiState.value = state.copy(playlists = state.playlists.filterNot { it.id == playlistId })
        persistLibrary()
    }

    fun addMoodHistory(mood: String) {
        val label = when (mood.lowercase()) {
            "happy" -> "😊 Happy"
            "sad" -> "😔 Sad"
            else -> "😐 Neutral"
        }
        val newHistory = listOf("$label • ${System.currentTimeMillis()}") + ((_uiState.value?.moodHistory ?: emptyList()))
        _uiState.value = _uiState.value?.copy(moodHistory = newHistory.take(20))
        persistLibrary()
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

    private fun restoreLibrary() {
        val favoriteSongs: List<Song> = readJson("favorites") ?: emptyList()
        val playlists: List<Playlist> = readJson("playlists") ?: emptyList()
        val recentSongs: List<Song> = readJson("recent_songs") ?: emptyList()
        val moodHistory: List<String> = readJson("mood_history") ?: emptyList()

        _uiState.value = _uiState.value?.copy(
            favoriteSongs = favoriteSongs,
            playlists = playlists,
            recentSongs = recentSongs,
            moodHistory = moodHistory
        )
    }

    private fun persistLibrary() {
        val state = _uiState.value ?: return
        preferences.edit()
            .putString("favorites", gson.toJson(state.favoriteSongs))
            .putString("playlists", gson.toJson(state.playlists))
            .putString("recent_songs", gson.toJson(state.recentSongs))
            .putString("mood_history", gson.toJson(state.moodHistory))
            .apply()
    }

    private inline fun <reified T> readJson(key: String): T? {
        val json = preferences.getString(key, null) ?: return null
        return runCatching { gson.fromJson<T>(json, object : TypeToken<T>() {}.type) }.getOrNull()
    }

    private fun logSongPlay(songId: String) {
        if (songId.isBlank()) return
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        currentUser.getIdToken(false).addOnSuccessListener { result ->
            val idToken = result.token
            if (idToken != null) {
                viewModelScope.launch {
                    try {
                        songApiService.increaseSongPlayCount(
                            token = "Bearer $idToken",
                            request = PlaySongRequest(songId = songId)
                        )
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}

package com.emotify.ui.screen.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.emotify.data.model.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Trạng thái UI của màn hình phát nhạc
data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleOn: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF, // OFF, ONE, ALL
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = 0
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    // ExoPlayer instance — dùng AndroidViewModel để tránh leak Context
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _uiState = MutableLiveData(PlayerUiState())
    val uiState: LiveData<PlayerUiState> = _uiState

    init {
        // Theo dõi trạng thái play/pause từ ExoPlayer và cập nhật UI
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
                // Khi bài hát kết thúc và không lặp lại, tự chuyển bài
                if (playbackState == Player.STATE_ENDED) {
                    skipToNext()
                }
            }
        })

        // Cập nhật thanh tiến trình mỗi 500ms
        viewModelScope.launch {
            while (true) {
                val state = _uiState.value
                if (state?.isPlaying == true) {
                    _uiState.postValue(
                        state.copy(currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L))
                    )
                }
                delay(500)
            }
        }
    }

    // Gọi hàm này từ HomeScreen khi user bấm vào một bài hát
    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        val currentQueue = if (queue.isEmpty()) listOf(song) else queue
        val index = currentQueue.indexOfFirst { it.songId == song.songId }.coerceAtLeast(0)

        _uiState.value = _uiState.value?.copy(
            currentSong = song,
            queue = currentQueue,
            currentIndex = index,
            isPlaying = true,
            currentPositionMs = 0L
        )

        val mediaItem = MediaItem.fromUri(song.url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun skipToNext() {
        val state = _uiState.value ?: return
        val nextIndex = if (state.isShuffleOn) {
            (state.queue.indices - state.currentIndex).randomOrNull() ?: return
        } else {
            (state.currentIndex + 1) % state.queue.size
        }

        val nextSong = state.queue.getOrNull(nextIndex) ?: return
        playSong(nextSong, state.queue)
    }

    fun skipToPrevious() {
        val state = _uiState.value ?: return
        // Nếu đang phát quá 3 giây, tua về đầu thay vì chuyển bài
        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0L)
            return
        }
        val prevIndex = if (state.currentIndex > 0) state.currentIndex - 1 else state.queue.size - 1
        val prevSong = state.queue.getOrNull(prevIndex) ?: return
        playSong(prevSong, state.queue)
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _uiState.value = _uiState.value?.copy(currentPositionMs = positionMs)
    }

    fun toggleShuffle() {
        val newValue = !(_uiState.value?.isShuffleOn ?: false)
        exoPlayer.shuffleModeEnabled = newValue
        _uiState.value = _uiState.value?.copy(isShuffleOn = newValue)
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

    // Format milliseconds thành "3:45" để hiển thị trên UI
    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release() // Giải phóng bộ nhớ khi ViewModel bị hủy
    }
}
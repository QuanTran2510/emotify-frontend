package com.emotify.ui.screen.library

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotify.ui.screen.player.PlayerViewModel

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    onBack: () -> Unit,
    playerViewModel: PlayerViewModel = viewModel()
) {
    val state by playerViewModel.uiState.observeAsState()

    LaunchedEffect(playlistId) {
        playerViewModel.loadPlaylistDetail(playlistId)
    }

    val playlist = state?.playlists?.firstOrNull { it.id == playlistId }

    if (playlist == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Không tìm thấy playlist")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) { Text("Quay lại") }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(playlist.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Text("${playlist.songs.size} bài hát", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (playlist.songs.isNotEmpty()) {
            Button(
                onClick = { playerViewModel.playSong(playlist.songs.first(), playlist.songs) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Phát playlist")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (playlist.songs.isEmpty()) {
            EmptyStateMessage(
                icon = Icons.Default.PlayArrow,
                message = "Playlist còn trống",
                subtitle = "Mở một bài hát, bấm nút thêm vào playlist để lưu vào đây"
            )
        } else {
            SongListContent(
                songs = playlist.songs,
                onSongClick = { song -> playerViewModel.playSong(song, playlist.songs) },
                trailingIcon = { song ->
                    IconButton(onClick = { playerViewModel.removeSongFromPlaylist(song.songId, playlist.id) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Xoá khỏi playlist")
                    }
                }
            )
        }
    }
}

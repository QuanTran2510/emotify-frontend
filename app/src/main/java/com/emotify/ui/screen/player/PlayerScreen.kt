package com.emotify.ui.screen.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.emotify.data.model.Playlist

private val BgDark = Color(0xFF121212)
private val SurfaceDark = Color(0xFF1E1E1E)
private val GreenAccent = Color(0xFF38D9C6)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFAAAAAA)

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    onOpenCamera: () -> Unit = {},
    playerViewModel: PlayerViewModel = viewModel()
) {
    val state by playerViewModel.uiState.observeAsState()
    val currentSong = state?.currentSong ?: return

    val isPlaying = state?.isPlaying ?: false
    val currentPositionMs = state?.currentPositionMs ?: 0L
    val durationMs = state?.durationMs ?: 1L
    val isShuffleOn = state?.isShuffleOn ?: false
    val repeatMode = state?.repeatMode ?: Player.REPEAT_MODE_OFF
    val isFavorite = playerViewModel.isFavorite(currentSong.songId)

    var showPlaylistDialog by remember { mutableStateOf(false) }

    val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "player_progress")

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFF2A2A2A), BgDark)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Đóng", tint = TextPrimary, modifier = Modifier.size(28.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Đang phát", fontSize = 11.sp, color = TextSecondary, letterSpacing = 1.sp)
                    Text(
                        text = currentSong.mood.replaceFirstChar { it.uppercase() },
                        fontSize = 13.sp,
                        color = GreenAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = {
                        playerViewModel.getPlaylists()
                        showPlaylistDialog = true
                    }
                ) {
                    Icon(Icons.Default.PlaylistAdd, contentDescription = "Thêm vào playlist", tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
            ) {
                AsyncImage(
                    model = currentSong.cover,
                    contentDescription = "Ảnh bìa ${currentSong.title}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSong.artist.joinToString(", "),
                        fontSize = 15.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { playerViewModel.toggleFavorite(currentSong) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = if (isFavorite) Color(0xFFFF4D67) else TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column {
                Slider(
                    value = animatedProgress.coerceIn(0f, 1f),
                    onValueChange = { fraction -> playerViewModel.seekTo((fraction * durationMs).toLong()) },
                    colors = SliderDefaults.colors(
                        thumbColor = TextPrimary,
                        activeTrackColor = GreenAccent,
                        inactiveTrackColor = Color(0xFF404040)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(playerViewModel.formatDuration(currentPositionMs), color = TextSecondary, fontSize = 12.sp)
                    Text(playerViewModel.formatDuration(durationMs), color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { playerViewModel.toggleShuffle() }) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Ngẫu nhiên", tint = if (isShuffleOn) GreenAccent else TextSecondary, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { playerViewModel.skipToPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Bài trước", tint = TextPrimary, modifier = Modifier.size(32.dp))
                }
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(GreenAccent).clickable { playerViewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = { playerViewModel.skipToNext() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Bài tiếp theo", tint = TextPrimary, modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = { playerViewModel.toggleRepeat() }) {
                    Icon(
                        imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Lặp lại",
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) GreenAccent else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedButton(
                onClick = onOpenCamera,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenAccent)
            ) {
                Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gợi ý nhạc theo cảm xúc của bạn", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = state?.playlists ?: emptyList(),
            onDismiss = { showPlaylistDialog = false },
            onCreatePlaylist = { name -> playerViewModel.createPlaylist(name) },
            onAddToPlaylist = { playlistId ->
                playerViewModel.addSongToPlaylist(currentSong, playlistId)
                showPlaylistDialog = false
            }
        )
    }
}

@Composable
private fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onAddToPlaylist: (String) -> Unit
) {
    var newPlaylistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm vào playlist") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Tên playlist mới") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        onCreatePlaylist(newPlaylistName)
                        newPlaylistName = ""
                    },
                    enabled = newPlaylistName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tạo playlist")
                }
                HorizontalDivider()
                if (playlists.isEmpty()) {
                    Text("Chưa có playlist. Hãy tạo playlist mới trước.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    playlists.forEach { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.name) },
                            supportingContent = { Text("${playlist.songs.size} bài hát") },
                            leadingContent = { Icon(Icons.Default.QueueMusic, contentDescription = null) },
                            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onAddToPlaylist(playlist.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Đóng") } }
    )
}

package com.emotify.ui.screen.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.emotify.data.model.Playlist
import com.emotify.data.model.Song
import com.emotify.ui.screen.player.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LibraryTab(val label: String) {
    PLAYLISTS("Playlist"),
    FAVORITES("Yêu thích"),
    RECENT("Nghe gần đây"),
    MOOD_HISTORY("Cảm xúc")
}

@Composable
fun LibraryScreen(
    playerViewModel: PlayerViewModel = viewModel(),
    onPlaylistClick: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.PLAYLISTS) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val playerState by playerViewModel.uiState.observeAsState()

    LaunchedEffect(Unit) {
        playerViewModel.refreshLibrary()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Thư viện của tôi", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            IconButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tạo playlist")
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.background,
            divider = {}
        ) {
            LibraryTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.label, fontSize = 14.sp, fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal) }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

        when (selectedTab) {
            LibraryTab.PLAYLISTS -> PlaylistContent(
                playlists = playerState?.playlists ?: emptyList(),
                onPlaylistClick = onPlaylistClick,
                onDeletePlaylist = { playerViewModel.deletePlaylist(it) }
            )
            LibraryTab.FAVORITES -> FavoritesContent(
                songs = playerState?.favoriteSongs ?: emptyList(),
                onSongClick = { song -> playerViewModel.playSong(song, playerState?.favoriteSongs ?: emptyList()) },
                onToggleFavorite = { playerViewModel.toggleFavorite(it) }
            )
            LibraryTab.RECENT -> RecentContent(
                songs = playerState?.recentSongs ?: emptyList(),
                onSongClick = { song -> playerViewModel.playSong(song, playerState?.recentSongs ?: emptyList()) }
            )
            LibraryTab.MOOD_HISTORY -> MoodHistoryContent(playerState?.moodHistory ?: emptyList())
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = {
                playerViewModel.createPlaylist(it)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreatePlaylistDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo playlist mới") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên playlist") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = { onCreate(name) }, enabled = name.isNotBlank()) { Text("Tạo") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } }
    )
}

@Composable
fun PlaylistContent(
    playlists: List<Playlist>,
    onPlaylistClick: (String) -> Unit,
    onDeletePlaylist: (String) -> Unit
) {
    if (playlists.isEmpty()) {
        EmptyStateMessage(Icons.Default.QueueMusic, "Chưa có playlist", "Bấm dấu + để tạo playlist riêng của bạn")
    } else {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlists) { playlist ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onPlaylistClick(playlist.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(playlist.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${playlist.songs.size} bài hát", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                        IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Xoá playlist")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesContent(songs: List<Song>, onSongClick: (Song) -> Unit, onToggleFavorite: (Song) -> Unit) {
    if (songs.isEmpty()) {
        EmptyStateMessage(Icons.Default.FavoriteBorder, "Chưa có bài hát yêu thích", "Bấm biểu tượng trái tim khi nghe nhạc để thêm vào đây")
    } else {
        SongListContent(songs = songs, onSongClick = onSongClick, trailingIcon = {
            IconButton(onClick = { onToggleFavorite(it) }) {
                Icon(Icons.Default.Favorite, contentDescription = "Bỏ yêu thích", tint = Color(0xFFFF4D67))
            }
        })
    }
}

@Composable
fun RecentContent(songs: List<Song>, onSongClick: (Song) -> Unit) {
    if (songs.isEmpty()) {
        EmptyStateMessage(Icons.Default.History, "Chưa có lịch sử nghe", "Các bài hát bạn phát sẽ xuất hiện ở đây")
    } else {
        SongListContent(songs = songs, onSongClick = onSongClick)
    }
}

@Composable
fun MoodHistoryContent(history: List<String>) {
    if (history.isEmpty()) {
        EmptyStateMessage(Icons.Default.Face, "Chưa có lịch sử cảm xúc", "Quét khuôn mặt để lưu lại tâm trạng gần đây")
        return
    }

    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
        items(history) { item ->
            val parts = item.split("•")
            val mood = parts.firstOrNull()?.trim().orEmpty()
            val time = parts.getOrNull(1)?.trim()?.toLongOrNull()?.let { formatTime(it) } ?: "Vừa xong"
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(mood.take(2), fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(mood.drop(2).trim(), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
}

@Composable
fun SongListContent(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    trailingIcon: @Composable (Song) -> Unit = {
        Icon(Icons.Default.MoreVert, contentDescription = "Tuỳ chọn", modifier = Modifier.size(20.dp))
    }
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(songs) { song ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onSongClick(song) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.cover,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(song.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist.joinToString(", "), fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                trailingIcon(song)
            }
        }
    }
}

@Composable
fun EmptyStateMessage(icon: ImageVector, message: String, subtitle: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), textAlign = TextAlign.Center)
        }
    }
}

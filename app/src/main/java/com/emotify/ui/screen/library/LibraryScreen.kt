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
import com.emotify.data.model.Song
import com.emotify.ui.screen.player.PlayerViewModel

// ====================== SỬA IMPORT Ở ĐÂY ======================
import com.emotify.ui.screen.home.MusicViewModel     // ← Thay đổi package nếu cần
import com.emotify.ui.screen.home.MusicUiState       // ← Thay đổi package nếu cần
// ============================================================

// Các tab trong thư viện
enum class LibraryTab(val label: String) {
    FAVORITES("Yêu thích"),
    RECENT("Nghe gần đây"),
    MOOD_HISTORY("Lịch sử cảm xúc")
}

@Composable
fun LibraryScreen(
    musicViewModel: MusicViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.FAVORITES) }
    val playerState by playerViewModel.uiState.observeAsState()
    val musicState by musicViewModel.musicState.observeAsState(MusicUiState.Loading)

    // Lấy danh sách bài hát đã phát gần đây từ queue của player
    val recentSongs = playerState?.queue?.take(20) ?: emptyList()

    // Tạm thời giả lập favorites
    val favoriteSongs = recentSongs.take(3)

    Column(modifier = Modifier.fillMaxSize()) {

        // === HEADER ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                "Thư viện của tôi",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        // === TAB ROW ===
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
                    text = {
                        Text(
                            tab.label,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

        // === NỘI DUNG TỪNG TAB ===
        when (selectedTab) {
            LibraryTab.FAVORITES -> FavoritesContent(
                songs = favoriteSongs,
                onSongClick = { song -> playerViewModel.playSong(song, favoriteSongs) }
            )
            LibraryTab.RECENT -> RecentContent(
                songs = recentSongs,
                onSongClick = { song -> playerViewModel.playSong(song, recentSongs) }
            )
            LibraryTab.MOOD_HISTORY -> MoodHistoryContent()
        }
    }
}

// Các hàm còn lại giữ nguyên (chỉ tinh chỉnh nhỏ)
@Composable
fun FavoritesContent(songs: List<Song>, onSongClick: (Song) -> Unit) {
    if (songs.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.FavoriteBorder,
            message = "Chưa có bài hát yêu thích",
            subtitle = "Bấm vào biểu tượng trái tim khi nghe nhạc để thêm vào đây"
        )
    } else {
        SongListContent(songs = songs, onSongClick = onSongClick)
    }
}

@Composable
fun RecentContent(songs: List<Song>, onSongClick: (Song) -> Unit) {
    if (songs.isEmpty()) {
        EmptyStateMessage(
            icon = Icons.Default.History,
            message = "Chưa có lịch sử nghe",
            subtitle = "Các bài hát bạn phát sẽ xuất hiện ở đây"
        )
    } else {
        SongListContent(songs = songs, onSongClick = onSongClick)
    }
}

@Composable
fun MoodHistoryContent() {
    val moodHistory = listOf(
        Triple("😊 Happy", "Hôm nay, 14:30", 8),
        Triple("😔 Sad", "Hôm qua, 20:15", 5),
        Triple("😌 Relaxed", "Hôm qua, 08:00", 12),
        Triple("😐 Neutral", "2 ngày trước, 16:45", 3)
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "Lịch sử nhận diện cảm xúc",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(vertical = 12.dp)
        )
        moodHistory.forEach { (mood, time, songCount) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(mood.split(" ")[0], fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            mood.split(" ")[1],
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "$songCount bài",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SongListContent(songs: List<Song>, onSongClick: (Song) -> Unit) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(songs) { song ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSongClick(song) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.cover,
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        song.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        song.artist.joinToString(", "),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { /* TODO: Xoá khỏi danh sách yêu thích */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Tuỳ chọn", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(
    icon: ImageVector,
    message: String,
    subtitle: String
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
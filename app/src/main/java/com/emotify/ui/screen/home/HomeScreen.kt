package com.emotify.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.emotify.data.model.Song
import com.emotify.ui.screen.home.MusicUiState
import com.emotify.ui.screen.home.MusicViewModel

@Composable
fun HomeScreen(
    onSongClick: (Song) -> Unit = {},
    musicViewModel: MusicViewModel = viewModel() // Inject MusicViewModel vào UI
) {
    // Lắng nghe trạng thái tải nhạc từ Server
    val musicState by musicViewModel.musicState.observeAsState(MusicUiState.Loading)

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = musicState) {
            is MusicUiState.Loading -> {
                // Hiển thị vòng xoay chờ tải 32 bài nhạc
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is MusicUiState.Error -> {
                // Hiển thị giao diện báo lỗi nếu Render bị sập hoặc nghẽn mạng
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { musicViewModel.fetchSongsFromServer() }) {
                        Text("Thử lại")
                    }
                }
            }
            is MusicUiState.Success -> {
                // SỬA TẠI ĐÂY: Lấy trực tiếp các danh sách đã được Server Node.js phân loại sẵn
                val moodData = state.moodData
                val trendingSongs = state.trendingSongs
                val happySongs = moodData.happy
                val sadSongs = moodData.sad
                val neutralSongs = moodData.neutral

                // Tạm thời lấy tối đa 5 bài của Happy và Sad gộp lại làm Trending trong lúc đợi API Trending riêng

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                ) {
                    // Tiêu đề chào mừng
                    item {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "Chào bạn trở lại!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Gợi ý cho tâm trạng của bạn",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // 4 Hàng mục bài hát tự động đồng bộ từ API Backend chính chủ
                    if (trendingSongs.isNotEmpty()) {
                        item { MusicSection("Đang Thịnh Hành", trendingSongs, onSongClick) }
                    }
                    if (happySongs.isNotEmpty()) {
                        item { MusicSection("Nạp Vitamin Tích Cực!", happySongs, onSongClick) }
                    }
                    if (sadSongs.isNotEmpty()) {
                        item { MusicSection("Góc Nhỏ Cho Tâm Trạng", sadSongs, onSongClick) }
                    }
                    if (neutralSongs.isNotEmpty()) {
                        item { MusicSection("Bình Yên Và Tập Trung", neutralSongs, onSongClick) }
                    }
                }
            }
        }
    }
}

@Composable
fun MusicSection(title: String, songs: List<Song>, onSongClick: (Song) -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(songs) { song ->
                SongItem(song = song, onClick = { onSongClick(song) })
            }
        }
    }
}

@Composable
fun SongItem(song: Song, onClick: () -> Unit) {
    Column(modifier = Modifier.width(150.dp).clickable { onClick() }.padding(4.dp)) {
        AsyncImage(
            model = song.cover, // SỬA: Đổi từ song.coverUrl thành song.cover cho khớp data class
            contentDescription = "Cover of ${song.title}",
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // SỬA CHỖ NÀY: Do song.artist hiện tại là List<String>, ta dùng joinToString để gộp tên các ca sĩ lại
        Text(
            text = song.artist.joinToString(", "),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}
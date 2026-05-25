package com.emotify.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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

@Composable
fun HomeScreen(
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    onCameraClick: () -> Unit = {},
    musicViewModel: MusicViewModel = viewModel()
) {
    val musicState by musicViewModel.musicState.observeAsState(MusicUiState.Loading)
    val selectedMood by musicViewModel.selectedMood.observeAsState(null)

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = musicState) {
            is MusicUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            is MusicUiState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
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
                val moodData = state.moodData
                val trendingSongs = state.trendingSongs
                val happySongs = moodData.happy
                val sadSongs = moodData.sad
                val relaxedSongs = moodData.relaxed
                val neutralSongs = moodData.neutral
                val detectedMoodSongs = moodData.songsByMood(selectedMood)

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                ) {
                    item {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "Chào bạn trở lại!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Text(
                                text = if (selectedMood == null) "Gợi ý cho tâm trạng của bạn" else "Nhạc hợp với ${moodLabel(selectedMood)}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onCameraClick,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Quét khuôn mặt để gợi ý nhạc")
                            }
                        }
                    }

                    if (selectedMood != null && detectedMoodSongs.isNotEmpty()) {
                        item {
                            MusicSection(
                                title = "Playlist đề xuất cho ${moodLabel(selectedMood)}",
                                songs = detectedMoodSongs,
                                onSongClick = { song -> onSongClick(song, detectedMoodSongs) }
                            )
                        }
                    }

                    if (trendingSongs.isNotEmpty()) {
                        item { MusicSection("Đang Thịnh Hành", trendingSongs) { song -> onSongClick(song, trendingSongs) } }
                    }
                    if (happySongs.isNotEmpty()) {
                        item { MusicSection("Nạp Vitamin Tích Cực!", happySongs) { song -> onSongClick(song, happySongs) } }
                    }
                    if (sadSongs.isNotEmpty()) {
                        item { MusicSection("Góc Nhỏ Cho Tâm Trạng", sadSongs) { song -> onSongClick(song, sadSongs) } }
                    }
                    if (relaxedSongs.isNotEmpty()) {
                        item { MusicSection("Thư Giãn Nhẹ Nhàng", relaxedSongs) { song -> onSongClick(song, relaxedSongs) } }
                    }
                    if (neutralSongs.isNotEmpty()) {
                        item { MusicSection("Bình Yên Và Tập Trung", neutralSongs) { song -> onSongClick(song, neutralSongs) } }
                    }
                }
            }
        }
    }
}

private fun moodLabel(mood: String?): String {
    return when (mood?.lowercase()) {
        "happy" -> "Happy 😊"
        "sad" -> "Sad 😔"
        "relaxed" -> "Relaxed 😌"
        "neutral" -> "Neutral 😐"
        else -> "tâm trạng hiện tại"
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
            model = song.cover,
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
        Text(
            text = song.artist.joinToString(", "),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

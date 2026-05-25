package com.emotify.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.emotify.ui.screen.player.PlayerViewModel

@Composable
fun MiniPlayer(
    onPlayerClick: () -> Unit,
    playerViewModel: PlayerViewModel = viewModel()  // Nhận chung ViewModel với PlayerScreen
) {
    val state by playerViewModel.uiState.observeAsState()
    val currentSong = state?.currentSong

    // Không hiển thị MiniPlayer nếu chưa có bài nhạc nào đang phát
    if (currentSong == null) return

    val isPlaying = state?.isPlaying ?: false
    val progress = if ((state?.durationMs ?: 0L) > 0L) {
        (state?.currentPositionMs ?: 0L).toFloat() / (state?.durationMs ?: 1L).toFloat()
    } else 0f

    // Animation mượt cho thanh progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "mini_player_progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .clickable { onPlayerClick() }
    ) {
        // Thanh tiến trình mỏng ở trên cùng MiniPlayer (giống Spotify)
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(2.dp),
            color = Color(0xFF1DB954),        // Màu xanh Spotify-like
            trackColor = Color(0xFF333333)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail bài hát với bo góc nhẹ
            AsyncImage(
                model = currentSong.cover,
                contentDescription = "Ảnh bìa ${currentSong.title}",
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF333333)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Tên bài hát và tên ca sĩ
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentSong.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSong.artist.joinToString(", "),
                    color = Color(0xFFAAAAAA),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { playerViewModel.skipToPrevious() }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Bài trước",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Nút Play / Pause
            IconButton(onClick = { playerViewModel.togglePlayPause() }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Dừng" else "Phát",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Nút Next
            IconButton(onClick = { playerViewModel.skipToNext() }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Bài tiếp theo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

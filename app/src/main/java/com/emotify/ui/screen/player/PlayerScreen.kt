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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import coil.compose.AsyncImage

// Màu sắc theme tối cho PlayerScreen (tương tự dark mode của Spotify)
private val BgDark = Color(0xFF121212)
private val SurfaceDark = Color(0xFF1E1E1E)
private val GreenAccent = Color(0xFF1DB954)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFAAAAAA)

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    playerViewModel: PlayerViewModel = viewModel()
) {
    val state by playerViewModel.uiState.observeAsState()
    val currentSong = state?.currentSong ?: return // Nếu không có bài đang phát thì đóng màn hình

    val isPlaying = state?.isPlaying ?: false
    val currentPositionMs = state?.currentPositionMs ?: 0L
    val durationMs = state?.durationMs ?: 1L
    val isShuffleOn = state?.isShuffleOn ?: false
    val repeatMode = state?.repeatMode ?: Player.REPEAT_MODE_OFF

    val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "player_progress")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Gradient mờ phía trên để tạo chiều sâu cho ảnh bìa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2A2A2A), BgDark),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding()
        ) {

            // === HEADER: Nút Back + tiêu đề + nút menu ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
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
                IconButton(onClick = { /* TODO: Mở bottom sheet tuỳ chọn */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Tuỳ chọn", tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === ẢNH BÌA BÀI HÁT ===
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

            // === TÊN BÀI HÁT + TÊN CA SĨ + NÚT THÊM VÀO YÊU THÍCH ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                IconButton(onClick = { /* TODO: Toggle yêu thích + gọi API */ }) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // === THANH TIẾN TRÌNH ===
            Column {
                Slider(
                    value = animatedProgress,
                    onValueChange = { fraction ->
                        playerViewModel.seekTo((fraction * durationMs).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = TextPrimary,
                        activeTrackColor = GreenAccent,
                        inactiveTrackColor = Color(0xFF404040)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = playerViewModel.formatDuration(currentPositionMs),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = playerViewModel.formatDuration(durationMs),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === CÁC NÚT ĐIỀU KHIỂN CHÍNH ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút Shuffle — đổi màu khi bật
                IconButton(onClick = { playerViewModel.toggleShuffle() }) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Ngẫu nhiên",
                        tint = if (isShuffleOn) GreenAccent else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Nút Skip Previous
                IconButton(onClick = { playerViewModel.skipToPrevious() }) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Bài trước",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Nút Play/Pause (to hơn, nền tròn)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(GreenAccent)
                        .clickable { playerViewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Nút Skip Next
                IconButton(onClick = { playerViewModel.skipToNext() }) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Bài tiếp theo",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Nút Repeat — 3 chế độ: OFF, ALL, ONE
                IconButton(onClick = { playerViewModel.toggleRepeat() }) {
                    Icon(
                        imageVector = if (repeatMode == Player.REPEAT_MODE_ONE)
                            Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Lặp lại",
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) GreenAccent else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // === KHU VỰC PHÍA DƯỚI: Nút Camera Scan Cảm Xúc ===
            // Đây là điểm độc đáo của Emotify so với các app nhạc thông thường
            OutlinedButton(
                onClick = { /* TODO: Navigate tới CameraScreen */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenAccent),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(GreenAccent.copy(alpha = 0.5f))
                )
            ) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Gợi ý nhạc theo cảm xúc của bạn",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

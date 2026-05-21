package com.emotify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MiniPlayer(onPlayerClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF222222)) // Màu nền tối cho app nhạc
            .clickable { onPlayerClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ảnh Thumbnail tạm thời
        Box(modifier = Modifier.size(45.dp).background(Color.Gray))

        Spacer(modifier = Modifier.width(12.dp))

        // Tên bài hát & Ca sĩ
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Tên Bài Hát Đang Phát", color = Color.White)
            Text(text = "Ca sĩ", color = Color.Gray)
        }

        // Nút Play nhanh
        IconButton(onClick = { /* Handle Play/Pause */ }) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
        }
    }
}

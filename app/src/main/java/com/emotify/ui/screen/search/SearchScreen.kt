package com.emotify.ui.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.emotify.data.model.Song
import com.emotify.ui.screen.player.PlayerViewModel

// Experimental cho FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.livedata.observeAsState

// Import theo package của bạn
import com.emotify.ui.screen.home.MusicViewModel
import com.emotify.ui.screen.home.MusicUiState

data class MoodChip(
    val label: String,
    val key: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onSongClick: (Song) -> Unit = {},
    musicViewModel: MusicViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeMoodFilter by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val musicState by musicViewModel.musicState.observeAsState(MusicUiState.Loading)

    // ==================== LẤY TẤT CẢ BÀI HÁT ====================
    val allSongs = remember(musicState) {
        if (musicState is MusicUiState.Success) {
            val data = (musicState as MusicUiState.Success).moodData

            buildList {
                addAll(data.happy)
                addAll(data.sad)
                addAll(data.neutral)
            }
                // Sử dụng distinctBy an toàn (nếu không có id thì dùng title + artist)
                .distinctBy { song ->
                    song.songId
                }
        } else emptyList()
    }

    // Logic lọc
    val filteredSongs = remember(searchQuery, activeMoodFilter, allSongs) {
        allSongs.filter { song ->
            val matchesQuery = searchQuery.isBlank() ||
                    song.title.contains(searchQuery, ignoreCase = true) ||
                    song.artist.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesMood = activeMoodFilter == null || song.mood == activeMoodFilter

            matchesQuery && matchesMood
        }
    }

    val moodChips = listOf(
        MoodChip("Vui vẻ", "happy", Icons.Default.SentimentVerySatisfied, Color(0xFFFF6B35)),
        MoodChip("Buồn", "sad", Icons.Default.MoodBad, Color(0xFF4A90D9)),
        MoodChip("Trung lập", "neutral", Icons.Default.SentimentNeutral, Color(0xFF8E44AD))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Tìm kiếm",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Bài hát, ca sĩ...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Xoá")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Lọc theo tâm trạng",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            moodChips.forEach { chip ->
                val isSelected = activeMoodFilter == chip.key
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        activeMoodFilter = if (isSelected) null else chip.key
                    },
                    label = { Text(chip.label, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(chip.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chip.color.copy(alpha = 0.2f),
                        selectedLabelColor = chip.color,
                        selectedLeadingIconColor = chip.color
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (musicState) {
            is MusicUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is MusicUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không tải được dữ liệu", color = MaterialTheme.colorScheme.error)
                }
            }

            else -> {
                if (searchQuery.isBlank() && activeMoodFilter == null) {
                    EmptySearchUI()
                } else if (filteredSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Không tìm thấy bài hát nào",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Text(
                        "${filteredSongs.size} bài hát",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(filteredSongs) { song ->
                            SearchSongRow(
                                song = song,
                                onClick = {
                                    onSongClick(song)
                                    playerViewModel.playSong(song, filteredSongs)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchUI() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Gõ tên bài hát hoặc chọn tâm trạng",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SearchSongRow(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.cover,
            contentDescription = "Ảnh bìa ${song.title}",
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist.joinToString(", "),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val moodColor = when (song.mood) {
            "happy" -> Color(0xFFFF6B35)
            "sad" -> Color(0xFF4A90D9)
            else -> Color(0xFF8E44AD)
        }

        Text(
            text = song.mood.replaceFirstChar { it.uppercase() },
            fontSize = 11.sp,
            color = moodColor,
            modifier = Modifier
                .background(moodColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
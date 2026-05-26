package com.emotify.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val uiState by profileViewModel.uiState.observeAsState(ProfileUiState(isLoading = true))
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val apiUser = uiState.user

    val displayName = apiUser?.displayName?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.displayName?.takeIf { it.isNotBlank() }
        ?: "Người dùng Emotify"
    val email = apiUser?.email?.takeIf { it.isNotBlank() }
        ?: firebaseUser?.email?.takeIf { it.isNotBlank() }
        ?: "Chưa có email"
    val photoUrl = apiUser?.photoURL?.takeIf { it.isNotBlank() } ?: firebaseUser?.photoUrl?.toString()
    val providerName = remember(firebaseUser) {
        firebaseUser?.providerData
            ?.mapNotNull { provider ->
                when (provider.providerId) {
                    "password" -> "Email / Mật khẩu"
                    "google.com" -> "Google"
                    "facebook.com" -> "Facebook"
                    else -> null
                }
            }
            ?.distinct()
            ?.joinToString(", ")
            ?.takeIf { it.isNotBlank() } ?: "Firebase Authentication"
    }

    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Cá nhân",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Cập nhật hồ sơ")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(model = photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = displayName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = email,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        uiState.message?.takeIf { it.isNotBlank() }?.let { message ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(message, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(28.dp))

        ProfileInfoCard(
            title = "Tài khoản",
            value = "Đã đăng nhập qua backend API",
            icon = Icons.Default.VerifiedUser
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileInfoCard(
            title = "Dịch vụ đăng nhập",
            value = providerName,
            icon = Icons.Default.Email
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = displayName,
            currentPhotoUrl = photoUrl.orEmpty(),
            onDismiss = { showEditDialog = false },
            onSave = { name, photo ->
                profileViewModel.updateDisplayName(name, photo.takeIf { it.isNotBlank() })
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentPhotoUrl: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    var photo by remember(currentPhotoUrl) { mutableStateOf(currentPhotoUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cập nhật hồ sơ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên hiển thị") }, singleLine = true)
                OutlinedTextField(value = photo, onValueChange = { photo = it }, label = { Text("Link ảnh đại diện") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, photo) }, enabled = name.isNotBlank()) { Text("Lưu") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Huỷ") } }
    )
}

@Composable
private fun ProfileInfoCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                    fontSize = 13.sp
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

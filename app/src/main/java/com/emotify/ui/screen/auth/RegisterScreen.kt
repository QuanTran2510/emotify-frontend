package com.emotify.ui.screen.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.Scaffold // Đảm bảo đã import Scaffold ở đầu file
import com.emotify.ui.navigations.Screen
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authStateState = authViewModel.authState.observeAsState(AuthState.Idle)
    val authState = authStateState.value
    val context = LocalContext.current

    // Thay thế khối LaunchedEffect(key1 = authStateState.value) trong RegisterScreen.kt:

    LaunchedEffect(key1 = authStateState.value) {
        when (val currentState = authStateState.value) {
            is AuthState.RegisterSuccess -> { // <-- ĐÓN TRẠNG THÁI NÀY
                // 1. Hiện thông báo đăng ký thành công
                Toast.makeText(context, "Đăng ký thành công! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show()

                // 2. Đăng xuất Firebase ngay lập tức để người dùng phải tự nhập lại mật khẩu
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

                // 3. Đá thẳng ra màn hình Login
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Register.route) {
                        inclusive = true
                    }
                }
                authViewModel.resetAuthState()
            }

            is AuthState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthState()
            }
            else -> {}
        }
    }

    // ↓ DÙNG SCAFFOLD ĐỂ ÉP APP CHẠY VÀO VÙNG HIỂN THỊ AN TOÀN, TRÁNH BỊ TAI THỎ CHE KHUẤT
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // innerPadding này giữ khoảng cách an toàn với thanh trạng thái pin/wifi

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Áp dụng khoảng cách an toàn vào đây
                .padding(24.dp),       // Padding thêm cho các thành phần bên trong cách biên
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tạo tài khoản Emotify",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground // Đảm bảo chữ tự đổi màu theo theme sáng/tối
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ô nhập Họ và tên
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ô nhập Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ô nhập Mật khẩu
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Xử lý nút bấm theo trạng thái Loading
            if (authState is AuthState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { authViewModel.registerWithEmail(email, password, name) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Đăng ký")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Đã có tài khoản? Đăng nhập ngay")
                }
            }
        }
    }
}
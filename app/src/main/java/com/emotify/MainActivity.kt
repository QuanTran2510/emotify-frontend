package com.emotify

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.emotify.ui.navigations.SetupNavGraph
import com.emotify.ui.theme.EmotifyTheme
import com.facebook.CallbackManager // ← NHỚ THÊM IMPORT NÀY

class MainActivity : ComponentActivity() {

    // Khai báo callbackManager ở đây để các màn hình khác (như LoginScreen) có thể gọi tới
    companion object {
        val callbackManager: CallbackManager = CallbackManager.Factory.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmotifyTheme {
                val navController = rememberNavController()
                SetupNavGraph(navController = navController)
            }
        }
    }

    // CỰC KỲ QUAN TRỌNG: Hàm này nhận kết quả từ Facebook gửi về và chuyển tiếp vào SDK
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
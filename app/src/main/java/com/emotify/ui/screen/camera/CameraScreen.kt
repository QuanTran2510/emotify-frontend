package com.emotify.ui.screen.camera

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

// Các trạng thái của quá trình quét khuôn mặt
sealed class ScanState {
    object Idle : ScanState()         // Chờ người dùng bấm Quét
    object Scanning : ScanState()     // Đang phân tích khuôn mặt
    object NoFace : ScanState()       // Không tìm thấy khuôn mặt
    data class Result(val mood: String) : ScanState() // Đã nhận diện được cảm xúc
}

// Map từ góc cười (smiling probability) và góc mắt → mood
fun detectMoodFromFace(smilingProb: Float, leftEyeOpen: Float, rightEyeOpen: Float): String {
    val avgEyeOpen = (leftEyeOpen + rightEyeOpen) / 2f
    return when {
        smilingProb > 0.7f -> "happy"
        avgEyeOpen < 0.3f && smilingProb < 0.3f -> "sad"
        smilingProb < 0.25f && avgEyeOpen > 0.65f -> "neutral"
        else -> "neutral"
    }
}

@Composable
fun CameraScreen(
    onMoodDetected: (String) -> Unit,  // Callback để navigate về Home và lọc nhạc theo mood
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var scanState by remember { mutableStateOf<ScanState>(ScanState.Idle) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher xin quyền camera
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        if (!hasCameraPermission) {
            // === CHƯA CÓ QUYỀN CAMERA ===
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Emotify cần quyền truy cập camera", color = Color.White, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Để nhận diện cảm xúc và gợi ý nhạc phù hợp", color = Color(0xFFAAAAAA), fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Cấp quyền Camera")
                }
            }
        } else {
            // === CÓ QUYỀN CAMERA — HIỂN THỊ CAMERA PREVIEW ===
            val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

            // Cấu hình ML Kit Face Detector — bật tính năng phân loại cảm xúc
            val faceDetectorOptions = remember {
                FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // Bật smiling + eye open probability
                    .build()
            }
            val faceDetector = remember { FaceDetection.getClient(faceDetectorOptions) }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        // ImageAnalysis dùng để đưa frame vào ML Kit
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    // Chỉ phân tích khi đang ở trạng thái Scanning
                                    if (scanState is ScanState.Scanning) {
                                        @androidx.camera.core.ExperimentalGetImage
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )
                                            faceDetector.process(image)
                                                .addOnSuccessListener { faces ->
                                                    if (faces.isEmpty()) {
                                                        scanState = ScanState.NoFace
                                                    } else {
                                                        val face = faces[0]
                                                        val smiling = face.smilingProbability ?: 0f
                                                        val leftEye = face.leftEyeOpenProbability ?: 0.5f
                                                        val rightEye = face.rightEyeOpenProbability ?: 0.5f
                                                        val mood = detectMoodFromFace(smiling, leftEye, rightEye)
                                                        scanState = ScanState.Result(mood)
                                                        Log.d("Emotify", "Mood detected: $mood (smile=$smiling)")
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    scanState = ScanState.Idle
                                                    Log.e("Emotify", "Face detection failed", it)
                                                }
                                                .addOnCompleteListener { imageProxy.close() }
                                        } else {
                                            imageProxy.close()
                                        }
                                    } else {
                                        imageProxy.close()
                                    }
                                }
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_FRONT_CAMERA, // Camera trước để selfie
                                preview,
                                imageAnalyzer
                            )
                        } catch (e: Exception) {
                            Log.e("Emotify", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // === OVERLAY UI ĐÈ LÊN CAMERA ===

            // Nút Back
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
            }

            // Hướng dẫn phía trên
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 56.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Đặt khuôn mặt vào khung hình",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Khung hình mục tiêu (oval guide)
            Box(
                modifier = Modifier
                    .size(240.dp, 300.dp)
                    .align(Alignment.Center)
                    .border(
                        width = 2.dp,
                        color = when (scanState) {
                            is ScanState.Result -> Color(0xFF38D9C6)
                            is ScanState.Scanning -> Color(0xFFFFD700)
                            is ScanState.NoFace -> Color(0xFFFF4444)
                            else -> Color.White.copy(alpha = 0.6f)
                        },
                        shape = RoundedCornerShape(50)
                    )
            )

            // Kết quả phát hiện
            AnimatedVisibility(
                visible = scanState is ScanState.Result || scanState is ScanState.NoFace,
                modifier = Modifier.align(Alignment.Center).padding(top = 340.dp),
                enter = fadeIn() + slideInVertically()
            ) {
                when (val state = scanState) {
                    is ScanState.Result -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val (emoji, label, color) = when (state.mood) {
                                "happy" -> Triple("😊", "Vui vẻ", Color(0xFFFF6B35))
                                "sad" -> Triple("😔", "Buồn bã", Color(0xFF4A90D9))
                                else -> Triple("😐", "Trung lập", Color(0xFFAAAAAA))
                            }
                            Text(emoji, fontSize = 48.sp)
                            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    is ScanState.NoFace -> {
                        Text("Không tìm thấy khuôn mặt", color = Color(0xFFFF4444), fontWeight = FontWeight.Medium)
                    }
                    else -> {}
                }
            }

            // === NÚT HÀNH ĐỘNG PHÍA DƯỚI ===
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 40.dp, start = 32.dp, end = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (scanState is ScanState.Result) {
                    // Bấm để áp dụng mood đã nhận diện
                    val detectedMood = (scanState as ScanState.Result).mood
                    Button(
                        onClick = { onMoodDetected(detectedMood) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38D9C6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gợi ý nhạc theo cảm xúc này", color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { scanState = ScanState.Idle }) {
                        Text("Thử lại", color = Color.White)
                    }
                } else {
                    // Nút quét tròn to
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                            .background(
                                if (scanState is ScanState.Scanning) Color(0xFFFFD700)
                                else Color.White.copy(alpha = 0.15f)
                            )
                            .clickable {
                                if (scanState !is ScanState.Scanning) {
                                    scanState = ScanState.Scanning
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (scanState is ScanState.Scanning) {
                            CircularProgressIndicator(color = Color.Black, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
                        } else {
                            Icon(Icons.Default.Face, contentDescription = "Quét cảm xúc", tint = Color.White, modifier = Modifier.size(36.dp))
                        }
                    }
                    Text(
                        text = if (scanState is ScanState.Scanning) "Đang phân tích..." else "Bấm để quét",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
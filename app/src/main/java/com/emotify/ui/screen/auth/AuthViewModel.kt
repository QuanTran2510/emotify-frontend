package com.emotify.ui.screen.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emotify.data.model.UserProfile
import com.emotify.data.remote.api.RetrofitClient
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import java.util.Collections
import com.facebook.login.LoginBehavior
import com.emotify.ui.screen.auth.AuthState

class AuthViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authApiService = RetrofitClient.authApiService

    // UI State quản lý trạng thái hiển thị của màn hình Login
    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState


    // --- 1. LOCAL AUTH: LOGIN VỚI EMAIL VÀ PASSWORD ---
    fun loginWithEmail(email: String, pass: String) {
        _authState.value = AuthState.Loading
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                // Khi họ gõ tài khoản vừa đăng ký để đăng nhập -> Tiến hành sync với backend tạo Firestore luôn!
                syncWithBackend()
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.localizedMessage ?: "Đăng nhập thất bại")
            }
    }

    // --- 2. LOCAL AUTH: ĐĂNG KÝ EMAIL VÀ PASSWORD ---
    // --- 5. ĐĂNG KÝ TÀI KHOẢN BẰNG EMAIL ---
    fun registerWithEmail(email: String, pass: String, name: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _authState.value = AuthState.Error("Vui lòng điền đầy đủ thông tin")
            return
        }

        _authState.value = AuthState.Loading

        firebaseAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                    displayName = name
                }

                authResult.user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener {
                        // Bỏ lệnh sync ngầm ở đây, gán thẳng trạng thái đăng ký thành công!
                        // Thay thế dòng lỗi đỏ bằng dòng dài đầy đủ này:
                        _authState.postValue(com.emotify.ui.screen.auth.AuthState.RegisterSuccess)
                    }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.localizedMessage ?: "Đăng ký thất bại")
            }
    }

    // --- 3. OAUTH2: ĐĂNG NHẬP GOOGLE (Credential Manager) ---
    fun loginWithGoogle(context: Context) {
        _authState.value = AuthState.Loading
        val credentialManager = CredentialManager.create(context)

        // Web Client ID lấy từ file google-services.json hoặc Firebase Console
        val webClientId = "114594916435-cbjotg19bgs2rk15fgkq0s8p011mdf75.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                    firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener { syncWithBackend() }
                        .addOnFailureListener { _authState.value = AuthState.Error("Lỗi kết nối Firebase với Google") }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Huỷ đăng nhập Google")
            }
        }
    }

    // --- 4. OAUTH2: ĐĂNG NHẬP FACEBOOK ---
    fun loginWithFacebook(context: Context, callbackManager: CallbackManager) {
        // Check an toàn để lấy đúng Activity từ Jetpack Compose, tránh bị crash app
        val activity = when (context) {
            is android.app.Activity -> context
            is android.content.ContextWrapper -> context.baseContext as? android.app.Activity
            else -> null
        }

        if (activity == null) {
            _authState.value = AuthState.Error("Không thể xác định Android Activity")
            return
        }

        _authState.value = AuthState.Loading

        // 1. Đăng ký nhận kết quả trước khi gọi lệnh mở màn hình đăng nhập
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val token = result.accessToken.token
                val credential = FacebookAuthProvider.getCredential(token)
                firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener { syncWithBackend() }
                    .addOnFailureListener { _authState.value = AuthState.Error("Lỗi kết nối Firebase với Facebook") }
            }

            override fun onCancel() {
                _authState.value = AuthState.Idle
            }

            override fun onError(error: FacebookException) {
                _authState.value = AuthState.Error(error.localizedMessage ?: "Lỗi đăng nhập Facebook")
            }
        })

        // 2. ↓ THÊM DÒNG NÀY: Ép SDK chạy trên luồng Web, cấm gọi sang App Facebook gốc gây lỗi
        LoginManager.getInstance().setLoginBehavior(LoginBehavior.WEB_ONLY)

        // 3. Gọi lệnh mở màn hình đăng nhập (Truyền biến activity an toàn đã check ở trên vào)
        LoginManager.getInstance().logInWithReadPermissions(
            activity,
            listOf("public_profile")
        )
    }

    // --- 5. ĐỒNG BỘ TOKEN LÊN NODE.JS BACKEND ---
    private fun syncWithBackend() {
        _authState.value = AuthState.Loading
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.Error("Không tìm thấy User trên Firebase")
            return
        }

        // Ép Firebase cấp IdToken hiện tại
        currentUser.getIdToken(true).addOnSuccessListener { result ->
            val idToken = result.token
            if (idToken != null) {
                viewModelScope.launch {
                    try {
                        // Gửi token dạng Bearer lên api/users/auth của bạn
                        val response = authApiService.syncUserWithBackend("Bearer $idToken")
                        if (response.isSuccessful && response.body()?.success == true) {
                            val userProfile = response.body()!!.user
                            if (userProfile != null) {
                                _authState.value = AuthState.Success(userProfile)
                            } else {
                                _authState.value = AuthState.Error("Dữ liệu người dùng trống")
                            }
                        } else {
                            _authState.value = AuthState.Error(response.body()?.message ?: "Backend từ chối Token")
                        }
                    } catch (e: Exception) {
                        _authState.value = AuthState.Error("Không kết nối được server Render: ${e.localizedMessage}")
                    }
                }
            }
        }.addOnFailureListener {
            _authState.value = AuthState.Error("Lấy token Firebase thất bại")
        }
    }

    // Đăng xuất giải phóng tài khoản
    fun logout(context: Context) {
        firebaseAuth.signOut()
        LoginManager.getInstance().logOut()
        val credentialManager = CredentialManager.create(context)
        viewModelScope.launch {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
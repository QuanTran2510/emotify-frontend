package com.emotify.ui.screen.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emotify.data.model.UpdateProfileRequest
import com.emotify.data.model.UserProfile
import com.emotify.data.remote.api.FirebaseTokenProvider
import com.emotify.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserProfile? = null,
    val message: String? = null
)

class ProfileViewModel : ViewModel() {
    private val authApiService = RetrofitClient.authApiService

    private val _uiState = MutableLiveData(ProfileUiState(isLoading = true))
    val uiState: LiveData<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val token = FirebaseTokenProvider.bearerToken() ?: run {
                _uiState.value = ProfileUiState(message = "Chưa đăng nhập")
                return@launch
            }

            _uiState.value = _uiState.value?.copy(isLoading = true, message = null)
            try {
                val response = authApiService.getProfile(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = ProfileUiState(user = response.body()?.user, isLoading = false)
                } else {
                    _uiState.value = ProfileUiState(isLoading = false, message = response.body()?.message ?: "Không tải được hồ sơ")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState(isLoading = false, message = e.localizedMessage ?: "Lỗi kết nối hồ sơ")
            }
        }
    }

    fun updateDisplayName(displayName: String, photoURL: String?) {
        val name = displayName.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            val token = FirebaseTokenProvider.bearerToken() ?: return@launch
            _uiState.value = _uiState.value?.copy(isLoading = true, message = null)
            try {
                val response = authApiService.updateProfile(token, UpdateProfileRequest(name, photoURL))
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = ProfileUiState(user = response.body()?.user, isLoading = false, message = "Đã cập nhật hồ sơ")
                } else {
                    _uiState.value = _uiState.value?.copy(isLoading = false, message = response.body()?.message ?: "Không cập nhật được hồ sơ")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(isLoading = false, message = e.localizedMessage ?: "Lỗi cập nhật hồ sơ")
            }
        }
    }
}

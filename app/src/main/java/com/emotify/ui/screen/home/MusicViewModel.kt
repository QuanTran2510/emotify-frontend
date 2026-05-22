package com.emotify.ui.screen.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emotify.data.model.HomeMoodData
import com.google.firebase.auth.FirebaseAuth
import com.emotify.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch

sealed class MusicUiState {
    object Loading : MusicUiState()
    data class Success(val moodData: HomeMoodData) : MusicUiState() // Trả về cục data phân loại sẵn
    data class Error(val message: String) : MusicUiState()
}

class MusicViewModel : ViewModel() {
    private val authApiService = RetrofitClient.authApiService
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _musicState = MutableLiveData<MusicUiState>(MusicUiState.Loading)
    val musicState: LiveData<MusicUiState> = _musicState

    init {
        fetchSongsFromServer()
    }

    fun fetchSongsFromServer() {
        _musicState.value = MusicUiState.Loading
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            _musicState.value = MusicUiState.Error("Vui lòng đăng nhập để tải nhạc")
            return
        }

        // Lấy Token Firebase để gắn vào Header API
        currentUser.getIdToken(false).addOnSuccessListener { result ->
            val idToken = result.token
            if (idToken != null) {
                viewModelScope.launch {
                    try {
                        val response = authApiService.getHomeSongs("Bearer $idToken")
                        if (response.isSuccessful && response.body()?.success == true) {
                            // Thành công: Đẩy thẳng khối data chứa happy, sad, neutral sang UI [cite: 111, 114]
                            _musicState.postValue(MusicUiState.Success(response.body()!!.data))
                        } else {
                            _musicState.postValue(MusicUiState.Error("Server từ chối cấp dữ liệu nhạc"))
                        }
                    } catch (e: Exception) {
                        _musicState.postValue(MusicUiState.Error("Lỗi kết nối Render: ${e.localizedMessage}"))
                    }
                }
            }
        }.addOnFailureListener {
            _musicState.value = MusicUiState.Error("Lấy token xác thực thất bại")
        }
    }
}
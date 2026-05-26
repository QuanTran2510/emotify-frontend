package com.emotify.ui.screen.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emotify.data.model.HomeMoodData
import com.google.firebase.auth.FirebaseAuth
import com.emotify.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch
import com.emotify.data.model.Song
import com.emotify.data.remote.api.RetrofitClient.songApiService
import kotlinx.coroutines.async

sealed class MusicUiState {
    object Loading : MusicUiState()
    data class Success(
        val moodData: HomeMoodData,
        val trendingSongs: List<Song>
    ) : MusicUiState()
    data class Error(val message: String) : MusicUiState()
}

class MusicViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _musicState = MutableLiveData<MusicUiState>(MusicUiState.Loading)
    val musicState: LiveData<MusicUiState> = _musicState

    private val _selectedMood = MutableLiveData<String?>(null)
    val selectedMood: LiveData<String?> = _selectedMood

    fun setSelectedMood(mood: String?) {
        val normalizedMood = mood?.lowercase()
        _selectedMood.value = normalizedMood
        if (!normalizedMood.isNullOrBlank()) {
            fetchRecommendedByMood(normalizedMood)
        }
    }

    init {
        fetchSongsFromServer()
    }

    private fun fetchRecommendedByMood(mood: String) {
        val current = _musicState.value
        if (current !is MusicUiState.Success) return
        val currentUser = firebaseAuth.currentUser ?: return

        currentUser.getIdToken(false).addOnSuccessListener { result ->
            val idToken = result.token ?: return@addOnSuccessListener
            viewModelScope.launch {
                try {
                    val response = songApiService.getRecommended("Bearer $idToken", mood)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val recommended = response.body()?.recommended ?: emptyList()
                        if (recommended.isNotEmpty()) {
                            val oldData = current.moodData
                            val newData = when (mood) {
                                "happy" -> oldData.copy(happy = recommended)
                                "sad" -> oldData.copy(sad = recommended)
                                "neutral" -> oldData.copy(neutral = recommended)
                                else -> oldData
                            }
                            _musicState.postValue(current.copy(moodData = newData))
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    fun fetchSongsFromServer() {
        _musicState.value = MusicUiState.Loading
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            _musicState.value = MusicUiState.Error("Vui lòng đăng nhập để tải nhạc")
            return
        }

        currentUser.getIdToken(false).addOnSuccessListener { result ->
            val idToken = result.token
            if (idToken != null) {
                viewModelScope.launch {
                    try {
                        val tokenBearer = "Bearer $idToken"

                        // Chạy song song 2 API cùng lúc bằng async để tối ưu hóa tốc độ load app ⚡
                        val homeDataDeferred = viewModelScope.async { songApiService.getHomeSongs(tokenBearer) }
                        val trendingDataDeferred = viewModelScope.async { songApiService.getTrendingSongs(tokenBearer) }

                        val homeResponse = homeDataDeferred.await()
                        val trendingResponse = runCatching { trendingDataDeferred.await() }.getOrNull()

                        if (homeResponse.isSuccessful && homeResponse.body()?.success == true) {
                            _musicState.postValue(
                                MusicUiState.Success(
                                    moodData = homeResponse.body()!!.data,
                                    trendingSongs = trendingResponse?.body()?.trending ?: emptyList()
                                )
                            )
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
package com.emotify.ui.screen.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emotify.data.model.Song
import com.emotify.data.remote.api.FirebaseTokenProvider
import com.emotify.data.remote.api.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SearchUiState(
    val isLoading: Boolean = false,
    val results: List<Song> = emptyList(),
    val message: String? = null
)

class SearchViewModel : ViewModel() {
    private val songApiService = RetrofitClient.songApiService
    private val _uiState = MutableLiveData(SearchUiState())
    val uiState: LiveData<SearchUiState> = _uiState

    private var searchJob: Job? = null

    fun search(query: String) {
        val keyword = query.trim()
        searchJob?.cancel()
        if (keyword.isBlank()) {
            _uiState.value = SearchUiState()
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            val token = FirebaseTokenProvider.bearerToken() ?: return@launch
            _uiState.value = _uiState.value?.copy(isLoading = true, message = null)
            try {
                val response = songApiService.searchSongs(token, keyword)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = SearchUiState(results = response.body()?.results ?: emptyList())
                } else {
                    _uiState.value = SearchUiState(message = response.body()?.message ?: "Không tìm thấy bài hát")
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState(message = e.localizedMessage ?: "Lỗi tìm kiếm")
            }
        }
    }
}

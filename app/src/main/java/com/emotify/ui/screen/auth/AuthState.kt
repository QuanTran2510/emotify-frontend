package com.emotify.ui.screen.auth

import com.emotify.data.model.UserProfile

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object RegisterSuccess : AuthState()
    data class Success(val user: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}
package com.emotify.data.remote.api

import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

object FirebaseTokenProvider {
    suspend fun bearerToken(forceRefresh: Boolean = false): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        val token = suspendCancellableCoroutine<String?> { continuation ->
            user.getIdToken(forceRefresh)
                .addOnSuccessListener { result -> continuation.resume(result.token) }
                .addOnFailureListener { error -> continuation.resumeWithException(error) }
        }
        return token?.let { "Bearer $it" }
    }
}

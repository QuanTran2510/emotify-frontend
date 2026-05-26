package com.emotify.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("user") val user: UserProfile? = null
)

data class UserProfile(
    @SerializedName("uid") val uid: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("photoURL") val photoURL: String? = null,
    @SerializedName("favorites") val favorites: List<Any> = emptyList(),
    @SerializedName("recentlyPlayed") val recentlyPlayed: List<Any> = emptyList(),
    @SerializedName("preferredMoods") val preferredMoods: List<String> = emptyList(),
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("lastLogin") val lastLogin: String = ""
)

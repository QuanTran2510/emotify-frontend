package com.emotify.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: UserProfile?
)

data class UserProfile(
    @SerializedName("uid") val uid: String,
    @SerializedName("email") val email: String,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("photoURL") val photoURL: String?,
    @SerializedName("favorites") val favorites: List<String> = emptyList(),
    @SerializedName("recentlyPlayed") val recentlyPlayed: List<String> = emptyList(),
    @SerializedName("preferredMoods") val preferredMoods: List<String> = emptyList(),
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("lastLogin") val lastLogin: String
)


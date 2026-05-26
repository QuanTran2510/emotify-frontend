package com.emotify.data.remote.api

import com.emotify.data.model.Playlist
import com.emotify.data.model.Song
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Backend online mặc định
    // Nếu chạy backend local cùng Wi-Fi, đổi thành dạng http://192.168.x.x:PORT/
    private const val BASE_URL = "https://emotify-backend-kf11.onrender.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(logging)
        .build()

    private val gson = GsonBuilder()
        .registerTypeAdapter(Song::class.java, SongDeserializer())
        .registerTypeAdapter(Playlist::class.java, PlaylistDeserializer())
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val songApiService: SongApiService by lazy { retrofit.create(SongApiService::class.java) }
    val libraryApiService: LibraryApiService by lazy { retrofit.create(LibraryApiService::class.java) }
}

private class SongDeserializer : JsonDeserializer<Song> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Song {
        val obj = json.asJsonObject
        val artistElement = obj.get("artist")
        val artists = when {
            artistElement == null || artistElement.isJsonNull -> emptyList()
            artistElement.isJsonArray -> artistElement.asJsonArray.mapNotNull { it.safeString() }.filter { it.isNotBlank() }
            else -> listOfNotNull(artistElement.safeString()).filter { it.isNotBlank() }
        }

        return Song(
            songId = obj.string("songId"),
            title = obj.string("title"),
            artist = artists,
            cover = obj.string("cover"),
            url = obj.string("url"),
            duration = obj.intFlexible("duration"),
            mood = obj.string("mood").ifBlank { "neutral" }.lowercase(),
            playCount = obj.intFlexible("playCount"),
            lastPlayed = obj.stringOrNull("lastPlayed"),
            score = obj.intFlexibleOrNull("score")
        )
    }
}

private class PlaylistDeserializer : JsonDeserializer<Playlist> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Playlist {
        val obj = json.asJsonObject
        val songs = mutableListOf<Song>()
        val songsElement = obj.get("songs")
        if (songsElement is JsonArray) {
            songsElement.forEach { item ->
                if (item != null && item.isJsonObject) {
                    songs.add(context.deserialize(item, Song::class.java))
                }
            }
        }

        return Playlist(
            id = obj.string("playlistId").ifBlank { obj.string("id") },
            name = obj.string("title").ifBlank { obj.string("name") },
            ownerId = obj.string("ownerId"),
            songs = songs,
            createdAt = obj.string("createdAt")
        )
    }
}

private fun JsonObject.string(key: String): String = get(key)?.safeString().orEmpty()
private fun JsonObject.stringOrNull(key: String): String? = get(key)?.safeString()?.takeIf { it.isNotBlank() }

private fun JsonObject.intFlexible(key: String): Int = intFlexibleOrNull(key) ?: 0

private fun JsonObject.intFlexibleOrNull(key: String): Int? {
    val element = get(key) ?: return null
    return runCatching {
        when {
            element.isJsonNull -> null
            element.asJsonPrimitive.isNumber -> element.asInt
            else -> element.asString.toDoubleOrNull()?.toInt()
        }
    }.getOrNull()
}

private fun JsonElement.safeString(): String? {
    return runCatching {
        if (isJsonNull) null else asString
    }.getOrNull()
}

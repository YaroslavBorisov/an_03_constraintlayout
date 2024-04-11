package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import ru.netology.nmedia.dto.Post
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class PostRepositoryImpl : PostRepository {
    private companion object {
        const val BASE_URL = "http://10.0.2.2:9999/"
        val jsonType = "application/json".toMediaType()

    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val type : Type = object : TypeToken<List<Post>>(){}.type

    override fun getAll(): List<Post> {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .build()

        val call = client.newCall(request)
        val response = call.execute()

        val bodyText = requireNotNull(response.body).string()
        if(!response.isSuccessful) error ("Response code: ${response.code}")

        return gson.fromJson(bodyText, type)
    }

    override fun save(post: Post) : Post {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .post(gson.toJson(post, Post::class.java).toRequestBody(jsonType))
            .build()

        val call = client.newCall(request)
        val response = call.execute()

        val bodyText = requireNotNull(response.body).string()
        if(!response.isSuccessful) error ("Response code: ${response.code}")

        return gson.fromJson(bodyText, Post::class.java)
    }

    override fun likeById(id: Long, likedByMe: Boolean): Post {
        val request = if (!likedByMe) {
            Request.Builder()
                .url("${BASE_URL}api/slow/posts/${id}/likes")
                .post(EMPTY_REQUEST)
                .build()
        } else {
            Request.Builder()
                .url("${BASE_URL}api/slow/posts/${id}/likes")
                .delete()
                .build()
        }

        val call = client.newCall(request)
        val response = call.execute()

        val bodyText = requireNotNull(response.body).string()
        if(!response.isSuccessful) error ("Response code: ${response.code}")

        return gson.fromJson(bodyText, Post::class.java)
    }

    override fun shareById(id: Long) = Unit

    override fun removeById(id: Long){
        val request = Request.Builder()
                .url("${BASE_URL}api/slow/posts/${id}")
                .delete()
                .build()

        val call = client.newCall(request)
        val response = call.execute()

        if(!response.isSuccessful) error ("Response code: ${response.code}")

    }

    override fun saveDraft(content: String) = Unit

    override fun getDraft() = ""

    override fun deleteDraft() = Unit

}
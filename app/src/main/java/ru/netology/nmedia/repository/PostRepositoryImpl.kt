package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import ru.netology.nmedia.dto.Post
import java.io.IOException
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

    private val type: Type = object : TypeToken<List<Post>>() {}.type


    override fun save(post: Post, callback: PostRepository.Callback<Post>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .post(gson.toJson(post, Post::class.java).toRequestBody(jsonType))
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {

                    val bodyText = requireNotNull(response.body).string()

                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(bodyText))
                        return
                    }

                    try {
                        callback.onSuccess(gson.fromJson(bodyText, Post::class.java))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

            }
            )
    }

    override fun getAll(callback: PostRepository.Callback<List<Post>>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {

                    val bodyText = requireNotNull(response.body).string()

                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(bodyText))
                        return
                    }

                    try {
                        callback.onSuccess(gson.fromJson(bodyText, type))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

            }
            )

    }

    override fun likeById(id: Long, likedByMe: Boolean, callback: PostRepository.Callback<Post>) {
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

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {

                    val bodyText = requireNotNull(response.body).string()

                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(bodyText))
                        return
                    }

                    try {
                        callback.onSuccess(gson.fromJson(bodyText, Post::class.java))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

            }
            )

    }

    override fun shareById(id: Long) = Unit

    override fun removeById(id: Long, callback: PostRepository.Callback<Unit>) {
        val request = Request.Builder()
            .url("${BASE_URL}api/slow/posts/${id}")
            .delete()
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {

                    val bodyText = requireNotNull(response.body).string()

                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(bodyText))
                        return
                    }

                    callback.onSuccess(Unit)

                }

            }
            )

    }

    override fun saveDraft(content: String) = Unit

    override fun getDraft() = ""

    override fun deleteDraft() = Unit

}
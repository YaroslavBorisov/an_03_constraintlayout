package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.Post

class PostRepositoryImpl : PostRepository {

    override fun save(post: Post, callback: PostRepository.Callback<Post>) {
        return ApiService.service
            .save(post)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.errorBody()?.string()))
                        return
                    }

                    val body = response.body() ?: throw RuntimeException("body is null")
                    callback.onSuccess(body)
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }

            })
    }

    override fun getAll(callback: PostRepository.Callback<List<Post>>) {
        return ApiService.service
            .getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.errorBody()?.string()))
                        return
                    }

                    val body = response.body() ?: throw RuntimeException("body is null")
                    callback.onSuccess(body)
                }

                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    callback.onError(Exception(t))
                }

            })

    }

    override fun likeById(id: Long, likedByMe: Boolean, callback: PostRepository.Callback<Post>) {
        return ApiService.service
            .run {
                if (likedByMe) {
                    dislikeById(id)
                } else {
                    likeById(id)
                }
                    .enqueue(object : Callback<Post> {
                        override fun onResponse(call: Call<Post>, response: Response<Post>) {
                            if (!response.isSuccessful) {
                                callback.onError(RuntimeException(response.errorBody()?.string()))
                                return
                            }

                            val body = response.body() ?: throw RuntimeException("body is null")
                            callback.onSuccess(body)
                        }

                        override fun onFailure(call: Call<Post>, t: Throwable) {
                            callback.onError(Exception(t))
                        }

                    })


            }
    }

    override fun shareById(id: Long) = Unit

    override fun removeById(id: Long, callback: PostRepository.Callback<Unit>) {

        return ApiService.service
            .deleteById(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.errorBody()?.string()))
                        return
                    }

                    callback.onSuccess(Unit)

                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
    }

    override fun saveDraft(content: String) = Unit

    override fun getDraft() = ""

    override fun deleteDraft() = Unit

}
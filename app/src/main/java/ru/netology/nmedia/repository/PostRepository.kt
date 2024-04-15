package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import java.lang.Exception

interface PostRepository {
    fun getAll(callback: Callback<List<Post>>)
    fun likeById(id: Long, likedByMe: Boolean, callback:  Callback<Post>)
    fun shareById(id: Long)
    fun removeById(id: Long, callback: Callback<Unit>)
    fun save(post: Post, callback: Callback<Post>)
    fun saveDraft(content: String)
    fun getDraft(): String
    fun deleteDraft()

    interface Callback <T> {
        fun onSuccess(result: T)
        fun onError(exception: Exception)
    }
}
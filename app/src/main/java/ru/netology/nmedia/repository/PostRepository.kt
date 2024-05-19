package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import java.lang.Exception

interface PostRepository {
    suspend fun getAll(): List<Post>
    suspend fun likeById(id: Long, likedByMe: Boolean): Post
    suspend fun shareById(id: Long)
    suspend fun removeById(id: Long): Unit
    suspend fun save(post: Post): Post
    suspend fun saveDraft(content: String)
    suspend fun getDraft(): String
    suspend fun deleteDraft()

}
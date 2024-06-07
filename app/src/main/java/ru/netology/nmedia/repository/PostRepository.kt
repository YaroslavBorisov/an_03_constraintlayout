package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

interface PostRepository {
    val data: Flow<List<Post>>

    suspend fun getAll()
    suspend fun likeById(id: Long, likedByMe: Boolean)
    suspend fun shareById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun saveDraft(content: String)
    suspend fun getDraft()
    suspend fun deleteDraft()

    fun getNewerCount(newerId: Long): Flow<Int>

}
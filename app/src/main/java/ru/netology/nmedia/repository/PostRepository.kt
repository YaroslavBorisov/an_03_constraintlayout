package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    //val data: Flow<List<Post>>
    val data: Flow<PagingData<Post>>

    suspend fun getAll()
    suspend fun likeById(id: Long, likedByMe: Boolean)
    suspend fun shareById(id: Long)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, file: File)
    suspend fun saveDraft(content: String)
    suspend fun getDraft()
    suspend fun deleteDraft()

    fun getNewerCount(newerId: Long): Flow<Int>
    suspend fun showHiddenPosts()

}
package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException
import java.io.IOException
import java.lang.IllegalArgumentException

class PostRepositoryImpl(private val dao: PostDao): PostRepository {

    override val data: LiveData<List<Post>>
        //get() = dao.getAll().map { it.toDto()}
        get() = dao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = ApiService.service.getAll()
            if(!response.isSuccessful) throw ApiError(response.code())

            val posts = response.body() ?: throw UnknownException
            dao.insert(posts.toEntity())
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: ApiError) {
            throw e
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean){
        return ApiService.service.run {
                if (likedByMe) {
                    dislikeById(id)
                } else {
                    likeById(id)
                }
            }
    }

    override suspend fun removeById(id: Long) {
        ApiService.service.deleteById(id)
    }

    override suspend fun save(post: Post) {
        ApiService.service.save(post)
    }

    override suspend fun shareById(id: Long) = Unit

    override suspend fun saveDraft(content: String) = Unit

    override suspend fun getDraft() = Unit

    override suspend fun deleteDraft() = Unit

}
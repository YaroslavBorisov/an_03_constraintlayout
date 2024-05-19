package ru.netology.nmedia.repository

import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.Post

class PostRepositoryImpl : PostRepository {

    override suspend fun shareById(id: Long) = Unit

    override suspend fun saveDraft(content: String) = Unit

    override suspend fun getDraft() = ""

    override suspend fun deleteDraft() = Unit
    override suspend fun getAll() = ApiService.service.getAll()

    override suspend fun likeById(id: Long, likedByMe: Boolean): Post {
        return ApiService.service.run {
                if (likedByMe) {
                    dislikeById(id)
                } else {
                    likeById(id)
                }
            }
    }

    override suspend fun removeById(id: Long) = ApiService.service.deleteById(id)

    override suspend fun save(post: Post) = ApiService.service.save(post)

}
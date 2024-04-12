package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long, likedByMe: Boolean): Post
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post) : Post
    fun saveDraft(content: String)
    fun getDraft(): String
    fun deleteDraft()
}
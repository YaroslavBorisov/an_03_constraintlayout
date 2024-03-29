package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val videoUrl: String? = "",
    val draft: Boolean = false
) {
    fun toDto() = Post(id, author, content, published, likes, likedByMe, shares, views, videoUrl)

    companion object {
        fun fromDto(post: Post) = PostEntity(
            post.id,
            post.author,
            post.content,
            post.published,
            post.likes,
            post.likedByMe,
            post.shares,
            post.views,
            post.videoUrl
        )
    }
}

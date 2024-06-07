package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val videoUrl: String? = "",
    val draft: Boolean = false,
    val visible: Boolean = true
) {
    fun toDto() = Post(id, author, authorAvatar, content, published, likes, likedByMe, shares, views, videoUrl)

    companion object {
        fun fromDto(post: Post, visible:Boolean = true) = PostEntity(
            post.id,
            post.author,
            post.authorAvatar,
            post.content,
            post.published,
            post.likes,
            post.likedByMe,
            post.shares,
            post.views,
            post.videoUrl,
            false,
            visible
        )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(visible:Boolean = true): List<PostEntity> = map { PostEntity.fromDto(it, visible) }
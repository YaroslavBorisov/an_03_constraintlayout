package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enumeration.AttachmentType

@Entity
class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorId: Long,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val videoUrl: String? = "",
    val draft: Boolean = false,
    val visible: Boolean = true,
    @Embedded
    var attachment: AttachmentEmbeddable?,
) {
    fun toDto() = Post(id, author, authorId, authorAvatar, content, published, likes, likedByMe, shares, views, videoUrl, attachment?.toDto())

    companion object {
        fun fromDto(post: Post, visible:Boolean = true) = PostEntity(
            post.id,
            post.author,
            post.authorId,
            post.authorAvatar,
            post.content,
            post.published,
            post.likes,
            post.likedByMe,
            post.shares,
            post.views,
            post.videoUrl,
            false,
            visible,
            AttachmentEmbeddable.fromDto(post.attachment)
        )
    }
}

data class AttachmentEmbeddable(var url: String, var type: AttachmentType) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(visible:Boolean = true): List<PostEntity> = map { PostEntity.fromDto(it, visible) }
package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE NOT draft ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert
    fun insert(post: PostEntity)

    @Query("UPDATE PostEntity SET content=:text WHERE id=:id")
    fun changeContentById(id: Long, text: String)
    fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else changeContentById(post.id, post.content)

    @Query(
        "UPDATE PostEntity SET\n" +
                "likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,\n" +
                "likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END\n" +
                "WHERE id = :id"
    )
    fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id=:id")
    fun removeById(id: Long)

    @Query("UPDATE PostEntity SET shares = shares + 1 WHERE id = :id")
    fun shareById(id: Long)

    class Draft(val id:Long, val content: String)

    fun saveDraft(content: String) =
        getDraftId()?.let { changeContentById(it, content) } ?: insertDraft(content)

    fun insertDraft(content: String) =
        insert(PostEntity(0, "Me", content = content, "Now", draft = true))

    fun getDraft() = getDraftEntity().getOrNull(0)?.content ?: ""

    @Query("DELETE FROM PostEntity WHERE draft")
    fun deleteDraft()

    private fun getDraftId() = getDraftEntity().getOrNull(0)?.id

    @Query("SELECT id, content FROM PostEntity WHERE draft")
    fun getDraftEntity(): List<Draft>
}
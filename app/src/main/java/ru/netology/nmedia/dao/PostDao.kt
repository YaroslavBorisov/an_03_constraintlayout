package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE (NOT draft) AND visible ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query(
        "UPDATE PostEntity SET\n" +
                "likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,\n" +
                "likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END\n" +
                "WHERE id = :id"
    )
    suspend fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id=:id")
    suspend fun removeById(id: Long)

    @Query("UPDATE PostEntity SET shares = shares + 1 WHERE id = :id")
    suspend fun shareById(id: Long)

    @Query("UPDATE PostEntity SET visible = 1 WHERE NOT visible")
    suspend fun showAll()

    @Query("SELECT COUNT(*) FROM PostEntity WHERE NOT visible")
    suspend fun countHidden(): Int

    class Draft(val id:Long, val content: String)

    suspend fun saveDraft(content: String) = Unit
    //    getDraftId()?.let { changeContentById(it, content) } ?: insertDraft(content)

    suspend fun insertDraft(content: String) =
        insert(PostEntity(0, "Me", "", content = content, "Now", draft = true, attachment = null))

    suspend fun getDraft() = getDraftEntity().getOrNull(0)?.content ?: ""

    @Query("DELETE FROM PostEntity WHERE draft")
    suspend fun deleteDraft()

    private suspend fun getDraftId() = getDraftEntity().getOrNull(0)?.id

    @Query("SELECT id, content FROM PostEntity WHERE draft")
    suspend fun getDraftEntity(): List<Draft>
}
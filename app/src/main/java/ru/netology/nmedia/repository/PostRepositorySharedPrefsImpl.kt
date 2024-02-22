package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositorySharedPrefsImpl(context: Context) : PostRepository {
    private var prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "posts"
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type

    private var nextId: Long
    private var posts = emptyList<Post>()
        set(value) {
            field = value
            data.value = posts
            sync()
        }
    private val data = MutableLiveData(posts)

    init {
        prefs.getString(key, null)?.let {
            posts = gson.fromJson(it, type)
        }
        nextId = posts.maxOfOrNull { it.id }?.inc() ?: 1
    }

    override fun getAll(): LiveData<List<Post>> = data
    override fun likeById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                likedByMe = !it.likedByMe,
                likes = it.likes + if (it.likedByMe) -1 else 1
            )
        }
    }

    override fun shareById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                shares = it.shares + 1
            )
        }
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(post.copy(id = nextId++, published = "Now", author = "Netology")) + posts
        } else {
            posts.map {
                if (it.id != post.id) it else it.copy(content = post.content)
            }
        }
    }

    private fun sync() {
        prefs.edit().apply {
            putString(key, gson.toJson(posts))
            apply()
        }
    }
}
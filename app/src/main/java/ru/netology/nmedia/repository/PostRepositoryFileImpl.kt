package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositoryFileImpl(private val context: Context) : PostRepository {

    private val gson = Gson()
    private val filename = "posts.json"
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
        if(context.filesDir.resolve(filename).exists()){
            context.openFileInput(filename).bufferedReader().use {
                posts = gson.fromJson(it, type)
            }
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
        context.openFileOutput(filename, Context.MODE_PRIVATE).bufferedWriter().use {
                it.write(gson.toJson(posts))
        }
    }
}
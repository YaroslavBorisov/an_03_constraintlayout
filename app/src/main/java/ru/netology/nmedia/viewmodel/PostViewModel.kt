package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import java.lang.Exception
import kotlin.concurrent.thread

val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    likedByMe = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()

    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val edited = MutableLiveData(empty)

    init {
        load()
    }

    fun load() {
        thread {
            _data.postValue(FeedModel(loading = true))

            try {
                val posts = repository.getAll()
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (e: Exception) {
                FeedModel(error = true)
            }.also(_data::postValue)

        }
    }

    fun likeById(id: Long, likedByMe: Boolean) {
        thread {
            val oldPosts = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = oldPosts.map {
                    if (it.id == id) it.copy(likedByMe = !it.likedByMe,
                        likes = it.likes + if (it.likedByMe) -1 else 1)
                        else it
                })
            )
            try{
                val post = repository.likeById(id, likedByMe)
                _data.postValue(
                    _data.value?.copy(posts = oldPosts.map {
                        if (it.id == id) post else it
                    })
                )
            } catch (e:IOException) {
                _data.postValue(_data.value?.copy(posts = oldPosts, error = true))
            }

        }
    }
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) {
        thread {
            val oldPosts = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = oldPosts.filter { it.id != id })
            )
            try{
                repository.removeById(id)
            } catch (e:IOException) {
                _data.postValue(_data.value?.copy(posts = oldPosts, error = true))
            }
        }
    }

    fun updateSave(content: String) {
        edited.value?.let {
            if (content != it.content) {
                thread {
                    repository.save(it.copy(content = content))
                    _postCreated.postValue(Unit)
                }
            }
            edited.value = empty
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun clearEditedPost(){
        edited.value = empty
    }

    fun saveDraft(content: String) = repository.saveDraft(content)

    fun getDraft() = repository.getDraft()

    fun deleteDraft() = repository.deleteDraft()

}
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
import java.lang.Exception

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
        _data.value = FeedModel(loading = true)

        repository.getAll(
            object : PostRepository.Callback<List<Post>> {
                override fun onSuccess(result: List<Post>) {
                    _data.postValue(FeedModel(posts = result, empty = result.isEmpty()))
                }

                override fun onError(exception: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            }
        )
    }

    fun likeById(id: Long, likedByMe: Boolean) {
        val oldPosts = _data.value?.posts.orEmpty()
        _data.value =
            _data.value?.copy(posts = oldPosts.map {
                if (it.id == id) it.copy(
                    likedByMe = !it.likedByMe,
                    likes = it.likes + if (it.likedByMe) -1 else 1
                )
                else it
            })


        repository.likeById(id, likedByMe, object : PostRepository.Callback<Post> {
            override fun onSuccess(result: Post) {
                _data.postValue(
                    _data.value?.copy(posts = oldPosts.map {
                        if (it.id == id) result else it
                    })
                )
            }

            override fun onError(exception: Exception) {
                _data.postValue(FeedModel(error = true))
            }

        })
    }

    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) {
        val oldPosts = _data.value?.posts.orEmpty()
        _data.value =
            _data.value?.copy(posts = oldPosts.filter { it.id != id })

        repository.removeById(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(result: Unit) {
            }

            override fun onError(exception: Exception) {
                _data.postValue(_data.value?.copy(posts = oldPosts, error = true))
            }
        })
    }

    fun updateSave(content: String) {
        edited.value?.let {
            if (content != it.content) {
                repository.save(
                    it.copy(content = content),
                    object : PostRepository.Callback<Post> {
                        override fun onSuccess(result: Post) {
                            _data.postValue(
                                _data.value?.copy(posts = listOf(result) + _data.value?.posts.orEmpty())
                            )
                        }

                        override fun onError(exception: Exception) {
                            _data.postValue(FeedModel(error = true))
                        }
                    })
                _postCreated.postValue(Unit)
            }
            edited.value = empty
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun clearEditedPost() {
        edited.value = empty
    }

    fun saveDraft(content: String) = repository.saveDraft(content)

    fun getDraft() = repository.getDraft()

    fun deleteDraft() = repository.deleteDraft()

}
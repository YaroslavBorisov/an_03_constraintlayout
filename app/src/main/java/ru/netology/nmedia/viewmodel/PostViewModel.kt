package ru.netology.nmedia.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

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

    fun load() = viewModelScope.launch {
        _data.value = FeedModel(loading = true)
        val posts = repository.getAll()
        _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
    }

    fun likeById(id: Long, likedByMe: Boolean) = viewModelScope.launch {
//        val oldPosts = _data.value?.posts.orEmpty()
//        _data.value =
//            _data.value?.copy(posts = oldPosts.map {
//                if (it.id == id) it.copy(
//                    likedByMe = !it.likedByMe,
//                    likes = it.likes + if (it.likedByMe) -1 else 1
//                )
//                else it
//            })


        repository.likeById(id, likedByMe)
    }

    private fun showToast() {
        Toast.makeText(getApplication<Application>().applicationContext,
            R.string.error_updating, Toast.LENGTH_LONG).show()
    }

    fun shareById(id: Long) = viewModelScope.launch {
        repository.shareById(id)
    }
    fun removeById(id: Long) = viewModelScope.launch {
//        val oldPosts = _data.value?.posts.orEmpty()
//        _data.value =
//            _data.value?.copy(posts = oldPosts.filter { it.id != id })

        repository.removeById(id)
    }

    fun updateSave(content: String) = viewModelScope.launch {
        edited.value?.let {
            if (content != it.content) {
                repository.save(
                    it.copy(content = content))
                _postCreated.value = Unit
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

    fun saveDraft(content: String) = viewModelScope.launch {
        repository.saveDraft(content)
    }

    fun getDraft() =  viewModelScope.launch {
        repository.getDraft()
    }

    fun deleteDraft() =  viewModelScope.launch {
        repository.deleteDraft()
    }

}
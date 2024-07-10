package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File

val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    likedByMe = false,
    authorId = 0
)

private val nophoto = PhotoModel()

class PostViewModel(
    private val repository: PostRepository,
    appAuth: AppAuth,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _data = appAuth.state.flatMapLatest { auth ->
        repository.data
            .map { posts ->
                FeedModel(posts.map {
                    it.copy(
                        ownedByMe = it.authorId == auth?.id
                    )

                })
            }
    }
        .asLiveData(Dispatchers.Default)

    val data: LiveData<FeedModel>
        get() = _data

    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _photo = MutableLiveData(nophoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    val newerCount: LiveData<Int> = data.switchMap {
        val newerId = it.posts.firstOrNull()?.id ?: 0L

        repository.getNewerCount(newerId)
            .asLiveData(Dispatchers.Default)
    }


    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val edited = MutableLiveData(empty)

    init {
        load()
    }

    fun load(refresh: Boolean = false) = viewModelScope.launch {
        try {
            if (refresh) {
                _dataState.value = FeedModelState(refreshing = true)
            } else {
                _dataState.value = FeedModelState(loading = true)
            }
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            if (e is AppError) {
                when (e) {
                    is ApiError -> {}
                    NetworkException -> {}
                    UnknownException -> {}
                }
            }
        }
    }

    fun refreshPosts() = viewModelScope.launch { load(true) }

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


        try {
            repository.likeById(id, likedByMe)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

//    private fun showToast() {
//        Toast.makeText(getApplication<Application>().applicationContext,
//            R.string.error_updating, Toast.LENGTH_LONG).show()
//    }

    fun shareById(id: Long) = viewModelScope.launch {
        repository.shareById(id)
    }

    fun removeById(id: Long) = viewModelScope.launch {
//        val oldPosts = _data.value?.posts.orEmpty()
//        _data.value =
//            _data.value?.copy(posts = oldPosts.filter { it.id != id })
        try {
            repository.removeById(id)
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun updateSave(content: String) = viewModelScope.launch {
//        edited.value?.let {
//            if (content != it.content) {
//                repository.save(
//                    it.copy(content = content))
//                _postCreated.value = Unit
//            }
//            edited.value = empty
//        }
        edited.value?.let {
            _postCreated.value = Unit
            try {
                when (_photo.value) {
                    nophoto -> repository.save(it.copy(content = content))
                    else -> _photo.value?.file?.let { file ->
                        repository.saveWithAttachment(it.copy(content = content), file)
                    }
                }
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
            edited.value = empty
            _photo.value = nophoto
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

    fun getDraft() = viewModelScope.launch {
        repository.getDraft()
    }

    fun deleteDraft() = viewModelScope.launch {
        repository.deleteDraft()
    }

    fun showHiddenPosts() = viewModelScope.launch {
        repository.showHiddenPosts()
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun dropPhoto() {
        _photo.value = nophoto
    }

}
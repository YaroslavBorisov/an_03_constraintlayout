package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException
import java.io.IOException
import java.util.Collections

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    private val pendingRequests = Collections.synchronizedList(mutableListOf<PendingRequest>())
    private val executedRequests = Collections.synchronizedList(mutableListOf<PendingRequest>())

    override val data: LiveData<List<Post>>
        //get() = dao.getAll().map { it.toDto()}
        get() = dao.getAll().map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            executePendingRequests()

            val response = ApiService.service.getAll()
            if (!response.isSuccessful) throw ApiError(response.code())

            val posts = response.body() ?: throw UnknownException
            dao.insert(posts.toEntity())
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: ApiError) {
            throw e
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    private suspend fun executePendingRequests() {
        clearExecutedRequests()
        withContext(Dispatchers.IO) {
            pendingRequests.map { request ->
                try {
                    when (request.type) {
                        PendingRequestType.LIKE -> {
                            //Log.d("Myapp", "LIKE ${request.id}")
                            async {
                                ApiService.service.likeById(request.id)
                                synchronized(executedRequests) {
                                    executedRequests.add(request)
                                }
                            }
                        }

                        PendingRequestType.DISLIKE -> {
                            async {
                                ApiService.service.dislikeById(request.id)
                                synchronized(executedRequests) {
                                    executedRequests.add(request)
                                }
                            }
                        }

                        PendingRequestType.DELETE -> {
                            async {
                                ApiService.service.deleteById(request.id)
                                synchronized(executedRequests) {
                                    executedRequests.add(request)
                                }
                            }
                        }
                    }

                } catch (e: Exception) {
                    throw e
                }

            }.awaitAll()
        }
        clearExecutedRequests()
    }

    private fun clearExecutedRequests() {
        synchronized(pendingRequests) {
            synchronized(executedRequests){
                pendingRequests.removeAll(executedRequests)
                executedRequests.clear()
            }
        }
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean) {
        try {
            dao.likeById(id)

            synchronized(pendingRequests) {
                if (likedByMe) {
                    pendingRequests.add(PendingRequest(PendingRequestType.DISLIKE, id))
                } else {
                    pendingRequests.add(PendingRequest(PendingRequestType.LIKE, id))
                }
            }
            executePendingRequests()
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }

//            return ApiService.service.run {
//                if (likedByMe) {
//                    dislikeById(id)
//                } else {
//                    likeById(id)
//                }
//            }
    }

    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            synchronized(pendingRequests) {
                pendingRequests.add(PendingRequest(PendingRequestType.DELETE, id))
            }
            executePendingRequests()
            //ApiService.service.deleteById(id)
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun save(post: Post) {
        ApiService.service.save(post)
    }

    override suspend fun shareById(id: Long) = Unit

    override suspend fun saveDraft(content: String) = Unit

    override suspend fun getDraft() = Unit

    override suspend fun deleteDraft() = Unit

}

enum class PendingRequestType { DELETE, LIKE, DISLIKE }

data class PendingRequest(val type: PendingRequestType, val id: Long)


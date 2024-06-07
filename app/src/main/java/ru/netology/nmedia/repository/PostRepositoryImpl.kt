package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.api.PostApi
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
import kotlin.time.Duration.Companion.seconds

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    private val pendingRequests = Collections.synchronizedList(mutableListOf<PendingRequest>())
    private val executedRequests = Collections.synchronizedList(mutableListOf<PendingRequest>())

    override val data: Flow<List<Post>>
        //get() = dao.getAll().map { it.toDto()}
        get() = dao.getAll()
            .map(List<PostEntity>::toDto)
            .flowOn(Dispatchers.Default)

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

        coroutineScope {
            synchronized(pendingRequests) {
                pendingRequests.filter { !it.sent }.map { request ->
                    request.sent = true
                    launch {
                        try {
                            when (request.type) {
                                PendingRequestType.LIKE -> {
                                    ApiService.service.likeById(request.id)
                                    synchronized(executedRequests) {
                                        executedRequests.add(request)
                                    }
                                }

                                PendingRequestType.DISLIKE -> {
                                    ApiService.service.dislikeById(request.id)
                                    synchronized(executedRequests) {
                                        executedRequests.add(request)
                                    }
                                }

                                PendingRequestType.DELETE -> {
                                    ApiService.service.deleteById(request.id)
                                    synchronized(executedRequests) {
                                        executedRequests.add(request)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            request.sent = false
                            throw e
                        }
                    }
                }
            }
        }
        clearExecutedRequests()
    }

    private fun clearExecutedRequests() {
        synchronized(pendingRequests) {
            synchronized(executedRequests) {
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

    override fun getNewerCount(newerId: Long): Flow<Int>  = flow {
        while(true) {
            delay(10.seconds)
            try {
                val response = ApiService.service.getNewer(newerId)
                val posts = response.body() ?: continue
                dao.insert(posts.toEntity())
                emit(posts.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // do nothing
            }

        }

    }

}

enum class PendingRequestType { DELETE, LIKE, DISLIKE }

data class PendingRequest(val type: PendingRequestType, val id: Long, var sent: Boolean = false)
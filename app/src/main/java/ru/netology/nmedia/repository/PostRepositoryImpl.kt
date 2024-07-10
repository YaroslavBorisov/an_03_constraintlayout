package ru.netology.nmedia.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkException
import ru.netology.nmedia.error.UnknownException
import java.io.File
import java.io.IOException
import java.util.Collections
import kotlin.time.Duration.Companion.seconds

class PostRepositoryImpl(
    private val dao: PostDao,
    private val apiService: ApiService
    ) : PostRepository {

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

            val response = apiService.getAll()
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
                                    apiService.likeById(request.id)
                                    synchronized(executedRequests) {
                                        executedRequests.add(request)
                                    }
                                }

                                PendingRequestType.DISLIKE -> {
                                    apiService.dislikeById(request.id)
                                    synchronized(executedRequests) {
                                        executedRequests.add(request)
                                    }
                                }

                                PendingRequestType.DELETE -> {
                                    apiService.deleteById(request.id)
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

//            return apiService.service.run {
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
            //apiService.service.deleteById(id)
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun save(post: Post) {
        try {
            val response = apiService.save(post)

            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            val body = response.body() ?: throw ApiError(response.code())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }

    }

    override suspend fun saveWithAttachment(post: Post, file: File) {
        save(post.copy(attachment = Attachment(upload(file).id, AttachmentType.IMAGE)))
    }

    private suspend fun upload(file: File): Media {
        try {
            val part = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
            val response = apiService.upload(part)
            if (!response.isSuccessful) {
                throw ApiError(response.code())
            }

            return response.body() ?: throw ApiError(response.code())
        } catch (e: IOException) {
            throw NetworkException
        } catch (e: Exception) {
            throw UnknownException
        }
    }

    override suspend fun shareById(id: Long) = Unit

    override suspend fun saveDraft(content: String) = Unit
    override suspend fun getDraft() = Unit
    override suspend fun deleteDraft() = Unit

    override fun getNewerCount(newerId: Long): Flow<Int> = flow {
        while (true) {
            delay(10.seconds)
            try {
                val response = apiService.getNewer(newerId)
                val posts = response.body() ?: continue
                dao.insert(posts.toEntity(false))
                //emit(posts.size)
                emit(dao.countHidden())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // do nothing
            }

        }

    }

    override suspend fun showHiddenPosts() {
        dao.showAll()
    }

}

enum class PendingRequestType { DELETE, LIKE, DISLIKE }

data class PendingRequest(val type: PendingRequestType, val id: Long, var sent: Boolean = false)
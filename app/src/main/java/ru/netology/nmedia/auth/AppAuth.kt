package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.error.ApiError
import java.io.File

class AppAuth private constructor (context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<Token?>(null)
    val state: StateFlow<Token?> = _state.asStateFlow()


    init {
        val id = prefs.getLong(ID_KEY, 0L)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id != 0L && token != null) {
            _state.value = Token(id, token)
        } else {
            prefs.edit { clear() }
        }
        sendPushToken()
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        _state.value = Token(id, token)
        prefs.edit {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
        }
        sendPushToken()
    }

    @Synchronized
    fun clearAuth() {
        _state.value = null
        prefs.edit { clear() }
        sendPushToken()
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val push = PushToken(token ?: Firebase.messaging.token.await())
                ApiService.service.saveToken(push)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"

        private var INSTANCE: AppAuth? = null

        fun getInstance() = requireNotNull(INSTANCE) {
            "You must init before"
        }

        fun init(context: Context) {
            INSTANCE = AppAuth(context.applicationContext)

        }

        val isAuthorized
            get() = getInstance()._state.value?.token != null

        suspend fun signIn(login: String, pass: String) {
            try {
                val response = ApiService.service.updateUser(login, pass)
                if (!response.isSuccessful) {
                    throw ApiError(response.code())
                }
                val token = requireNotNull(response.body())
                getInstance().setAuth(token.id, token.token)

            } catch (e: Exception) {
                throw e
            }
        }

        suspend fun signUp(userName: String, login: String, pass: String, file: File?) {
            try {
                val response = file?.let {
                    ApiService.service.registerUserWithPhoto(userName.toRequestBody("text/plain".toMediaType()),
                        login.toRequestBody("text/plain".toMediaType()),
                        pass.toRequestBody("text/plain".toMediaType()),
                        MultipartBody.Part.createFormData("file", it.name, it.asRequestBody()))
                } ?: ApiService.service.registerUser(login, pass, userName)

                if (!response.isSuccessful) {
                    throw ApiError(response.code())
                }
                val token = requireNotNull(response.body())
                getInstance().setAuth(token.id, token.token)

            } catch (e: Exception) {
                throw e
            }
        }

    }
}
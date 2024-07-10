package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel(
    private val appAuth: AppAuth
) : ViewModel() {
    val auth = appAuth.state
        .asLiveData(Dispatchers.Default)

    val isAuthorized: Boolean
        get() = appAuth.state.value?.token != null

}
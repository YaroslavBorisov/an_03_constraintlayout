package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel : ViewModel() {
    val auth = AppAuth.getInstance().state
        .asLiveData()
    val isAuthorized: Boolean
        get() = auth.value?.token != null

}
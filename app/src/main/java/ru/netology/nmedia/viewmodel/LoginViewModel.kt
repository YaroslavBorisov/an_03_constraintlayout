package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.model.LoginModelState
import ru.netology.nmedia.model.PhotoModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val appAuth: AppAuth
) : ViewModel() {
    private val _loginState = MutableLiveData(LoginModelState())

    val loginState: LiveData<LoginModelState>
        get() = _loginState

    private val nophoto = PhotoModel()

    private val _photo = MutableLiveData(nophoto)
    val photo: LiveData<PhotoModel>
        get() = _photo


    fun signIn(login: String, pass: String)  = viewModelScope.launch {
        try {
            _loginState.value = LoginModelState(loading = true)
            appAuth.signIn(login, pass)
        } catch (e: Exception) {
            _loginState.value = LoginModelState(error = true)
        }
    }

    fun resetState() {
        _loginState.value = LoginModelState()
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun dropPhoto() {
        _photo.value = nophoto
    }

    fun signUp(userName: String, login: String, pass: String) = viewModelScope.launch {
        try {
            _loginState.value = LoginModelState(loading = true)
            appAuth.signUp(userName, login, pass, _photo.value?.file)
        } catch (e: Exception) {
            _loginState.value = LoginModelState(error = true)
        }
    }
}
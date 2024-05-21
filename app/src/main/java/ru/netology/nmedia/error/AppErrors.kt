package ru.netology.nmedia.error

sealed class AppError(): RuntimeException()

data class ApiError(val code: Int): AppError()
object NetworkException: AppError()
object UnknownException: AppError()
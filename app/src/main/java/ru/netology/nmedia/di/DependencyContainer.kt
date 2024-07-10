package ru.netology.nmedia.di

import android.content.Context
import androidx.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.util.concurrent.TimeUnit

class DependencyContainer(private val context: Context) {
    private val appDb = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration()
        .build()

    companion object {


        @Volatile
        private var instance: DependencyContainer? = null

        fun initApp(context: Context) {
            instance = DependencyContainer(context)
        }

        fun getInstance(): DependencyContainer {
            return instance!!
        }


        private const val BASE_URL = "${BuildConfig.BASE_URL}api/slow/"
    }


    val appAuth = AppAuth(context)

    private val retrofit = Retrofit.Builder()
        .client(
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .run {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                            //level = HttpLoggingInterceptor.Level.HEADERS
                        })
                    } else {
                        this
                    }
                }.addInterceptor { chain ->
                    chain.proceed(
                        chain.run {
                            val token = appAuth.state.value?.token

                            if (token != null) {
                                request().newBuilder()
                                    .addHeader("Authorization", token)
                                    .build()
                            } else {
                                request()
                            }
                        }
                    )

                }
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    val apiService = retrofit.create<ApiService>()

    private val postDao = appDb.postDao

    val repository: PostRepository = PostRepositoryImpl(postDao, apiService)


}
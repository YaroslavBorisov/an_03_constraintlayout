package ru.netology.nmedia.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}api/slow/"
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        appAuth: AppAuth
    ): Retrofit = Retrofit.Builder()
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

    @Singleton
    @Provides
    fun provideApiService(
        retrofit:Retrofit)
    :ApiService = retrofit.create()


}
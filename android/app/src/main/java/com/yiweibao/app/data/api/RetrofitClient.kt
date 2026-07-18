package com.yiweibao.app.data.api

import com.yiweibao.app.BuildConfig
import com.yiweibao.app.util.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var tokenManager: TokenManager? = null

    fun init(tm: TokenManager) {
        tokenManager = tm
    }

    private val authInterceptor = Interceptor { chain ->
        val token = tokenManager?.let {
            runBlocking { it.tokenFlow.firstOrNull() }
        }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Accept-Language", "zh-CN")
                .build()
        } else {
            chain.request().newBuilder()
                .addHeader("Accept-Language", "zh-CN")
                .build()
        }
        chain.proceed(request)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

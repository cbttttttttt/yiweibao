package com.yiweibao.app.data.repository

import com.yiweibao.app.data.api.RetrofitClient
import com.yiweibao.app.data.model.LoginRequest

class AuthRepository {
    private val api = RetrofitClient.apiService

    suspend fun login(username: String, password: String) =
        api.login(LoginRequest(username, password))
}

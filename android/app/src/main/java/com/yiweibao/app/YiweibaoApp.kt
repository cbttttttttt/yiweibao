package com.yiweibao.app

import android.app.Application
import com.yiweibao.app.data.api.RetrofitClient
import com.yiweibao.app.util.TokenManager

class YiweibaoApp : Application() {
    lateinit var tokenManager: TokenManager

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        RetrofitClient.init(tokenManager)
    }
}

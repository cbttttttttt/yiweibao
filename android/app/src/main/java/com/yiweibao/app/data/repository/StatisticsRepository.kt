package com.yiweibao.app.data.repository

import com.yiweibao.app.data.api.RetrofitClient

class StatisticsRepository {
    private val api = RetrofitClient.apiService

    suspend fun getOverview() = api.getStatisticsOverview()

    suspend fun getFaultTypes() = api.getFaultTypes()

    suspend fun getTopEquipment(limit: Int = 5) = api.getTopEquipment(limit)

    suspend fun getFaultAvgTime() = api.getFaultAvgTime()
}

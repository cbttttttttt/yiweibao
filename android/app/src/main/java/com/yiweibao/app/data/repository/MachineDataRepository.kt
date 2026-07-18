package com.yiweibao.app.data.repository

import com.yiweibao.app.data.api.RetrofitClient

class MachineDataRepository {
    private val api = RetrofitClient.apiService

    suspend fun getRealtime() = api.getMachineDataRealtime()

    suspend fun getHistory(equipmentId: Long, minutes: Int = 30) =
        api.getMachineDataHistory(equipmentId, minutes)

    suspend fun getLatest(equipmentId: Long) =
        api.getMachineDataLatest(equipmentId)
}

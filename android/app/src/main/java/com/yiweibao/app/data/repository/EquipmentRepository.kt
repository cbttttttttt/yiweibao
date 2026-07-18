package com.yiweibao.app.data.repository

import com.yiweibao.app.data.api.RetrofitClient
import com.yiweibao.app.data.model.EquipmentRequest

class EquipmentRepository {
    private val api = RetrofitClient.apiService

    suspend fun getList(keyword: String? = null, page: Int = 0, size: Int = 20) =
        api.getEquipmentList(keyword, page, size)

    suspend fun getById(id: Long) = api.getEquipment(id)

    suspend fun create(request: EquipmentRequest) = api.createEquipment(request)

    suspend fun update(id: Long, request: EquipmentRequest) = api.updateEquipment(id, request)

    suspend fun scrap(id: Long) = api.scrapEquipment(id)

    suspend fun delete(id: Long) = api.deleteEquipment(id)
}

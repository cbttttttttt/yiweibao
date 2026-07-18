package com.yiweibao.app.data.repository

import com.yiweibao.app.data.api.RetrofitClient
import com.yiweibao.app.data.model.ApiResponse
import com.yiweibao.app.data.model.WorkOrderCompleteRequest
import com.yiweibao.app.data.model.WorkOrderCreateRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class WorkOrderRepository {
    private val api = RetrofitClient.apiService

    suspend fun getList(status: String? = null, equipmentId: Long? = null, page: Int = 0) =
        api.getWorkOrders(status, equipmentId, page)

    suspend fun getById(id: Long) = api.getWorkOrder(id)

    suspend fun create(request: WorkOrderCreateRequest) =
        api.createWorkOrder(request)

    suspend fun accept(id: Long) = api.acceptWorkOrder(id)

    suspend fun cancel(id: Long) = api.cancelWorkOrder(id)

    suspend fun complete(id: Long, request: WorkOrderCompleteRequest) =
        api.completeWorkOrder(id, request)

    suspend fun uploadFile(file: File): ApiResponse<String> {
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        return api.uploadFile(part)
    }
}

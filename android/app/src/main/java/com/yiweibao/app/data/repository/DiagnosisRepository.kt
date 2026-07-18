package com.yiweibao.app.data.repository

import com.yiweibao.app.data.api.RetrofitClient

class DiagnosisRepository {
    private val api = RetrofitClient.apiService
    suspend fun getDiagnosis(equipmentId: Long) = api.getDiagnosis(equipmentId)
    suspend fun getRules() = api.getDiagnosisRules()
}

package com.yiweibao.app.data.repository

import com.yiweibao.app.data.api.RetrofitClient

class DiagnosisRepository {
    private val api = RetrofitClient.apiService
    suspend fun getDiagnosis(equipmentId: Long) = api.getDiagnosis(equipmentId)
    suspend fun getRules() = api.getDiagnosisRules()
    suspend fun getRulesByCategory(category: String) = api.getDiagnosisRulesByCategory(category)
    suspend fun searchRules(keyword: String, category: String? = null) = api.searchDiagnosisRules(keyword, category)
    suspend fun searchCases(keyword: String, category: String? = null) = api.searchDiagnosisCases(keyword, category)
    suspend fun getCategories() = api.getDiagnosisCategories()
}

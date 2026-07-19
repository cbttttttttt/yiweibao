package com.yiweibao.app.data.api

import com.yiweibao.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: Map<String, String>): ApiResponse<LoginResponse>

    @GET("api/equipment")
    suspend fun getEquipmentList(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageData<Equipment>>

    @GET("api/equipment/{id}")
    suspend fun getEquipment(@Path("id") id: Long): ApiResponse<Equipment>

    @POST("api/equipment")
    suspend fun createEquipment(@Body request: EquipmentRequest): ApiResponse<Equipment>

    @PUT("api/equipment/{id}")
    suspend fun updateEquipment(@Path("id") id: Long, @Body request: EquipmentRequest): ApiResponse<Equipment>

    @PUT("api/equipment/{id}/scrap")
    suspend fun scrapEquipment(@Path("id") id: Long): ApiResponse<Equipment>

    @DELETE("api/equipment/{id}")
    suspend fun deleteEquipment(@Path("id") id: Long): ApiResponse<Map<String, String>>

    @GET("api/work-orders")
    suspend fun getWorkOrders(
        @Query("status") status: String? = null,
        @Query("equipmentId") equipmentId: Long? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageData<WorkOrder>>

    @GET("api/work-orders/{id}")
    suspend fun getWorkOrder(@Path("id") id: Long): ApiResponse<WorkOrder>

    @POST("api/work-orders")
    suspend fun createWorkOrder(@Body request: WorkOrderCreateRequest): ApiResponse<WorkOrder>

    @PUT("api/work-orders/{id}/accept")
    suspend fun acceptWorkOrder(@Path("id") id: Long): ApiResponse<WorkOrder>

    @PUT("api/work-orders/{id}/cancel")
    suspend fun cancelWorkOrder(@Path("id") id: Long): ApiResponse<WorkOrder>

    @PUT("api/work-orders/{id}/complete")
    suspend fun completeWorkOrder(@Path("id") id: Long, @Body request: WorkOrderCompleteRequest): ApiResponse<WorkOrder>

    @Multipart
    @POST("api/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): ApiResponse<String>

    @GET("api/statistics/overview")
    suspend fun getStatisticsOverview(): ApiResponse<StatisticsOverview>

    @GET("api/statistics/fault-types")
    suspend fun getFaultTypes(): ApiResponse<List<FaultTypeStat>>

    @GET("api/statistics/top-equipment")
    suspend fun getTopEquipment(@Query("limit") limit: Int = 5): ApiResponse<List<EquipmentFaultRank>>

    @GET("api/statistics/fault-avg-time")
    suspend fun getFaultAvgTime(): ApiResponse<List<FaultTypeAvgTime>>

    @GET("api/machine-data/realtime")
    suspend fun getMachineDataRealtime(): ApiResponse<List<MachineData>>

    @GET("api/machine-data/equipment/{id}/realtime")
    suspend fun getMachineDataHistory(
        @Path("id") id: Long,
        @Query("minutes") minutes: Int = 30
    ): ApiResponse<List<MachineData>>

    @GET("api/machine-data/equipment/{id}/latest")
    suspend fun getMachineDataLatest(@Path("id") id: Long): ApiResponse<MachineData>

    @GET("api/diagnosis/equipment/{equipmentId}")
    suspend fun getDiagnosis(@Path("equipmentId") id: Long): ApiResponse<List<DiagnosisResult>>

    @GET("api/diagnosis/rules")
    suspend fun getDiagnosisRules(): ApiResponse<List<DiagnosisResult>>

    @GET("api/diagnosis/rules")
    suspend fun getDiagnosisRulesByCategory(
        @Query("category") category: String
    ): ApiResponse<List<DiagnosisResult>>

    @GET("api/diagnosis/rules/search")
    suspend fun searchDiagnosisRules(
        @Query("keyword") keyword: String,
        @Query("category") category: String? = null
    ): ApiResponse<List<DiagnosisResult>>

    @GET("api/diagnosis/cases/search")
    suspend fun searchDiagnosisCases(
        @Query("keyword") keyword: String,
        @Query("category") category: String? = null
    ): ApiResponse<List<DiagnosisCaseVO>>

    @GET("api/diagnosis/categories")
    suspend fun getDiagnosisCategories(): ApiResponse<List<String>>

    @GET("api/health/scores")
    suspend fun getHealthScores(): ApiResponse<List<HealthScore>>

    @GET("api/health/{equipmentId}")
    suspend fun getHealthDetail(@Path("equipmentId") id: Long): ApiResponse<HealthDetail>
}

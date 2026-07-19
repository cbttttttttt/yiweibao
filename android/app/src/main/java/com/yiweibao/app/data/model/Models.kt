package com.yiweibao.app.data.model

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: Long,
    val username: String,
    val role: Int,
    val realName: String
)

data class Equipment(
    val id: Long,
    val code: String,
    val name: String,
    val model: String?,
    val spec: String?,
    val manufacturer: String?,
    val location: String?,
    val workshop: String?,
    val manager: String?,
    val purchaseDate: String?,
    val startDate: String?,
    val status: Int,
    val qrCodePath: String?
)

data class EquipmentRequest(
    val code: String,
    val name: String,
    val model: String? = null,
    val spec: String? = null,
    val manufacturer: String? = null,
    val location: String? = null,
    val workshop: String? = null,
    val manager: String? = null,
    val purchaseDate: String? = null,
    val startDate: String? = null,
    val status: Int = 0
)

data class WorkOrder(
    val id: Long,
    val orderNo: String,
    val equipment: Equipment,
    val reporter: String,
    val faultDesc: String,
    val faultCategory: String?,
    val urgency: Int,
    val photos: String?,
    val status: Int,
    val repairEngineer: String?,
    val diagnosis: String?,
    val repairAction: String?,
    val replacedParts: String?,
    val completedAt: String?,
    val createdAt: String?
)

data class WorkOrderCreateRequest(
    val equipmentId: Long,
    val faultDesc: String,
    val faultCategory: String,
    val urgency: Int = 0,
    val photos: String? = null,
    val reporter: String
)

data class WorkOrderCompleteRequest(
    val diagnosis: String,
    val repairAction: String,
    val replacedParts: String? = null,
    val photos: String? = null
)

data class StatisticsOverview(
    val totalFaults: Long,
    val thisMonthFaults: Long,
    val pendingOrders: Long,
    val avgRepairHours: Double
)

data class FaultTypeStat(
    val type: String,
    val count: Long
)

data class EquipmentFaultRank(
    val equipmentId: Long,
    val equipmentName: String,
    val workshop: String,
    val faultCount: Long
)

data class FaultTypeAvgTime(
    val type: String,
    val count: Long,
    val avgHours: Double
)

data class PageData<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)

data class DiagnosisResult(
    val id: Long,
    val name: String,
    val symptomDescription: String,
    val possibleCause: String,
    val recommendedAction: String,
    val faultCategory: String,
    val severityLevel: Int,
    val priority: Int,
    val verifiedCount: Int,
    val alternativeActions: String? = null,
    val keywords: String? = null,
    val repairSteps: String? = null,
    val toolsRequired: String? = null,
    val safetyNotes: String? = null,
    val estimatedHours: Double? = null,
    val applicableModels: String? = null
)

data class DiagnosisCaseVO(
    val id: Long,
    val workOrderId: Long,
    val orderNo: String,
    val equipmentId: Long,
    val equipmentName: String,
    val faultCategory: String,
    val faultDesc: String,
    val diagnosis: String,
    val repairAction: String,
    val replacedParts: String? = null,
    val repairEngineer: String? = null,
    val status: Int,
    val createdAt: String
)

data class MachineData(
    val id: Long,
    val equipmentId: Long,
    val equipmentName: String,
    val workshop: String?,
    val spindleSpeed: Double,
    val temperature: Double,
    val vibration: Double,
    val current: Double,
    val power: Double,
    val pressure: Double,
    val status: Int,
    val timestamp: String
)

data class HealthScore(
    val equipmentId: Long,
    val equipmentName: String,
    val workshop: String?,
    val score: Double,
    val status: String,
    val vibScore: Double,
    val tempScore: Double,
    val elecScore: Double
)

data class HealthDetail(
    val equipmentId: Long,
    val equipmentName: String,
    val workshop: String?,
    val totalScore: Double,
    val status: String,
    val factors: List<FactorDetail>,
    val rul: String,
    val vibScore: Double,
    val tempScore: Double,
    val elecScore: Double
)

data class FactorDetail(
    val name: String,
    val value: Double,
    val unit: String,
    val reference: String,
    val level: String
)

package com.yiweibao.app.ui.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiweibao.app.data.api.RetrofitClient
import com.yiweibao.app.data.model.HealthScore
import com.yiweibao.app.data.model.MachineData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MonitorUiState(
    val realtimeData: List<MachineData> = emptyList(),
    val healthScores: Map<Long, HealthScore> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortByHealth: Boolean = true
)

class MonitorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

    private val api = RetrofitClient.apiService
    private var pollingJob: Job? = null

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    val realtimeResult = api.getMachineDataRealtime()
                    val healthResult = api.getHealthScores()
                    if (realtimeResult.code == 200 && healthResult.code == 200) {
                        val healthMap = healthResult.data!!.associateBy { it.equipmentId }
                        _uiState.value = _uiState.value.copy(
                            realtimeData = realtimeResult.data ?: emptyList(),
                            healthScores = healthMap,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = realtimeResult.message
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
                delay(5000)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun toggleSort() {
        _uiState.value = _uiState.value.copy(
            sortByHealth = !_uiState.value.sortByHealth
        )
    }
}

package com.yiweibao.app.ui.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiweibao.app.data.model.DiagnosisResult
import com.yiweibao.app.data.model.MachineData
import com.yiweibao.app.data.repository.DiagnosisRepository
import com.yiweibao.app.data.repository.MachineDataRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MonitorUiState(
    val realtimeData: List<MachineData> = emptyList(),
    val historyData: List<MachineData> = emptyList(),
    val diagnosisResults: List<DiagnosisResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MonitorViewModel : ViewModel() {
    private val machineRepo = MachineDataRepository()
    private val diagnosisRepo = DiagnosisRepository()
    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState
    private var pollJob: Job? = null

    fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                loadRealtime()
                delay(5000)
            }
        }
    }

    fun stopPolling() { pollJob?.cancel() }

    fun loadHistory(equipmentId: Long) {
        viewModelScope.launch {
            try {
                val result = machineRepo.getHistory(equipmentId)
                _uiState.value = _uiState.value.copy(historyData = result.data ?: emptyList())
                loadDiagnosis(equipmentId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "加载历史数据失败: ${e.message}")
            }
        }
    }

    fun loadDiagnosis(equipmentId: Long) {
        viewModelScope.launch {
            try {
                val result = diagnosisRepo.getDiagnosis(equipmentId)
                _uiState.value = _uiState.value.copy(diagnosisResults = result.data ?: emptyList())
            } catch (_: Exception) { }
        }
    }

    private suspend fun loadRealtime() {
        try {
            val result = machineRepo.getRealtime()
            _uiState.value = _uiState.value.copy(
                realtimeData = result.data ?: emptyList(), isLoading = false, error = null)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "加载失败: ${e.message}")
        }
    }

    override fun onCleared() { super.onCleared(); pollJob?.cancel() }
}

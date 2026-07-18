package com.yiweibao.app.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiweibao.app.data.model.*
import com.yiweibao.app.data.repository.StatisticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StatisticsUiState(
    val overview: StatisticsOverview? = null,
    val faultTypes: List<FaultTypeStat> = emptyList(),
    val faultAvgTimes: List<FaultTypeAvgTime> = emptyList(),
    val topEquipment: List<EquipmentFaultRank> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class StatisticsViewModel : ViewModel() {
    private val repository = StatisticsRepository()

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val overviewR = repository.getOverview()
                val typesR = repository.getFaultTypes()
                val avgTimeR = repository.getFaultAvgTime()
                val topR = repository.getTopEquipment()

                _uiState.value = _uiState.value.copy(
                    overview = overviewR.data,
                    faultTypes = typesR.data ?: emptyList(),
                    faultAvgTimes = avgTimeR.data ?: emptyList(),
                    topEquipment = topR.data ?: emptyList(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "加载失败: ${e.message}")
            }
        }
    }
}

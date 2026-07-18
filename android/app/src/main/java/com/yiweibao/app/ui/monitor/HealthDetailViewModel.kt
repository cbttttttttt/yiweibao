package com.yiweibao.app.ui.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiweibao.app.data.api.RetrofitClient
import com.yiweibao.app.data.model.HealthDetail
import com.yiweibao.app.data.model.MachineData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HealthDetailUiState(
    val detail: HealthDetail? = null,
    val trendData: List<MachineData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HealthDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HealthDetailUiState())
    val uiState: StateFlow<HealthDetailUiState> = _uiState.asStateFlow()

    private val api = RetrofitClient.apiService

    fun load(equipmentId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val detailResult = api.getHealthDetail(equipmentId)
                val trendResult = api.getMachineDataHistory(equipmentId, 30)
                if (detailResult.code == 200) {
                    _uiState.value = _uiState.value.copy(
                        detail = detailResult.data,
                        trendData = if (trendResult.code == 200) trendResult.data ?: emptyList() else emptyList(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = detailResult.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }
}

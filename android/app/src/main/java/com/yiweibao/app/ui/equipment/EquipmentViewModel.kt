package com.yiweibao.app.ui.equipment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yiweibao.app.data.model.Equipment
import com.yiweibao.app.data.repository.EquipmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EquipmentListState(
    val list: List<Equipment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchKeyword: String = ""
)

data class EquipmentDetailState(
    val equipment: Equipment? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class EquipmentFormState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class EquipmentViewModel : ViewModel() {
    private val repository = EquipmentRepository()

    private val _listState = MutableStateFlow(EquipmentListState())
    val listState: StateFlow<EquipmentListState> = _listState

    private val _detailState = MutableStateFlow(EquipmentDetailState())
    val detailState: StateFlow<EquipmentDetailState> = _detailState

    private val _formState = MutableStateFlow(EquipmentFormState())
    val formState: StateFlow<EquipmentFormState> = _formState

    fun loadList(keyword: String? = null) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, error = null, searchKeyword = keyword ?: "")
            try {
                val response = repository.getList(keyword = keyword)
                if (response.code == 200 && response.data != null) {
                    _listState.value = _listState.value.copy(list = response.data.content, isLoading = false)
                } else {
                    _listState.value = _listState.value.copy(isLoading = false, error = response.message)
                }
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(isLoading = false, error = "加载失败: ${e.message}")
            }
        }
    }

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            _detailState.value = EquipmentDetailState(isLoading = true)
            try {
                val response = repository.getById(id)
                if (response.code == 200) {
                    _detailState.value = EquipmentDetailState(equipment = response.data, isLoading = false)
                } else {
                    _detailState.value = EquipmentDetailState(isLoading = false, error = response.message)
                }
            } catch (e: Exception) {
                _detailState.value = EquipmentDetailState(isLoading = false, error = "加载失败: ${e.message}")
            }
        }
    }

    fun saveEquipment(request: com.yiweibao.app.data.model.EquipmentRequest, editId: Long? = null) {
        viewModelScope.launch {
            _formState.value = EquipmentFormState(isSaving = true)
            try {
                val response = if (editId != null) repository.update(editId, request)
                else repository.create(request)
                if (response.code == 200) {
                    _formState.value = EquipmentFormState(saveSuccess = true)
                } else {
                    _formState.value = EquipmentFormState(error = response.message)
                }
            } catch (e: Exception) {
                _formState.value = EquipmentFormState(error = "保存失败: ${e.message}")
            }
        }
    }

    fun resetFormState() { _formState.value = EquipmentFormState() }

    private val _actionState = MutableStateFlow<EquipmentActionState>(EquipmentActionState.None)
    val actionState: StateFlow<EquipmentActionState> = _actionState

    fun scrapEquipment(id: Long) {
        viewModelScope.launch {
            _actionState.value = EquipmentActionState.Loading
            try {
                val response = repository.scrap(id)
                if (response.code == 200) {
                    _actionState.value = EquipmentActionState.Success("设备已报废")
                    loadDetail(id)
                } else {
                    _actionState.value = EquipmentActionState.Error(response.message)
                }
            } catch (e: Exception) {
                _actionState.value = EquipmentActionState.Error("报废失败: ${e.message}")
            }
        }
    }

    fun deleteEquipment(id: Long) {
        viewModelScope.launch {
            _actionState.value = EquipmentActionState.Loading
            try {
                val response = repository.delete(id)
                if (response.code == 200) {
                    _actionState.value = EquipmentActionState.Deleted
                } else {
                    _actionState.value = EquipmentActionState.Error(response.message)
                }
            } catch (e: Exception) {
                _actionState.value = EquipmentActionState.Error("删除失败: ${e.message}")
            }
        }
    }

    fun resetActionState() { _actionState.value = EquipmentActionState.None }
}

sealed class EquipmentActionState {
    data object None : EquipmentActionState()
    data object Loading : EquipmentActionState()
    data class Success(val message: String) : EquipmentActionState()
    data class Error(val message: String) : EquipmentActionState()
    data object Deleted : EquipmentActionState()
}

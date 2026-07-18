package com.yiweibao.app.ui.workorder

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yiweibao.app.data.model.WorkOrder
import com.yiweibao.app.data.model.WorkOrderCreateRequest
import com.yiweibao.app.data.model.WorkOrderCompleteRequest
import com.yiweibao.app.data.repository.WorkOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File

data class WorkOrderListState(
    val list: List<WorkOrder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTab: Int = 0
)

data class WorkOrderDetailState(
    val workOrder: WorkOrder? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class WorkOrderActionState(
    val isProcessing: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val successMessage: String = ""
)

data class PhotoItem(
    val uri: Uri,
    val uploadedUrl: String? = null,
    val isUploading: Boolean = false
)

class WorkOrderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WorkOrderRepository()

    private val _listState = MutableStateFlow(WorkOrderListState())
    val listState: StateFlow<WorkOrderListState> = _listState

    private val _detailState = MutableStateFlow(WorkOrderDetailState())
    val detailState: StateFlow<WorkOrderDetailState> = _detailState

    private val _actionState = MutableStateFlow(WorkOrderActionState())
    val actionState: StateFlow<WorkOrderActionState> = _actionState

    private val _photos = MutableStateFlow<List<PhotoItem>>(emptyList())
    val photos: StateFlow<List<PhotoItem>> = _photos

    fun addPhoto(uri: Uri) {
        _photos.value = _photos.value + PhotoItem(uri = uri)
    }

    fun removePhoto(index: Int) {
        _photos.value = _photos.value.toMutableList().also { it.removeAt(index) }
    }

    fun clearPhotos() {
        _photos.value = emptyList()
    }

    private suspend fun uploadPhotos(): String? {
        val items = _photos.value
        if (items.isEmpty()) return null

        val cacheDir = getApplication<Application>().cacheDir
        val resolver = getApplication<Application>().contentResolver
        val urls = mutableListOf<String>()

        for ((i, item) in items.withIndex()) {
            _photos.value = _photos.value.toMutableList().also { it[i] = item.copy(isUploading = true) }
            try {
                val file = File(cacheDir, "photo_${System.currentTimeMillis()}_$i.jpg")
                resolver.openInputStream(item.uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val response = repository.uploadFile(file)
                if (response.code == 200 && response.data != null) {
                    urls.add(response.data)
                }
                file.delete()
            } catch (_: Exception) {
                // Skip failed uploads
            } finally {
                _photos.value = _photos.value.toMutableList().also { it[i] = item.copy(isUploading = false) }
            }
        }
        return if (urls.isNotEmpty()) JSONArray(urls).toString() else null
    }

    fun loadList(tab: Int = 0, equipmentId: Long? = null) {
        val statusFilter = when (tab) {
            0 -> "0"
            1 -> "1"
            2 -> "2"
            3 -> "3"
            else -> null
        }
        viewModelScope.launch {
            _listState.value = WorkOrderListState(isLoading = true, currentTab = tab)
            try {
                val response = repository.getList(status = statusFilter, equipmentId = equipmentId)
                if (response.code == 200 && response.data != null) {
                    _listState.value = WorkOrderListState(list = response.data.content, currentTab = tab)
                } else {
                    _listState.value = WorkOrderListState(error = response.message, currentTab = tab)
                }
            } catch (e: Exception) {
                _listState.value = WorkOrderListState(error = "加载失败: ${e.message}", currentTab = tab)
            }
        }
    }

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            _detailState.value = WorkOrderDetailState(isLoading = true)
            try {
                val response = repository.getById(id)
                if (response.code == 200) {
                    _detailState.value = WorkOrderDetailState(workOrder = response.data)
                } else {
                    _detailState.value = WorkOrderDetailState(error = response.message)
                }
            } catch (e: Exception) {
                _detailState.value = WorkOrderDetailState(error = "加载失败: ${e.message}")
            }
        }
    }

    fun createWorkOrder(request: WorkOrderCreateRequest) {
        viewModelScope.launch {
            _actionState.value = WorkOrderActionState(isProcessing = true)
            try {
                val photosJson = uploadPhotos()
                val finalRequest = request.copy(photos = photosJson)
                val response = repository.create(finalRequest)
                if (response.code == 200) {
                    clearPhotos()
                    _actionState.value = WorkOrderActionState(success = true, successMessage = "报修成功")
                } else {
                    _actionState.value = WorkOrderActionState(error = response.message)
                }
            } catch (e: Exception) {
                _actionState.value = WorkOrderActionState(error = "提交失败: ${e.message}")
            }
        }
    }

    fun accept(id: Long) {
        viewModelScope.launch {
            _actionState.value = WorkOrderActionState(isProcessing = true)
            try {
                val response = repository.accept(id)
                if (response.code == 200) {
                    _actionState.value = WorkOrderActionState(success = true, successMessage = "接单成功")
                    loadDetail(id)
                } else {
                    _actionState.value = WorkOrderActionState(error = response.message)
                }
            } catch (e: Exception) {
                _actionState.value = WorkOrderActionState(error = "操作失败: ${e.message}")
            }
        }
    }

    fun cancel(id: Long) {
        viewModelScope.launch {
            _actionState.value = WorkOrderActionState(isProcessing = true)
            try {
                val response = repository.cancel(id)
                if (response.code == 200) {
                    _actionState.value = WorkOrderActionState(success = true, successMessage = "已撤销")
                    loadDetail(id)
                } else {
                    _actionState.value = WorkOrderActionState(error = response.message)
                }
            } catch (e: Exception) {
                _actionState.value = WorkOrderActionState(error = "操作失败: ${e.message}")
            }
        }
    }

    fun complete(id: Long, request: WorkOrderCompleteRequest) {
        viewModelScope.launch {
            _actionState.value = WorkOrderActionState(isProcessing = true)
            try {
                val photosJson = uploadPhotos()
                val finalRequest = request.copy(photos = photosJson)
                val response = repository.complete(id, finalRequest)
                if (response.code == 200) {
                    clearPhotos()
                    _actionState.value = WorkOrderActionState(success = true, successMessage = "维修完成")
                } else {
                    _actionState.value = WorkOrderActionState(error = response.message)
                }
            } catch (e: Exception) {
                _actionState.value = WorkOrderActionState(error = "提交失败: ${e.message}")
            }
        }
    }

    fun resetActionState() { _actionState.value = WorkOrderActionState() }
}

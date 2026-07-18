package com.yiweibao.app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yiweibao.app.data.repository.AuthRepository
import com.yiweibao.app.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun updateUsername(v: String) { _uiState.value = _uiState.value.copy(username = v, error = null) }
    fun updatePassword(v: String) { _uiState.value = _uiState.value.copy(password = v, error = null) }

    fun login() {
        val s = _uiState.value
        if (s.username.isBlank() || s.password.isBlank()) {
            _uiState.value = s.copy(error = "请输入用户名和密码")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = repository.login(s.username, s.password)
                if (response.code == 200 && response.data != null) {
                    tokenManager.saveLogin(
                        response.data.token, response.data.userId,
                        response.data.username, response.data.role, response.data.realName
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = response.message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "连接服务器失败: ${e.message}"
                )
            }
        }
    }
}

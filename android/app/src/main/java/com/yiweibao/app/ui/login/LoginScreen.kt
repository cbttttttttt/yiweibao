package com.yiweibao.app.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) onLoginSuccess()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("易维保", fontSize = 32.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text("智能管理App", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(48.dp))

            OutlinedTextField(
                value = state.username, onValueChange = viewModel::updateUsername,
                label = { Text("用户名") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.password, onValueChange = viewModel::updatePassword,
                label = { Text("密码") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, "toggle")
                    }
                },
                enabled = !state.isLoading
            )
            Spacer(Modifier.height(8.dp))

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = viewModel::login, modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                else Text("登录", fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))
            Text("演示账号: admin / 123456", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

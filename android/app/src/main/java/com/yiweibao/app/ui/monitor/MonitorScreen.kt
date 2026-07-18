package com.yiweibao.app.ui.monitor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yiweibao.app.data.model.MachineData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    onBack: () -> Unit,
    onEquipmentClick: (Long, String) -> Unit,
    viewModel: MonitorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startPolling()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设备实时监控") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.error != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.startPolling() }) { Text("重试") }
                }
            }
        } else if (state.realtimeData.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("等待数据...", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("在线设备: ${state.realtimeData.size} 台",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text("自动刷新 5s", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                items(state.realtimeData, key = { it.equipmentId }) { data ->
                    EquipmentStatusCard(data = data, onClick = {
                        onEquipmentClick(data.equipmentId, data.equipmentName)
                    })
                }
            }
        }
    }
}

@Composable
fun EquipmentStatusCard(data: MachineData, onClick: () -> Unit) {
    val statusColor = when (data.status) {
        2 -> Color(0xFFE53935)
        1 -> Color(0xFFFB8C00)
        else -> Color(0xFF43A047)
    }
    val statusText = when (data.status) {
        2 -> "告警"
        1 -> "预警"
        else -> "正常"
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(data.equipmentName, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    if (!data.workshop.isNullOrBlank()) {
                        Text(data.workshop, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem("转速", "${data.spindleSpeed.toInt()} rpm", data.spindleSpeed > 3000)
                MetricItem("温度", "${data.temperature}°C", data.temperature > 60)
                MetricItem("振动", "${data.vibration} mm/s", data.vibration > 3.5)
                MetricItem("功率", "${data.power} kW", data.power > 8)
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, warning: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.SemiBold,
            color = if (warning) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline)
    }
}

package com.yiweibao.app.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(onBack: () -> Unit = {}, viewModel: StatisticsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAll() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("故障统计") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 概览卡片
            if (state.overview != null) {
                item {
                    Text("故障概览", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("总故障", "${state.overview!!.totalFaults}", Color(0xFF2196F3), Modifier.weight(1f))
                        StatCard("本月新增", "${state.overview!!.thisMonthFaults}", Color(0xFFFF9800), Modifier.weight(1f))
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("待处理", "${state.overview!!.pendingOrders}", Color(0xFFF44336), Modifier.weight(1f))
                        StatCard("平均处理(h)", "${state.overview!!.avgRepairHours}", Color(0xFF4CAF50), Modifier.weight(1f))
                    }
                }
            }

            // 故障分类
            if (state.faultTypes.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("故障分类", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            state.faultTypes.forEach { stat ->
                                val total = state.faultTypes.sumOf { it.count }.toFloat()
                                val ratio = if (total > 0) stat.count / total else 0f
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text(stat.type, modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium)
                                    LinearProgressIndicator(
                                        progress = { ratio },
                                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                                    )
                                    Text("${stat.count}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // 各分类平均处理时间
            if (state.faultAvgTimes.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("各分类平均处理时间", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            state.faultAvgTimes.forEach { avg ->
                                val timeColor = when {
                                    avg.avgHours < 2.0 -> Color(0xFF4CAF50)
                                    avg.avgHours < 6.0 -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(avg.type, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("${avg.count}单", style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline)
                                    }
                                    Text("${avg.avgHours}h", style = MaterialTheme.typography.titleMedium,
                                        color = timeColor, fontWeight = FontWeight.Bold)
                                }
                                if (avg != state.faultAvgTimes.last()) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 设备故障排行
            if (state.topEquipment.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("设备故障排行 Top${state.topEquipment.size}", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                }
                items(state.topEquipment) { rank ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rank.equipmentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(rank.workshop, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Text("${rank.faultCount}次", style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            if (state.error != null) {
                item { Text(state.error!!, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

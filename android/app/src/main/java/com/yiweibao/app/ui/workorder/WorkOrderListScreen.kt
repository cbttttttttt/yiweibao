package com.yiweibao.app.ui.workorder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
fun WorkOrderListScreen(
    onDetailClick: (Long) -> Unit,
    onCreateClick: () -> Unit,
    equipmentId: Long? = null,
    title: String = "维修工单",
    onBack: (() -> Unit)? = null,
    viewModel: WorkOrderViewModel = viewModel()
) {
    val state by viewModel.listState.collectAsState()

    LaunchedEffect(state.currentTab, equipmentId) { viewModel.loadList(state.currentTab, equipmentId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "返回")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (equipmentId == null) {
                FloatingActionButton(onClick = onCreateClick) {
                    Icon(Icons.Default.Add, "新建工单")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = state.currentTab) {
                Tab(selected = state.currentTab == 0, onClick = { viewModel.loadList(0) },
                    text = { Text("待处理") })
                Tab(selected = state.currentTab == 1, onClick = { viewModel.loadList(1) },
                    text = { Text("处理中") })
                Tab(selected = state.currentTab == 2, onClick = { viewModel.loadList(2) },
                    text = { Text("已完成") })
                Tab(selected = state.currentTab == 3, onClick = { viewModel.loadList(3) },
                    text = { Text("已撤销") })
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.list) { order ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { onDetailClick(order.id) }) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(order.orderNo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    UrgencyChip(order.urgency)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("设备: ${order.equipment.name}", style = MaterialTheme.typography.bodyMedium)
                                Text("报修人: ${order.reporter}", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline)
                                Text(order.faultDesc, style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    if (state.list.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("暂无工单", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UrgencyChip(urgency: Int) {
    val (text, color) = when (urgency) {
        0 -> "普通" to Color(0xFF4CAF50)
        1 -> "紧急" to Color(0xFFFF9800)
        2 -> "特急" to Color(0xFFF44336)
        else -> "" to Color.Gray
    }
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = color, style = MaterialTheme.typography.labelSmall)
    }
}

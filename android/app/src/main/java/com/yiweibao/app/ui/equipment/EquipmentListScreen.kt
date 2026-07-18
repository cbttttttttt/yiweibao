package com.yiweibao.app.ui.equipment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentListScreen(
    onDetailClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: EquipmentViewModel = viewModel()
) {
    val state by viewModel.listState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadList() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设备台账") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, "新增设备")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchText, onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索设备编码/名称/车间") },
                leadingIcon = { Icon(Icons.Default.Search, "搜索") },
                singleLine = true
            )
            Button(modifier = Modifier.padding(horizontal = 16.dp), onClick = { viewModel.loadList(searchText.ifBlank { null }) }) {
                Text("搜索")
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
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.list) { equipment ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { onDetailClick(equipment.id) }) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(equipment.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("编码: ${equipment.code}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                    Text(equipment.workshop ?: "", style = MaterialTheme.typography.bodySmall)
                                }
                                StatusChip(status = equipment.status)
                                Icon(Icons.Default.QrCode, "二维码", tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    if (state.list.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("暂无设备数据", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: Int) {
    val (text, color) = when (status) {
        0 -> "正常" to MaterialTheme.colorScheme.primary
        1 -> "待维修" to MaterialTheme.colorScheme.error
        2 -> "维修中" to MaterialTheme.colorScheme.tertiary
        3 -> "已报废" to MaterialTheme.colorScheme.outline
        else -> "未知" to MaterialTheme.colorScheme.outline
    }
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color, style = MaterialTheme.typography.labelSmall)
    }
}

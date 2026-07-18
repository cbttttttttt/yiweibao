package com.yiweibao.app.ui.equipment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.yiweibao.app.BuildConfig
import com.yiweibao.app.data.model.Equipment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentDetailScreen(
    id: Long, onBack: () -> Unit, onEdit: () -> Unit, onCreateOrder: () -> Unit,
    onViewOrders: (name: String) -> Unit,
    viewModel: EquipmentViewModel = viewModel()
) {
    val state by viewModel.detailState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    var showQr by remember { mutableStateOf(false) }
    var showScrapDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(id) { viewModel.loadDetail(id) }

    LaunchedEffect(actionState) {
        when (actionState) {
            is EquipmentActionState.Deleted -> onBack()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("设备详情") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                actions = {
                    IconButton(onClick = { showQr = true }) { Icon(Icons.Default.QrCode, "二维码") }
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "编辑") }
                })
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.equipment != null -> {
                val e = state.equipment!!
                Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(e.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(4.dp))
                            StatusChip(status = e.status)
                            Spacer(Modifier.height(12.dp))
                            InfoRow("设备编码", e.code)
                            InfoRow("型号", e.model ?: "-")
                            InfoRow("规格", e.spec ?: "-")
                            InfoRow("厂商", e.manufacturer ?: "-")
                            InfoRow("安装位置", e.location ?: "-")
                            InfoRow("所属车间", e.workshop ?: "-")
                            InfoRow("负责人", e.manager ?: "-")
                            InfoRow("购买日期", e.purchaseDate ?: "-")
                            InfoRow("启用日期", e.startDate ?: "-")
                        }
                    }

                    if (e.status != 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onCreateOrder, modifier = Modifier.weight(1f)
                            ) { Text("故障报修") }
                            OutlinedButton(
                                onClick = { showScrapDialog = true }, modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) { Text("报废") }
                        }
                    }

                    OutlinedButton(
                        onClick = { onViewOrders(e.name) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    ) { Text("维修记录") }

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("删除设备")
                    }

                    if (actionState is EquipmentActionState.Loading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showQr && state.equipment != null) {
        val e = state.equipment!!
        val qrUrl = "${BuildConfig.BASE_URL.removeSuffix("/")}${e.qrCodePath}"
        AlertDialog(onDismissRequest = { showQr = false },
            title = { Text("设备二维码") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = qrUrl,
                        contentDescription = "设备二维码",
                        modifier = Modifier.size(250.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("${e.code} ${e.name}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "yiweibao://equipment/${e.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showQr = false }) { Text("关闭") } }
        )
    }

    if (showScrapDialog && state.equipment != null) {
        AlertDialog(
            onDismissRequest = { showScrapDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("确认报废") },
            text = { Text("确定将「${state.equipment!!.name}」标记为已报废吗？报废后无法撤消，且该设备将无法创建新工单。") },
            confirmButton = {
                TextButton(onClick = {
                    showScrapDialog = false
                    viewModel.scrapEquipment(id)
                }) { Text("确认报废", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showScrapDialog = false }) { Text("取消") } }
        )
    }

    if (showDeleteDialog && state.equipment != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("确认删除") },
            text = { Text("确定删除「${state.equipment!!.name}」吗？此操作不可撤消，关联的工单记录也将被删除。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteEquipment(id)
                }) { Text("确认删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("取消") } }
        )
    }

    if (actionState is EquipmentActionState.Success) {
        val msg = (actionState as EquipmentActionState.Success).message
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(1500)
            viewModel.resetActionState()
        }
    }

    if (actionState is EquipmentActionState.Error) {
        val msg = (actionState as EquipmentActionState.Error).message
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetActionState()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.width(100.dp), color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

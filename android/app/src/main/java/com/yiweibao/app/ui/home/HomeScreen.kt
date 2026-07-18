package com.yiweibao.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class HomeMenuItem(
    val title: String,
    val subtitle: String,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    realName: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val menuItems = listOf(
        HomeMenuItem(
            title = "设备台账",
            subtitle = "管理设备信息、查看设备二维码",
            icon = { Icon(Icons.Default.PrecisionManufacturing, null, modifier = Modifier.size(40.dp)) }
        ),
        HomeMenuItem(
            title = "实时监控",
            subtitle = "查看设备运行数据与状态",
            icon = { Icon(Icons.Default.Sensors, null, modifier = Modifier.size(40.dp)) }
        ),
        HomeMenuItem(
            title = "维修工单",
            subtitle = "创建与跟踪维修工单",
            icon = { Icon(Icons.Default.Build, null, modifier = Modifier.size(40.dp)) }
        ),
        HomeMenuItem(
            title = "故障统计",
            subtitle = "查看故障分析报表",
            icon = { Icon(Icons.Default.Description, null, modifier = Modifier.size(40.dp)) }
        ),
        HomeMenuItem(
            title = "扫码查询",
            subtitle = "扫描设备二维码快速查询",
            icon = { Icon(Icons.Default.QrCode, null, modifier = Modifier.size(40.dp)) }
        ),
        HomeMenuItem(
            title = "诊断知识库",
            subtitle = "查看诊断规则与维修知识",
            icon = { Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(40.dp)) }
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("易维保") },
                actions = {
                    Text(realName, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, "退出登录")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                "欢迎, $realName",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Text(
                "智能维修管理，让设备维保更高效",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            val routes = listOf("equipment_list", "monitor", "workorder_list", "statistics", "scan", "knowledge_base")
            menuItems.forEachIndexed { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onNavigate(routes[index]) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CompositionLocalProvider(
                                    LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    item.icon()
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium)
                            Text(item.subtitle, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline)
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text("退出登录") },
            text = { Text("确定要退出当前账号吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    scope.launch { onLogout() }
                }) { Text("确认") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("取消") } }
        )
    }
}

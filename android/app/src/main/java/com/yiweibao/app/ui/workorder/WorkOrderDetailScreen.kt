package com.yiweibao.app.ui.workorder

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.yiweibao.app.BuildConfig
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderDetailScreen(
    id: Long, onBack: () -> Unit, onAccept: () -> Unit, onRepair: () -> Unit,
    viewModel: WorkOrderViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(id) { viewModel.loadDetail(id) }
    LaunchedEffect(actionState.success) {
        if (actionState.success) viewModel.resetActionState()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("工单详情") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } })
        }
    ) { padding ->
        when {
            detailState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            detailState.workOrder != null -> {
                val wo = detailState.workOrder!!
                Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(wo.orderNo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                UrgencyChip(wo.urgency)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            DetailRow("设备", wo.equipment.name)
                            DetailRow("设备编码", wo.equipment.code)
                            DetailRow("报修人", wo.reporter)
                            DetailRow("报修时间", wo.createdAt ?: "-")
                            DetailRow("故障描述", wo.faultDesc)
                            DetailRow("状态", when (wo.status) { 0->"待处理"; 1->"处理中"; 2->"已完成"; 3->"已撤销"; else->"未知" })

                            if (wo.status >= 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                DetailRow("维修人员", wo.repairEngineer ?: "-")
                            }
                            if (wo.status == 2) {
                                DetailRow("故障诊断", wo.diagnosis ?: "-")
                                DetailRow("维修措施", wo.repairAction ?: "-")
                                DetailRow("更换备件", wo.replacedParts ?: "无")
                                DetailRow("完成时间", wo.completedAt ?: "-")
                            }

                            // Display photos
                            val photoUrls = remember(wo.photos) { parsePhotoUrls(wo.photos) }
                            if (photoUrls.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text("现场照片", color = MaterialTheme.colorScheme.outline,
                                    style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    photoUrls.forEach { url ->
                                        Image(
                                            painter = rememberAsyncImagePainter(url),
                                            contentDescription = "现场照片",
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons
                    if (wo.status == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.accept(id) },
                                modifier = Modifier.weight(1f),
                                enabled = !actionState.isProcessing
                            ) {
                                if (actionState.isProcessing) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                                else Text("接单")
                            }
                            OutlinedButton(
                                onClick = { viewModel.cancel(id) },
                                modifier = Modifier.weight(1f),
                                enabled = !actionState.isProcessing,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("撤销")
                            }
                        }
                    }
                    if (wo.status == 1) {
                        Button(onClick = onRepair,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("填写维修记录")
                        }
                    }
                    if (actionState.error != null) {
                        Text(actionState.error!!, color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp))
                    }
                }
            }
            detailState.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(detailState.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun parsePhotoUrls(photos: String?): List<String> {
    if (photos.isNullOrBlank()) return emptyList()
    return try {
        val arr = JSONArray(photos)
        val baseUrl = BuildConfig.BASE_URL.removeSuffix("/")
        (0 until arr.length()).map { i -> baseUrl + arr.getString(i) }
    } catch (_: Exception) {
        emptyList()
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

package com.yiweibao.app.ui.monitor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yiweibao.app.data.model.DiagnosisResult
import com.yiweibao.app.data.model.MachineData
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentDataScreen(
    equipmentId: Long,
    equipmentName: String,
    onBack: () -> Unit,
    onCreateWorkOrder: ((Long, String, String) -> Unit)? = null,
    viewModel: MonitorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedMinutes by remember { mutableIntStateOf(30) }

    LaunchedEffect(equipmentId, selectedMinutes) {
        viewModel.loadHistory(equipmentId, selectedMinutes)
        viewModel.startPolling()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(equipmentName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadHistory(equipmentId, selectedMinutes) }) {
                        Icon(Icons.Default.Refresh, "刷新")
                    }
                }
            )
        }
    ) { padding ->
        val latest = state.realtimeData.find { it.equipmentId == equipmentId }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (latest != null) {
                LatestValuesCard(latest)
            }

            if (state.diagnosisResults.isNotEmpty()) {
                DiagnosisSection(
                    results = state.diagnosisResults,
                    equipmentId = equipmentId,
                    equipmentName = equipmentName,
                    onCreateWorkOrder = onCreateWorkOrder
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(10 to "10分钟", 30 to "30分钟", 60 to "1小时").forEach { (min, label) ->
                    FilterChip(
                        selected = selectedMinutes == min,
                        onClick = { selectedMinutes = min },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (state.historyData.isNotEmpty()) {
                Text("主轴转速 (rpm)", fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                LineChart(
                    data = state.historyData,
                    valueExtractor = { it.spindleSpeed },
                    modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 16.dp),
                    lineColor = Color(0xFF2196F3)
                )

                Text("温度 (°C)", fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                LineChart(
                    data = state.historyData,
                    valueExtractor = { it.temperature },
                    modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 16.dp),
                    lineColor = Color(0xFFE53935)
                )

                Text("振动 (mm/s)", fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                LineChart(
                    data = state.historyData,
                    valueExtractor = { it.vibration },
                    modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 16.dp),
                    lineColor = Color(0xFFFFA726)
                )

                Text("功率 (kW)", fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                LineChart(
                    data = state.historyData,
                    valueExtractor = { it.power },
                    modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 16.dp),
                    lineColor = Color(0xFF66BB6A)
                )

                Spacer(Modifier.height(16.dp))
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无历史数据", color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun LatestValuesCard(data: MachineData) {
    val statusColor = when (data.status) {
        2 -> Color(0xFFE53935)
        1 -> Color(0xFFFFA726)
        else -> Color(0xFF43A047)
    }
    val statusText = when (data.status) { 2 -> "告警" 1 -> "预警" else -> "正常" }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("当前状态", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Surface(shape = RoundedCornerShape(12.dp), color = statusColor.copy(alpha = 0.2f)) {
                    Text(statusText, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        color = statusColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ValueGauge("主轴转速", "${data.spindleSpeed.toInt()}", "rpm", data.spindleSpeed, 0.0, 5000.0)
                ValueGauge("温度", "${data.temperature}", "°C", data.temperature, 20.0, 80.0)
                ValueGauge("振动", "${data.vibration}", "mm/s", data.vibration, 0.0, 8.0)
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ValueGauge("电流", "${data.current}", "A", data.current, 0.0, 30.0)
                ValueGauge("功率", "${data.power}", "kW", data.power, 0.0, 15.0)
                ValueGauge("液压", "${data.pressure}", "MPa", data.pressure, 0.0, 10.0)
            }
        }
    }
}

@Composable
fun ValueGauge(
    label: String, value: String, unit: String,
    current: Double, min: Double, max: Double
) {
    val ratio = ((current - min) / (max - min)).toFloat().coerceIn(0f, 1f)
    val color = when {
        ratio > 0.8f -> Color(0xFFE53935)
        ratio > 0.6f -> Color(0xFFFFA726)
        else -> Color(0xFF43A047)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(unit, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun LineChart(
    data: List<MachineData>,
    valueExtractor: (MachineData) -> Double,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF2196F3)
) {
    val values = data.map(valueExtractor)
    if (values.isEmpty()) return

    val minVal = values.min()
    val maxVal = values.max()
    val range = if (maxVal == minVal) 1.0 else maxVal - minVal

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val w = size.width
            val h = size.height
            val stepX = w / (values.size - 1).coerceAtLeast(1)

            // Grid lines
            for (i in 0..4) {
                val y = h * i / 4
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }

            // Line path
            val path = Path()
            values.forEachIndexed { index, value ->
                val x = index * stepX
                val y = h - ((value - minVal) / range * h).toFloat()
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(path, color = lineColor, style = Stroke(width = 2.5f))

            // Dots
            val dotInterval = max(1, values.size / 20)
            values.forEachIndexed { index, value ->
                if (index % dotInterval == 0) {
                    val x = index * stepX
                    val y = h - ((value - minVal) / range * h).toFloat()
                    drawCircle(lineColor, radius = 3f, center = Offset(x, y))
                }
            }
        }
    }

    // Min/Max labels
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(String.format("%.1f", minVal), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        Text(String.format("%.1f", maxVal), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun DiagnosisSection(
    results: List<DiagnosisResult>,
    equipmentId: Long,
    equipmentName: String,
    onCreateWorkOrder: ((Long, String, String) -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text("诊断建议", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE65100).copy(alpha = 0.15f)) {
                    Text("${results.size} 条匹配",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        fontSize = 12.sp, color = Color(0xFFE65100))
                }
            }

            results.forEachIndexed { index, result ->
                if (index > 0) HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFFFE0B2)
                )
                Spacer(Modifier.height(4.dp))

                val sevColor = if (result.severityLevel >= 2) Color(0xFFE53935) else Color(0xFFE65100)
                val alternatives = parseAlternatives(result.alternativeActions)
                var expanded by remember { mutableStateOf(false) }

                Text(result.name, fontWeight = FontWeight.SemiBold,
                    color = sevColor, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text("可能原因: ${result.possibleCause}",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text("建议措施: ${result.recommendedAction}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (result.verifiedCount > 0) {
                    Spacer(Modifier.height(2.dp))
                    Surface(shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF43A047).copy(alpha = 0.1f)) {
                        Text("已验证 ${result.verifiedCount} 次",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            fontSize = 11.sp, color = Color(0xFF2E7D32))
                    }
                }

                // Expandable alternative solutions
                if (alternatives.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.clickable { expanded = !expanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1565C0).copy(alpha = 0.1f)) {
                            Text("${alternatives.size} 种替代方案",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 12.sp, color = Color(0xFF1565C0))
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            "展开", modifier = Modifier.size(18.dp),
                            tint = Color(0xFF1565C0)
                        )
                    }
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            alternatives.forEachIndexed { ai, alt ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE3F2FD)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("方案 ${ai + 1}: ${alt["action"] ?: ""}",
                                            fontSize = 12.sp,
                                            style = MaterialTheme.typography.bodySmall)
                                        Text("诊断: ${alt["diagnosis"] ?: ""}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("维修人: ${alt["engineer"] ?: ""} · ${alt["date"] ?: ""}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (onCreateWorkOrder != null) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        val desc = results.firstOrNull()?.let {
                            "${it.name}: ${it.recommendedAction}"
                        } ?: ""
                        val category = results.firstOrNull()?.faultCategory ?: ""
                        onCreateWorkOrder(equipmentId, desc, category)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("根据诊断建议创建工单")
                }
            }
        }
    }
}

private fun parseAlternatives(json: String?): List<Map<String, String>> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val gson = com.google.gson.Gson()
        val type = object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type
        gson.fromJson(json, type)
    } catch (e: Exception) {
        emptyList()
    }
}

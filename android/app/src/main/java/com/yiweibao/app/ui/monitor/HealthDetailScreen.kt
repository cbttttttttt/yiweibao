package com.yiweibao.app.ui.monitor

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yiweibao.app.data.model.FactorDetail
import com.yiweibao.app.data.model.MachineData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDetailScreen(
    equipmentId: Long,
    equipmentName: String,
    onBack: () -> Unit,
    viewModel: HealthDetailViewModel = viewModel()
) {
    LaunchedEffect(equipmentId) {
        viewModel.load(equipmentId)
    }

    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$equipmentName 健康详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.load(equipmentId) }) { Text("重试") }
                }
            }
            state.detail != null -> {
                val detail = state.detail!!
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TotalScoreCard(detail.totalScore, detail.status, detail.rul)

                    Text("因子扣分明细", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    detail.factors.forEach { factor ->
                        FactorCard(factor)
                    }

                    if (state.trendData.isNotEmpty()) {
                        Text("30分钟趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TrendChart(state.trendData, detail)
                    }
                }
            }
        }
    }
}

@Composable
fun TotalScoreCard(totalScore: Double, status: String, rul: String) {
    val scoreColor = when {
        totalScore >= 80 -> Color(0xFF43A047)
        totalScore >= 60 -> Color(0xFFFB8C00)
        else -> Color(0xFFE53935)
    }

    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { (totalScore / 100.0).toFloat() },
                    modifier = Modifier.fillMaxSize(),
                    color = scoreColor,
                    strokeWidth = 8.dp,
                    trackColor = scoreColor.copy(alpha = 0.12f),
                    strokeCap = StrokeCap.Round,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${totalScore.toInt()}", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = scoreColor)
                    Text("分", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = scoreColor.copy(alpha = 0.12f)) {
                Text(status, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontWeight = FontWeight.SemiBold, color = scoreColor)
            }
            Spacer(Modifier.height(8.dp))
            Text(rul, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun FactorCard(factor: FactorDetail) {
    val levelColor = if (factor.level == "warning") Color(0xFFE53935) else Color(0xFF43A047)
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(factor.name, fontWeight = FontWeight.SemiBold)
                Text(factor.reference, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${"%.1f".format(factor.value)} ${factor.unit}",
                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(factor.level, style = MaterialTheme.typography.labelSmall, color = levelColor)
            }
        }
    }
}

@Composable
fun TrendChart(data: List<MachineData>, detail: com.yiweibao.app.data.model.HealthDetail) {
    val vibColor = Color(0xFFE53935)
    val tempColor = Color(0xFFFF9800)

    Card(Modifier.fillMaxWidth().height(180.dp)) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            if (data.size < 2) return@Canvas
            val w = size.width
            val h = size.height
            val vibMax = data.maxOf { it.vibration } * 1.2
            val tempMax = data.maxOf { it.temperature } * 1.2
            val vibMin = 0.0
            val tempMin = data.minOf { it.temperature } * 0.8
            val vibRange = if (vibMax - vibMin > 0) vibMax - vibMin else 1.0
            val tempRange = if (tempMax - tempMin > 0) tempMax - tempMin else 1.0

            for (i in 1 until data.size) {
                val x1 = (i - 1).toFloat() / (data.size - 1) * w
                val x2 = i.toFloat() / (data.size - 1) * w
                val y1v = h - ((data[i - 1].vibration - vibMin) / vibRange * h).toFloat()
                val y2v = h - ((data[i].vibration - vibMin) / vibRange * h).toFloat()
                drawLine(vibColor, Offset(x1, y1v), Offset(x2, y2v), strokeWidth = 2.5f)

                val y1t = h - ((data[i - 1].temperature - tempMin) / tempRange * h).toFloat()
                val y2t = h - ((data[i].temperature - tempMin) / tempRange * h).toFloat()
                drawLine(tempColor, Offset(x1, y1t), Offset(x2, y2t), strokeWidth = 2.5f)
            }
        }
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(10.dp)) { drawCircle(vibColor, 5f, center = Offset(5f, 5f)) }
            Spacer(Modifier.width(4.dp))
            Text("振动", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.width(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(10.dp)) { drawCircle(tempColor, 5f, center = Offset(5f, 5f)) }
            Spacer(Modifier.width(4.dp))
            Text("温度", style = MaterialTheme.typography.labelSmall)
        }
    }
}

package com.yiweibao.app.ui.knowledge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yiweibao.app.data.model.DiagnosisResult
import com.yiweibao.app.data.repository.DiagnosisRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(onBack: () -> Unit) {
    var rules by remember { mutableStateOf<List<DiagnosisResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            isLoading = true; error = null
            try {
                val result = DiagnosisRepository().getRules()
                rules = result.data ?: emptyList()
            } catch (e: Exception) {
                error = "加载失败: ${e.message}"
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("诊断知识库") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { load() }) { Text("重试") }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("共 ${rules.size} 条规则", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.weight(1f))
                            Surface(shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2E7D32).copy(alpha = 0.15f)) {
                                Text("预置+学习", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
                itemsIndexed(rules) { index, rule ->
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp),
                                    color = if (rule.severityLevel >= 2)
                                        Color(0xFFE53935).copy(alpha = 0.15f)
                                    else Color(0xFFFFA726).copy(alpha = 0.15f)) {
                                    Text(
                                        if (rule.severityLevel >= 2) "告警" else "预警",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        color = if (rule.severityLevel >= 2) Color(0xFFE53935) else Color(0xFFE65100)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("#${index + 1} ${rule.name}", fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f))
                                if (rule.verifiedCount > 0) {
                                    Icon(Icons.Default.CheckCircle, null,
                                        modifier = Modifier.size(16.dp), tint = Color(0xFF43A047))
                                    Spacer(Modifier.width(2.dp))
                                    Text("${rule.verifiedCount}", fontSize = 12.sp, color = Color(0xFF43A047))
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Surface(shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                                Text(rule.symptomDescription, modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("分类: ${rule.faultCategory}", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary)
                            Text("原因: ${rule.possibleCause}", fontSize = 12.sp,
                                style = MaterialTheme.typography.bodySmall)
                            Text("措施: ${rule.recommendedAction}", fontSize = 12.sp,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)

                            // Expandable alternatives
                            val alternatives = parseAlts(rule.alternativeActions)
                            if (alternatives.isNotEmpty()) {
                                var expanded by remember { mutableStateOf(false) }
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.clickable { expanded = !expanded },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF1565C0).copy(alpha = 0.1f)) {
                                        Text("${alternatives.size} 种替代方案",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 11.sp, color = Color(0xFF1565C0))
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Icon(
                                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        "展开", modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF1565C0)
                                    )
                                }
                                AnimatedVisibility(visible = expanded,
                                    enter = expandVertically(), exit = shrinkVertically()
                                ) {
                                    Column(modifier = Modifier.padding(top = 4.dp)) {
                                        alternatives.forEachIndexed { ai, alt ->
                                            Surface(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFE3F2FD)
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text("方案 ${ai + 1}: ${alt["action"] ?: ""}",
                                                        fontSize = 11.sp,
                                                        style = MaterialTheme.typography.bodySmall)
                                                    Text("诊断: ${alt["diagnosis"] ?: ""}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("${alt["engineer"] ?: ""} · ${alt["date"] ?: ""}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.outline)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseAlts(json: String?): List<Map<String, String>> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val gson = com.google.gson.Gson()
        val type = object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type
        gson.fromJson(json, type)
    } catch (e: Exception) {
        emptyList()
    }
}

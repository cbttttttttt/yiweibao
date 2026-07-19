package com.yiweibao.app.ui.knowledge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import com.yiweibao.app.data.model.DiagnosisResult
import com.yiweibao.app.data.repository.DiagnosisRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(onBack: () -> Unit) {
    var rules by remember { mutableStateOf<List<DiagnosisResult>>(emptyList()) }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val repo = remember { DiagnosisRepository() }

    fun load() {
        scope.launch {
            isLoading = true; error = null
            try {
                val catResult = repo.getCategories()
                categories = catResult.data ?: emptyList()
                val rulesResult = repo.getRules()
                rules = rulesResult.data ?: emptyList()
            } catch (e: Exception) {
                error = "加载失败: ${e.message}"
            }
            isLoading = false
        }
    }

    fun search() {
        scope.launch {
            isLoading = true; error = null
            try {
                val result = if (searchQuery.isBlank() && selectedCategory == null) {
                    repo.getRules()
                } else if (searchQuery.isBlank() && selectedCategory != null) {
                    repo.getRulesByCategory(selectedCategory!!)
                } else {
                    repo.searchRules(searchQuery, selectedCategory)
                }
                rules = result.data ?: emptyList()
            } catch (e: Exception) {
                error = "搜索失败: ${e.message}"
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
        Column(modifier = Modifier.padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索故障现象、原因、措施...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            load()
                        }) { Icon(Icons.Default.Close, "清除") }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category filter chips
            if (categories.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null; search() },
                        label = { Text("全部") }
                    )
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = if (selectedCategory == cat) null else cat
                                search()
                            },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Results
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { load() }) { Text("重试") }
                    }
                }
                else -> LazyColumn(
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
                                if (searchQuery.isNotEmpty() || selectedCategory != null) {
                                    Spacer(Modifier.width(8.dp))
                                    Text("(已筛选)", fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary)
                                }
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
                        KnowledgeRuleCard(rule, index)
                    }
                    if (rules.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SearchOff, null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.height(8.dp))
                                    Text("未找到匹配的诊断规则", color = MaterialTheme.colorScheme.outline)
                                    Text("尝试修改搜索关键词或筛选条件", fontSize = 12.sp,
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

@Composable
private fun KnowledgeRuleCard(rule: DiagnosisResult, index: Int) {
    var expanded by remember { mutableStateOf(false) }
    var showAlts by remember { mutableStateOf(false) }
    val alternatives = parseAlts(rule.alternativeActions)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row
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
                Spacer(Modifier.width(4.dp))
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    "展开", modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.outline)
            }

            Spacer(Modifier.height(6.dp))

            // Symptom description
            Surface(shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Text(rule.symptomDescription, modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(6.dp))

            // Quick info row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                    Text(rule.faultCategory,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
                rule.applicableModels?.let { models ->
                    if (models.isNotBlank()) {
                        Text("适用: $models", fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1)
                    }
                }
            }

            // Expanded detail section
            AnimatedVisibility(visible = expanded,
                enter = expandVertically(), exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Cause
                    DetailSection("故障原因", rule.possibleCause)

                    // Action
                    DetailSection("维修措施", rule.recommendedAction)

                    // Repair steps
                    val steps = parseSteps(rule.repairSteps)
                    if (steps.isNotEmpty()) {
                        Text("维修步骤", fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                        steps.forEachIndexed { si, step ->
                            Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp)) {
                                Surface(shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(20.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${si + 1}", fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(step, fontSize = 12.sp,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Tools
                    rule.toolsRequired?.let { tools ->
                        if (tools.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(top = 8.dp)) {
                                Icon(Icons.Default.Build, null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(4.dp))
                                Text("所需工具: $tools", fontSize = 12.sp,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Safety notes
                    rule.safetyNotes?.let { notes ->
                        if (notes.isNotBlank()) {
                            Row(verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Default.Warning, null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFE65100))
                                Spacer(Modifier.width(4.dp))
                                Text("安全须知: $notes", fontSize = 12.sp,
                                    color = Color(0xFFE65100),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Estimated time
                    rule.estimatedHours?.let { hours ->
                        if (hours > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Default.Schedule, null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.width(4.dp))
                                Text("预估工时: ${hours}小时", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    // Keywords
                    rule.keywords?.let { kws ->
                        if (kws.isNotBlank()) {
                            Row(modifier = Modifier.padding(top = 4.dp)) {
                                Text("关键词: ", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline)
                                Text(kws, fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1)
                            }
                        }
                    }

                    // Alternatives
                    if (alternatives.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.clickable { showAlts = !showAlts },
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
                                if (showAlts) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                "展开", modifier = Modifier.size(16.dp),
                                tint = Color(0xFF1565C0)
                            )
                        }
                        AnimatedVisibility(visible = showAlts,
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

@Composable
private fun DetailSection(title: String, content: String) {
    if (content.isNotBlank()) {
        Text(title, fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
        Text(content, fontSize = 12.sp,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp, top = 2.dp))
    }
}

private fun parseSteps(json: String?): List<String> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val gson = com.google.gson.Gson()
        val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
        gson.fromJson(json, type)
    } catch (e: Exception) {
        emptyList()
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

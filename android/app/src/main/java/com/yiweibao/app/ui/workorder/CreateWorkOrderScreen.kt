package com.yiweibao.app.ui.workorder

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.yiweibao.app.data.model.Equipment
import com.yiweibao.app.data.model.WorkOrderCreateRequest
import com.yiweibao.app.data.repository.EquipmentRepository
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkOrderScreen(
    preselectedEquipmentId: Long? = null,
    preselectedFaultDesc: String? = null,
    preselectedFaultCategory: String? = null,
    onBack: () -> Unit,
    viewModel: WorkOrderViewModel = viewModel()
) {
    var equipmentList by remember { mutableStateOf<List<Equipment>>(emptyList()) }
    var selectedEquipmentId by remember { mutableStateOf(preselectedEquipmentId) }
    var faultDesc by remember { mutableStateOf(preselectedFaultDesc ?: "") }
    var faultCategory by remember { mutableStateOf(preselectedFaultCategory ?: "") }
    var urgency by remember { mutableIntStateOf(0) }
    var reporter by remember { mutableStateOf("") }
    val actionState by viewModel.actionState.collectAsState()
    var equipmentDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val photos by viewModel.photos.collectAsState()

    // Camera URI holder
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { viewModel.addPhoto(it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.addPhoto(it) }
    }

    LaunchedEffect(Unit) {
        val repo = EquipmentRepository()
        val r = repo.getList()
        if (r.code == 200 && r.data != null) {
            equipmentList = r.data.content
        }
    }
    LaunchedEffect(actionState.success) {
        if (actionState.success) {
            viewModel.resetActionState()
            onBack()
        }
    }

    val selectedName = equipmentList.find { it.id == selectedEquipmentId }?.name ?: "请选择设备"

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("故障报修") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Equipment selector dropdown
            ExposedDropdownMenuBox(expanded = equipmentDropdownExpanded, onExpandedChange = { equipmentDropdownExpanded = it }) {
                OutlinedTextField(
                    value = selectedName, onValueChange = {},
                    readOnly = true, label = { Text("选择设备*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = equipmentDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = equipmentDropdownExpanded, onDismissRequest = { equipmentDropdownExpanded = false }) {
                    equipmentList.forEach { eq ->
                        DropdownMenuItem(text = { Text("${eq.name} (${eq.code})") },
                            onClick = {
                                selectedEquipmentId = eq.id
                                equipmentDropdownExpanded = false
                            })
                    }
                }
            }

            OutlinedTextField(value = reporter, onValueChange = { reporter = it },
                label = { Text("报修人*") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            OutlinedTextField(value = faultDesc, onValueChange = { faultDesc = it },
                label = { Text("故障描述*") }, modifier = Modifier.fillMaxWidth(),
                minLines = 3, maxLines = 5)

            // Fault category dropdown
            val categories = listOf("机械故障", "电气故障", "液压故障", "温控故障", "传动故障", "控制系统故障", "其他")
            ExposedDropdownMenuBox(expanded = categoryDropdownExpanded, onExpandedChange = { categoryDropdownExpanded = it }) {
                OutlinedTextField(
                    value = faultCategory.ifBlank { "请选择故障分类" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("故障分类*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = categoryDropdownExpanded, onDismissRequest = { categoryDropdownExpanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) },
                            onClick = {
                                faultCategory = cat
                                categoryDropdownExpanded = false
                            })
                    }
                }
            }

            Text("紧急程度", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("普通" to 0, "紧急" to 1, "特急" to 2).forEach { (label, value) ->
                    FilterChip(
                        selected = urgency == value,
                        onClick = { urgency = value },
                        label = { Text(label) }
                    )
                }
            }

            // Photo section
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text("机床现状照片（可选）", style = MaterialTheme.typography.bodyMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Camera button
                OutlinedButton(onClick = {
                    val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    cameraUri = uri
                    cameraLauncher.launch(uri)
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("拍照")
                }
                // Gallery button
                OutlinedButton(onClick = {
                    galleryLauncher.launch("image/*")
                }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("从相册选择")
                }
            }

            // Photo thumbnails
            if (photos.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(photos) { index, item ->
                        Box(modifier = Modifier.size(80.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(item.uri),
                                contentDescription = "照片 ${index + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Delete button
                            IconButton(
                                onClick = { viewModel.removePhoto(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(22.dp)
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer,
                                        RoundedCornerShape(bottomStart = 8.dp)
                                    )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "删除",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }

            if (actionState.error != null) {
                Text(actionState.error!!, color = MaterialTheme.colorScheme.error)
            }

            Button(onClick = {
                viewModel.createWorkOrder(
                    WorkOrderCreateRequest(
                        equipmentId = selectedEquipmentId ?: 0,
                        faultDesc = faultDesc,
                        faultCategory = faultCategory,
                        urgency = urgency,
                        reporter = reporter
                    )
                )
            }, modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !actionState.isProcessing && selectedEquipmentId != null && faultCategory.isNotBlank()
                        && faultDesc.isNotBlank() && reporter.isNotBlank()
            ) {
                if (actionState.isProcessing) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                else Text("提交报修")
            }
        }
    }
}

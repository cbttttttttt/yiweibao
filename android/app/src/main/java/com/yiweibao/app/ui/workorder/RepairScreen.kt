package com.yiweibao.app.ui.workorder

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.yiweibao.app.data.model.WorkOrderCompleteRequest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairScreen(
    id: Long, onBack: () -> Unit,
    viewModel: WorkOrderViewModel = viewModel()
) {
    var diagnosis by remember { mutableStateOf("") }
    var repairAction by remember { mutableStateOf("") }
    var replacedParts by remember { mutableStateOf("") }
    val actionState by viewModel.actionState.collectAsState()

    val context = LocalContext.current
    val photos by viewModel.photos.collectAsState()

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

    LaunchedEffect(id) { viewModel.clearPhotos() }
    LaunchedEffect(actionState.success) {
        if (actionState.success) {
            viewModel.resetActionState()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("填写维修记录") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            OutlinedTextField(value = diagnosis, onValueChange = { diagnosis = it },
                label = { Text("故障诊断*（不少于10字）") },
                modifier = Modifier.fillMaxWidth(), minLines = 3)

            OutlinedTextField(value = repairAction, onValueChange = { repairAction = it },
                label = { Text("维修措施*（不少于20字）") },
                modifier = Modifier.fillMaxWidth(), minLines = 3)

            OutlinedTextField(value = replacedParts, onValueChange = { replacedParts = it },
                label = { Text("更换备件（选填）") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)

            // Photo section
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text("维修后照片（可选）", style = MaterialTheme.typography.bodyMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                OutlinedButton(onClick = {
                    galleryLauncher.launch("image/*")
                }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("从相册选择")
                }
            }

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

            Button(
                onClick = {
                    viewModel.complete(id, WorkOrderCompleteRequest(
                        diagnosis = diagnosis,
                        repairAction = repairAction,
                        replacedParts = replacedParts.ifBlank { null }
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !actionState.isProcessing && diagnosis.length >= 10 && repairAction.length >= 20
            ) {
                if (actionState.isProcessing) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                else Text("提交维修记录")
            }

            Text("诊断不少于10字，维修措施不少于20字",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

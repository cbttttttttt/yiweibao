package com.yiweibao.app.ui.equipment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yiweibao.app.data.model.EquipmentRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentFormScreen(
    editId: Long? = null, onBack: () -> Unit,
    viewModel: EquipmentViewModel = viewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val detailState by viewModel.detailState.collectAsState()
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var spec by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var workshop by remember { mutableStateOf("") }
    var manager by remember { mutableStateOf("") }
    var purchaseDateText by remember { mutableStateOf("") }
    var startDateText by remember { mutableStateOf("") }

    if (editId != null) {
        LaunchedEffect(editId) { viewModel.loadDetail(editId) }
        LaunchedEffect(detailState.equipment) {
            detailState.equipment?.let { e ->
                code = e.code
                name = e.name
                model = e.model ?: ""
                spec = e.spec ?: ""
                manufacturer = e.manufacturer ?: ""
                location = e.location ?: ""
                workshop = e.workshop ?: ""
                manager = e.manager ?: ""
                purchaseDateText = e.purchaseDate ?: ""
                startDateText = e.startDate ?: ""
            }
        }
    }

    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (editId != null) "编辑设备" else "新增设备") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("设备编码*") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("设备名称*") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("型号") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = spec, onValueChange = { spec = it }, label = { Text("规格") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = manufacturer, onValueChange = { manufacturer = it }, label = { Text("厂商") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("安装位置") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = workshop, onValueChange = { workshop = it }, label = { Text("所属车间") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = manager, onValueChange = { manager = it }, label = { Text("负责人") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)

            // 购买日期 — DatePicker
            var showPurchasePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = purchaseDateText,
                onValueChange = {},
                readOnly = true,
                label = { Text("购买日期") },
                placeholder = { Text("点击选择日期") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = { IconButton(onClick = { showPurchasePicker = true }) { Icon(Icons.Default.CalendarMonth, "选择日期") } }
            )
            if (showPurchasePicker) {
                val pickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showPurchasePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            pickerState.selectedDateMillis?.let { millis ->
                                purchaseDateText = dateFormat.format(Date(millis))
                            }
                            showPurchasePicker = false
                        }) { Text("确定") }
                    },
                    dismissButton = { TextButton(onClick = { showPurchasePicker = false }) { Text("取消") } }
                ) { DatePicker(state = pickerState) }
            }

            // 启用日期 — DatePicker
            var showStartPicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = startDateText,
                onValueChange = {},
                readOnly = true,
                label = { Text("启用日期") },
                placeholder = { Text("点击选择日期") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = { IconButton(onClick = { showStartPicker = true }) { Icon(Icons.Default.CalendarMonth, "选择日期") } }
            )
            if (showStartPicker) {
                val pickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showStartPicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            pickerState.selectedDateMillis?.let { millis ->
                                startDateText = dateFormat.format(Date(millis))
                            }
                            showStartPicker = false
                        }) { Text("确定") }
                    },
                    dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("取消") } }
                ) { DatePicker(state = pickerState) }
            }

            if (formState.error != null) {
                Text(formState.error!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    viewModel.saveEquipment(
                        EquipmentRequest(
                            code = code, name = name, model = model, spec = spec,
                            manufacturer = manufacturer, location = location,
                            workshop = workshop, manager = manager,
                            purchaseDate = purchaseDateText.ifBlank { null },
                            startDate = startDateText.ifBlank { null }
                        ), editId
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !formState.isSaving
            ) {
                if (formState.isSaving) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                else Text("保存")
            }
        }
    }
}

package com.yiweibao.app.navigation

import android.content.Intent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.yiweibao.app.ui.home.HomeScreen
import com.yiweibao.app.ui.login.LoginScreen
import com.yiweibao.app.ui.equipment.EquipmentListScreen
import com.yiweibao.app.ui.equipment.EquipmentDetailScreen
import com.yiweibao.app.ui.equipment.EquipmentFormScreen
import com.yiweibao.app.ui.scanner.ScanScreen
import com.yiweibao.app.ui.workorder.WorkOrderListScreen
import com.yiweibao.app.ui.workorder.WorkOrderDetailScreen
import com.yiweibao.app.ui.workorder.CreateWorkOrderScreen
import com.yiweibao.app.ui.workorder.RepairScreen
import com.yiweibao.app.ui.statistics.StatisticsScreen
import com.yiweibao.app.ui.monitor.MonitorScreen
import com.yiweibao.app.ui.monitor.EquipmentDataScreen
import com.yiweibao.app.ui.knowledge.KnowledgeBaseScreen
import com.yiweibao.app.util.TokenManager
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object EquipmentList : Screen("equipment_list")
    object EquipmentDetail : Screen("equipment_detail/{id}") {
        fun create(id: Long) = "equipment_detail/$id"
    }
    object EquipmentWorkOrders : Screen("equipment_work_orders/{id}/{name}") {
        fun create(id: Long, name: String) = "equipment_work_orders/$id/${java.net.URLEncoder.encode(name, "UTF-8")}"
    }
    object EquipmentForm : Screen("equipment_form?editId={editId}") {
        fun create(editId: Long? = null) = if (editId != null) "equipment_form?editId=$editId" else "equipment_form"
    }
    object WorkOrderList : Screen("workorder_list")
    object WorkOrderDetail : Screen("workorder_detail/{id}") {
        fun create(id: Long) = "workorder_detail/$id"
    }
    object CreateWorkOrder : Screen("workorder_create?equipmentId={equipmentId}&faultDesc={faultDesc}&faultCategory={faultCategory}") {
        fun create(equipmentId: Long? = null, faultDesc: String? = null, faultCategory: String? = null): String {
            val params = mutableListOf<String>()
            equipmentId?.let { params.add("equipmentId=$it") }
            faultDesc?.let { params.add("faultDesc=${java.net.URLEncoder.encode(it, "UTF-8")}") }
            faultCategory?.let { params.add("faultCategory=${java.net.URLEncoder.encode(it, "UTF-8")}") }
            return if (params.isEmpty()) "workorder_create" else "workorder_create?${params.joinToString("&")}"
        }
    }
    object Repair : Screen("repair/{id}") {
        fun create(id: Long) = "repair/$id"
    }
    object Statistics : Screen("statistics")
    object Scan : Screen("scan")
    object KnowledgeBase : Screen("knowledge_base")
    object Monitor : Screen("monitor")
    object EquipmentData : Screen("equipment_data/{id}/{name}") {
        fun create(id: Long, name: String) = "equipment_data/$id/${java.net.URLEncoder.encode(name, "UTF-8")}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YiweibaoNavGraph(intent: Intent? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()
    var realName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        realName = tokenManager.getRealName() ?: ""
    }

    val onLogout: () -> Unit = {
        scope.launch {
            tokenManager.clear()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    fun navigateTo(route: String) {
        navController.navigate(route)
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(realName = realName, onNavigate = ::navigateTo, onLogout = onLogout)
        }

        // === Equipment ===

        composable(Screen.EquipmentList.route) {
            EquipmentListScreen(
                onDetailClick = { navController.navigate(Screen.EquipmentDetail.create(it)) },
                onAddClick = { navController.navigate(Screen.EquipmentForm.create()) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EquipmentDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink { uriPattern = "yiweibao://equipment/{id}" })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            EquipmentDetailScreen(id = id, onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.EquipmentForm.create(id)) },
                onCreateOrder = { navController.navigate(Screen.CreateWorkOrder.create(id)) },
                onViewOrders = { name -> navController.navigate(Screen.EquipmentWorkOrders.create(id, name)) })
        }

        composable(
            route = Screen.EquipmentForm.route,
            arguments = listOf(navArgument("editId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val editId = backStackEntry.arguments?.getLong("editId")?.takeIf { it > 0 }
            EquipmentFormScreen(editId = editId, onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.EquipmentWorkOrders.route,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val equipmentId = backStackEntry.arguments?.getLong("id") ?: 0L
            val equipmentName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("name") ?: "设备", "UTF-8")
            WorkOrderListScreen(
                equipmentId = equipmentId,
                title = "$equipmentName 维修记录",
                onDetailClick = { navController.navigate(Screen.WorkOrderDetail.create(it)) },
                onCreateClick = {},
                onBack = { navController.popBackStack() }
            )
        }

        // === Work Orders ===

        composable(Screen.WorkOrderList.route) {
            WorkOrderListScreen(
                onDetailClick = { navController.navigate(Screen.WorkOrderDetail.create(it)) },
                onCreateClick = { navController.navigate(Screen.CreateWorkOrder.create()) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WorkOrderDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            WorkOrderDetailScreen(id = id, onBack = { navController.popBackStack() },
                onAccept = {}, onRepair = { navController.navigate(Screen.Repair.create(id)) })
        }

        composable(
            route = Screen.CreateWorkOrder.route,
            arguments = listOf(
                navArgument("equipmentId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("faultDesc") { type = NavType.StringType; defaultValue = "" },
                navArgument("faultCategory") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val equipmentId = backStackEntry.arguments?.getLong("equipmentId")?.takeIf { it > 0 }
            val faultDesc = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("faultDesc") ?: "", "UTF-8")
            val faultCategory = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("faultCategory") ?: "", "UTF-8")
            CreateWorkOrderScreen(
                preselectedEquipmentId = equipmentId,
                preselectedFaultDesc = faultDesc,
                preselectedFaultCategory = faultCategory,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Repair.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            RepairScreen(id = id, onBack = { navController.popBackStack() })
        }

        // === Statistics ===

        composable(Screen.Statistics.route) {
            StatisticsScreen(onBack = { navController.popBackStack() })
        }

        // === Machine Data Monitor ===

        composable(Screen.Monitor.route) {
            MonitorScreen(
                onBack = { navController.popBackStack() },
                onEquipmentClick = { id, name ->
                    navController.navigate(Screen.EquipmentData.create(id, name))
                },
                onHealthDetailClick = { _, _ -> }  // placeholder until Task 10
            )
        }

        composable(
            route = Screen.EquipmentData.route,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            val name = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("name") ?: "设备", "UTF-8")
            EquipmentDataScreen(
                equipmentId = id,
                equipmentName = name,
                onBack = { navController.popBackStack() },
                onCreateWorkOrder = { eqId, desc, cat ->
                    navController.navigate(Screen.CreateWorkOrder.create(eqId, desc, cat))
                }
            )
        }

        // === Scan ===

        composable(Screen.KnowledgeBase.route) {
            KnowledgeBaseScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Scan.route) {
            ScanScreen(
                onBack = { navController.popBackStack() },
                onScanned = { id ->
                    navController.navigate(Screen.EquipmentDetail.create(id)) {
                        popUpTo(Screen.Scan.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

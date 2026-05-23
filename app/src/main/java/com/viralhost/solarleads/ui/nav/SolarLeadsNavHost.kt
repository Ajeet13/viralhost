package com.viralhost.solarleads.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.viralhost.solarleads.ui.detail.LeadDetailScreen
import com.viralhost.solarleads.ui.edit.AddEditLeadScreen
import com.viralhost.solarleads.ui.import_.ImportScreen
import com.viralhost.solarleads.ui.list.LeadListScreen

object Routes {
    const val LIST = "list"
    const val IMPORT = "import"
    const val ADD_EDIT = "addEdit/{leadId}"
    const val DETAIL = "detail/{leadId}"

    fun addEdit(leadId: Long = 0L) = "addEdit/$leadId"
    fun detail(leadId: Long) = "detail/$leadId"
}

@Composable
fun SolarLeadsNavHost(initialLeadId: Long? = null) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            LeadListScreen(
                onAdd = { nav.navigate(Routes.addEdit()) },
                onOpen = { id -> nav.navigate(Routes.detail(id)) },
                onImport = { nav.navigate(Routes.IMPORT) }
            )
        }

        composable(Routes.IMPORT) {
            ImportScreen(onBack = { nav.popBackStack() })
        }

        composable(
            Routes.ADD_EDIT,
            arguments = listOf(navArgument("leadId") { type = NavType.LongType; defaultValue = 0L })
        ) { entry ->
            val leadId = entry.arguments?.getLong("leadId") ?: 0L
            AddEditLeadScreen(
                leadId = leadId,
                onBack = { nav.popBackStack() },
                onSaved = { id ->
                    if (leadId == 0L) {
                        // After creating, replace this screen with detail
                        nav.popBackStack()
                        nav.navigate(Routes.detail(id))
                    } else {
                        nav.popBackStack()
                    }
                }
            )
        }

        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("leadId") { type = NavType.LongType })
        ) { entry ->
            val leadId = entry.arguments?.getLong("leadId") ?: 0L
            LeadDetailScreen(
                leadId = leadId,
                onBack = { nav.popBackStack() },
                onEdit = { id -> nav.navigate(Routes.addEdit(id)) }
            )
        }
    }

    // Deep-link from notification tap
    LaunchedEffect(initialLeadId) {
        if (initialLeadId != null && initialLeadId > 0L) {
            nav.navigate(Routes.detail(initialLeadId))
        }
    }
}

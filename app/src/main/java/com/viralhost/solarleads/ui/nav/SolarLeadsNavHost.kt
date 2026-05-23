package com.viralhost.solarleads.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.viralhost.solarleads.ui.analytics.AnalyticsScreen
import com.viralhost.solarleads.ui.detail.LeadDetailScreen
import com.viralhost.solarleads.ui.edit.AddEditLeadScreen
import com.viralhost.solarleads.ui.import_.ImportScreen
import com.viralhost.solarleads.ui.list.LeadListScreen
import com.viralhost.solarleads.ui.templates.TemplatesScreen
import com.viralhost.solarleads.ui.today.TodayScreen

object Routes {
    const val LEADS = "leads"
    const val TODAY = "today"
    const val ANALYTICS = "analytics"

    const val IMPORT = "import"
    const val TEMPLATES = "templates"
    const val ADD_EDIT = "addEdit/{leadId}"
    const val DETAIL = "detail/{leadId}"

    fun addEdit(leadId: Long = 0L) = "addEdit/$leadId"
    fun detail(leadId: Long) = "detail/$leadId"
}

private data class TopLevelDest(val route: String, val icon: ImageVector, val label: String)

private val TOP_LEVEL = listOf(
    TopLevelDest(Routes.LEADS, Icons.Filled.People, "Leads"),
    TopLevelDest(Routes.TODAY, Icons.Filled.Today, "Today"),
    TopLevelDest(Routes.ANALYTICS, Icons.Filled.BarChart, "Analytics")
)

@Composable
fun SolarLeadsNavHost(initialLeadId: Long? = null) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in TOP_LEVEL.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TOP_LEVEL.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                if (currentRoute != dest.route) {
                                    nav.navigate(dest.route) {
                                        popUpTo(Routes.LEADS) { inclusive = false; saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.LEADS,
            modifier = Modifier.padding(padding)
        ) {

            composable(Routes.LEADS) {
                LeadListScreen(
                    onAdd = { nav.navigate(Routes.addEdit()) },
                    onOpen = { id -> nav.navigate(Routes.detail(id)) },
                    onImport = { nav.navigate(Routes.IMPORT) },
                    onOpenTemplates = { nav.navigate(Routes.TEMPLATES) }
                )
            }

            composable(Routes.TODAY) {
                TodayScreen(onOpenLead = { id -> nav.navigate(Routes.detail(id)) })
            }

            composable(Routes.ANALYTICS) {
                AnalyticsScreen()
            }

            composable(Routes.IMPORT) {
                ImportScreen(onBack = { nav.popBackStack() })
            }

            composable(Routes.TEMPLATES) {
                TemplatesScreen(onBack = { nav.popBackStack() })
            }

            composable(
                Routes.ADD_EDIT,
                arguments = listOf(navArgument("leadId") {
                    type = NavType.LongType; defaultValue = 0L
                })
            ) { entry ->
                val leadId = entry.arguments?.getLong("leadId") ?: 0L
                AddEditLeadScreen(
                    leadId = leadId,
                    onBack = { nav.popBackStack() },
                    onSaved = { id ->
                        if (leadId == 0L) {
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
    }

    LaunchedEffect(initialLeadId) {
        if (initialLeadId != null && initialLeadId > 0L) {
            nav.navigate(Routes.detail(initialLeadId))
        }
    }
}

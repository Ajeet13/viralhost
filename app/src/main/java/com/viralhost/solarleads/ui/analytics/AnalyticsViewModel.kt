package com.viralhost.solarleads.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viralhost.solarleads.SolarLeadsApp
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.RoofType
import com.viralhost.solarleads.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AnalyticsUiState(
    val totalLeads: Int = 0,
    val totalCalls: Int = 0,
    val pendingCallbacks: Int = 0,
    val convertedCount: Int = 0,
    val conversionRatePct: Int = 0,
    val statusCounts: Map<LeadStatus, Int> = emptyMap(),
    val roofCounts: Map<RoofType, Int> = emptyMap(),
    val callsLast7Days: List<Pair<String, Int>> = emptyList()
)

class AnalyticsViewModel(
    app: Application,
    private val repo: LeadRepository
) : AndroidViewModel(app) {

    private val callsLast7Days = MutableStateFlow<List<Pair<String, Int>>>(emptyList())

    val uiState: StateFlow<AnalyticsUiState> = combine(
        repo.observeAll(),
        callsLast7Days
    ) { leads, calls ->
        val statusCounts = LeadStatus.values().associateWith { s -> leads.count { it.status == s } }
        val roofCounts = RoofType.values().associateWith { r -> leads.count { it.roofType == r } }
        val converted = statusCounts[LeadStatus.CONVERTED] ?: 0
        val pending = statusCounts[LeadStatus.CALLBACK_SCHEDULED] ?: 0
        val total = leads.size
        val rate = if (total == 0) 0 else (converted * 100 / total)
        AnalyticsUiState(
            totalLeads = total,
            totalCalls = calls.sumOf { it.second },
            pendingCallbacks = pending,
            convertedCount = converted,
            conversionRatePct = rate,
            statusCounts = statusCounts,
            roofCounts = roofCounts,
            callsLast7Days = calls
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())

    init {
        refreshCallsHistogram()
    }

    fun refreshCallsHistogram() {
        viewModelScope.launch {
            val sevenDaysAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -6)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val start = sevenDaysAgo.timeInMillis
            val all = repo.allCallLogsSince(start)

            // bucket by yyyy-MM-dd
            val df = SimpleDateFormat("EEE", Locale.getDefault())
            val buckets = LinkedHashMap<String, Int>()
            for (i in 0..6) {
                val c = Calendar.getInstance().apply {
                    timeInMillis = start
                    add(Calendar.DAY_OF_YEAR, i)
                }
                buckets[df.format(c.time)] = 0
            }
            all.forEach { log ->
                val key = df.format(Date(log.calledAt))
                buckets[key] = (buckets[key] ?: 0) + 1
            }
            callsLast7Days.value = buckets.toList()
        }
    }

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repo = (app as SolarLeadsApp).repository
                AnalyticsViewModel(app, repo)
            }
        }
    }
}

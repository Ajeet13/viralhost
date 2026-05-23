package com.viralhost.solarleads.ui.today

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viralhost.solarleads.SolarLeadsApp
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.Reminder
import com.viralhost.solarleads.data.repository.LeadRepository
import com.viralhost.solarleads.reminders.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class TodayItem(val reminder: Reminder, val lead: Lead?)

data class TodayUiState(
    val overdue: List<TodayItem> = emptyList(),
    val today: List<TodayItem> = emptyList()
)

class TodayViewModel(
    app: Application,
    private val repo: LeadRepository
) : AndroidViewModel(app) {

    private fun startOfToday(): Long {
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    private fun endOfToday(): Long = startOfToday() + 24L * 60 * 60 * 1000

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TodayUiState> = repo.observeAll()
        .flatMapLatest { allLeads ->
            val leadMap = allLeads.associateBy { it.id }
            combine(
                repo.observeOverdueReminders(startOfToday()),
                repo.observeRemindersInRange(startOfToday(), endOfToday())
            ) { overdue, today ->
                TodayUiState(
                    overdue = overdue.map { TodayItem(it, leadMap[it.leadId]) },
                    today = today.map { TodayItem(it, leadMap[it.leadId]) }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun markDone(reminderId: Long) = viewModelScope.launch {
        ReminderScheduler.cancel(getApplication(), reminderId)
        repo.markReminderDone(reminderId)
    }

    fun delete(reminderId: Long) = viewModelScope.launch {
        ReminderScheduler.cancel(getApplication(), reminderId)
        repo.deleteReminder(reminderId)
    }

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repo = (app as SolarLeadsApp).repository
                TodayViewModel(app, repo)
            }
        }
    }
}

package com.viralhost.solarleads.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viralhost.solarleads.SolarLeadsApp
import com.viralhost.solarleads.data.model.CallLog
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.Reminder
import com.viralhost.solarleads.data.repository.LeadRepository
import com.viralhost.solarleads.reminders.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LeadDetailUiState(
    val lead: Lead? = null,
    val callLogs: List<CallLog> = emptyList(),
    val reminders: List<Reminder> = emptyList()
)

class LeadDetailViewModel(
    app: Application,
    private val repo: LeadRepository,
    private val leadId: Long
) : AndroidViewModel(app) {

    val uiState: StateFlow<LeadDetailUiState> =
        combine(
            repo.observeLead(leadId),
            repo.observeCallLogs(leadId),
            repo.observeReminders(leadId)
        ) { lead, calls, reminders -> LeadDetailUiState(lead, calls, reminders) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeadDetailUiState())

    val templates: StateFlow<List<com.viralhost.solarleads.data.model.MessageTemplate>> =
        repo.observeTemplates()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateStatus(status: LeadStatus) = viewModelScope.launch {
        repo.updateStatus(leadId, status)
    }

    fun logCall(outcome: String? = null, notes: String? = null) = viewModelScope.launch {
        repo.logCall(leadId, outcome, notes)
    }

    fun updateCallOutcome(callId: Long, outcome: String?, notes: String?) = viewModelScope.launch {
        repo.updateCallOutcome(callId, outcome, notes)
    }

    fun deleteCallLog(id: Long) = viewModelScope.launch { repo.deleteCallLog(id) }

    fun addReminder(triggerAt: Long, message: String?) = viewModelScope.launch {
        val lead = repo.getLead(leadId) ?: return@launch
        val id = repo.addReminder(Reminder(leadId = leadId, triggerAt = triggerAt, message = message))
        val reminder = repo.getReminder(id) ?: return@launch
        ReminderScheduler.schedule(getApplication(), lead, reminder)
        // If user scheduled a callback, set status accordingly
        repo.updateStatus(leadId, LeadStatus.CALLBACK_SCHEDULED)
    }

    fun deleteReminder(id: Long) = viewModelScope.launch {
        ReminderScheduler.cancel(getApplication(), id)
        repo.deleteReminder(id)
    }

    fun deleteLead() = viewModelScope.launch {
        repo.getLead(leadId)?.let { repo.delete(it) }
    }

    companion object {
        const val KEY_LEAD_ID = "leadId"
        val KEY = object : CreationExtras.Key<Long> {}
        fun factory(app: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val leadId = this[KEY] as? Long ?: 0L
                val repo = (app as SolarLeadsApp).repository
                LeadDetailViewModel(app, repo, leadId)
            }
        }
    }
}

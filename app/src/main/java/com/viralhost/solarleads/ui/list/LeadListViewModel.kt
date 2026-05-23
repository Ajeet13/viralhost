package com.viralhost.solarleads.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viralhost.solarleads.SolarLeadsApp
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.MessageTemplate
import com.viralhost.solarleads.data.model.RoofType
import com.viralhost.solarleads.data.repository.LeadRepository
import com.viralhost.solarleads.util.CsvExporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class LeadListUiState(
    val query: String = "",
    val statusFilter: LeadStatus? = null,
    val roofFilter: RoofType? = null,
    val leads: List<Lead> = emptyList(),
    val selectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val templates: List<MessageTemplate> = emptyList(),
    val cloudState: com.viralhost.solarleads.cloud.CloudSync.State =
        com.viralhost.solarleads.cloud.CloudSync.State.DISABLED
)

class LeadListViewModel(
    app: Application,
    private val repo: LeadRepository,
    private val cloudSync: com.viralhost.solarleads.cloud.CloudSync
) : AndroidViewModel(app) {

    private val query = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<LeadStatus?>(null)
    private val roofFilter = MutableStateFlow<RoofType?>(null)
    private val selectionMode = MutableStateFlow(false)
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    private val filters = combine(query, statusFilter, roofFilter) { q, s, r -> Triple(q, s, r) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val leads = filters.flatMapLatest { (q, s, r) -> repo.search(q, s, r) }

    private data class Selection(val mode: Boolean, val ids: Set<Long>)

    private val selection = combine(selectionMode, selectedIds) { mode, ids -> Selection(mode, ids) }

    val uiState: StateFlow<LeadListUiState> = combine(
        filters,
        leads,
        selection,
        repo.observeTemplates(),
        cloudSync.state
    ) { (q, s, r), list, sel, tpl, cloud ->
        LeadListUiState(
            query = q,
            statusFilter = s,
            roofFilter = r,
            leads = list,
            selectionMode = sel.mode,
            selectedIds = sel.ids,
            templates = tpl,
            cloudState = cloud
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeadListUiState())

    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile.asStateFlow()

    fun setQuery(value: String) { query.value = value }
    fun setStatusFilter(value: LeadStatus?) { statusFilter.value = value }
    fun setRoofFilter(value: RoofType?) { roofFilter.value = value }

    fun startSelection(initial: Long) {
        selectionMode.value = true
        selectedIds.value = setOf(initial)
    }

    fun toggleSelection(id: Long) {
        val current = selectedIds.value
        selectedIds.value = if (id in current) current - id else current + id
        if (selectedIds.value.isEmpty()) selectionMode.value = false
    }

    fun clearSelection() {
        selectionMode.value = false
        selectedIds.value = emptySet()
    }

    fun selectAllVisible() {
        val current = uiState.value.leads.map { it.id }.toSet()
        selectedIds.value = current
        selectionMode.value = current.isNotEmpty()
    }

    suspend fun selectedLeads(): List<Lead> = repo.getLeadsByIds(selectedIds.value.toList())

    fun delete(lead: Lead) = viewModelScope.launch { repo.delete(lead) }

    fun exportCsv() = viewModelScope.launch {
        val all = repo.all()
        val file = CsvExporter.exportToFile(getApplication(), all)
        _exportedFile.value = file
    }

    fun consumeExportedFile() { _exportedFile.value = null }

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val solarApp = app as SolarLeadsApp
                LeadListViewModel(app, solarApp.repository, solarApp.cloudSync)
            }
        }
    }
}

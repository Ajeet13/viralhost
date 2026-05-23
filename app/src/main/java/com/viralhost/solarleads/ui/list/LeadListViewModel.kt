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
    val leads: List<Lead> = emptyList()
)

class LeadListViewModel(
    app: Application,
    private val repo: LeadRepository
) : AndroidViewModel(app) {

    private val query = MutableStateFlow("")
    private val statusFilter = MutableStateFlow<LeadStatus?>(null)
    private val roofFilter = MutableStateFlow<RoofType?>(null)

    private val filters = combine(query, statusFilter, roofFilter) { q, s, r -> Triple(q, s, r) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val leads = filters.flatMapLatest { (q, s, r) -> repo.search(q, s, r) }

    val uiState: StateFlow<LeadListUiState> = combine(query, statusFilter, roofFilter, leads) { q, s, r, list ->
        LeadListUiState(q, s, r, list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeadListUiState())

    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile.asStateFlow()

    fun setQuery(value: String) { query.value = value }
    fun setStatusFilter(value: LeadStatus?) { statusFilter.value = value }
    fun setRoofFilter(value: RoofType?) { roofFilter.value = value }

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
                val repo = (app as SolarLeadsApp).repository
                LeadListViewModel(app, repo)
            }
        }
    }
}

package com.viralhost.solarleads.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viralhost.solarleads.SolarLeadsApp
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.RoofType
import com.viralhost.solarleads.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditUiState(
    val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val ivrs: String = "",
    val address: String = "",
    val roofType: RoofType = RoofType.OTHER,
    val systemSizeKw: String = "",
    val status: LeadStatus = LeadStatus.NEW,
    val notes: String = "",
    val isLoading: Boolean = false,
    val savedId: Long? = null,
    val errorMessage: String? = null
)

class AddEditLeadViewModel(
    app: Application,
    private val repo: LeadRepository,
    private val leadId: Long
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(AddEditUiState(isLoading = leadId != 0L))
    val state: StateFlow<AddEditUiState> = _state.asStateFlow()

    init {
        if (leadId != 0L) {
            viewModelScope.launch {
                repo.getLead(leadId)?.let { l ->
                    _state.value = AddEditUiState(
                        id = l.id,
                        name = l.name,
                        phone = l.phone,
                        ivrs = l.ivrs.orEmpty(),
                        address = l.address.orEmpty(),
                        roofType = l.roofType,
                        systemSizeKw = l.systemSizeKw?.toString().orEmpty(),
                        status = l.status,
                        notes = l.notes.orEmpty()
                    )
                }
            }
        }
    }

    fun update(update: (AddEditUiState) -> AddEditUiState) = _state.update(update)

    fun save() {
        val s = _state.value
        if (s.name.isBlank() || s.phone.isBlank()) {
            _state.update { it.copy(errorMessage = "Name and phone are required") }
            return
        }
        viewModelScope.launch {
            val lead = Lead(
                id = s.id,
                name = s.name.trim(),
                phone = s.phone.trim(),
                ivrs = s.ivrs.trim().ifBlank { null },
                address = s.address.trim().ifBlank { null },
                roofType = s.roofType,
                systemSizeKw = s.systemSizeKw.trim().toDoubleOrNull(),
                status = s.status,
                notes = s.notes.trim().ifBlank { null }
            )
            val id = repo.upsert(lead)
            _state.update { it.copy(savedId = id) }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }

    companion object {
        const val KEY_LEAD_ID = "leadId"
        fun factory(app: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val leadId = this[KEY] as? Long ?: 0L
                val repo = (app as SolarLeadsApp).repository
                AddEditLeadViewModel(app, repo, leadId)
            }
        }
        val KEY = object : CreationExtras.Key<Long> {}
    }
}

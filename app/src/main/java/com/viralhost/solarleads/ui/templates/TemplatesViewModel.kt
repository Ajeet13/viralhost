package com.viralhost.solarleads.ui.templates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viralhost.solarleads.SolarLeadsApp
import com.viralhost.solarleads.data.model.MessageTemplate
import com.viralhost.solarleads.data.repository.LeadRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplatesViewModel(
    app: Application,
    private val repo: LeadRepository
) : AndroidViewModel(app) {

    val templates: StateFlow<List<MessageTemplate>> = repo.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun upsert(t: MessageTemplate) = viewModelScope.launch { repo.upsertTemplate(t) }

    fun delete(t: MessageTemplate) = viewModelScope.launch { repo.deleteTemplate(t) }

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repo = (app as SolarLeadsApp).repository
                TemplatesViewModel(app, repo)
            }
        }
    }
}

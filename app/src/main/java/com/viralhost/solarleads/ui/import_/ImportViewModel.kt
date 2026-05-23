package com.viralhost.solarleads.ui.import_

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viralhost.solarleads.SolarLeadsApp
import com.viralhost.solarleads.data.repository.LeadRepository
import com.viralhost.solarleads.util.ExcelImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ImportUiState(
    val isLoading: Boolean = false,
    val previewCount: Int = 0,
    val skipped: Int = 0,
    val imported: Int = 0,
    val errors: List<String> = emptyList(),
    val done: Boolean = false
)

class ImportViewModel(
    app: Application,
    private val repo: LeadRepository
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(ImportUiState())
    val state: StateFlow<ImportUiState> = _state.asStateFlow()

    fun importFromUri(uri: Uri) {
        _state.value = ImportUiState(isLoading = true)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                ExcelImporter.import(getApplication(), uri)
            }
            if (result.errors.isNotEmpty() && result.leads.isEmpty()) {
                _state.value = ImportUiState(
                    isLoading = false,
                    errors = result.errors,
                    done = true
                )
                return@launch
            }
            val ids = withContext(Dispatchers.IO) { repo.insertAll(result.leads) }
            _state.value = ImportUiState(
                isLoading = false,
                previewCount = result.leads.size,
                skipped = result.skipped,
                imported = ids.size,
                errors = result.errors,
                done = true
            )
        }
    }

    fun reset() { _state.value = ImportUiState() }

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repo = (app as SolarLeadsApp).repository
                ImportViewModel(app, repo)
            }
        }
    }
}

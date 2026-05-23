package com.viralhost.solarleads.ui.import_

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBack: () -> Unit,
    vm: ImportViewModel = viewModel(
        factory = ImportViewModel.factory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val pickFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { /* not all providers grant persistable */ }
            vm.importFromUri(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Leads") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Expected columns", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Header row (first row) should include these columns " +
                            "(case-insensitive, order doesn't matter):"
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("- Name (required)")
                    Text("- Phone Number (required)")
                    Text("- IVRS")
                    Text("- Address/City")
                    Text("- Roof Type   (RCC / Tin / Other)")
                    Text("- System Size (kW)")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Supported files: .xlsx, .xls, .csv",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Button(
                onClick = {
                    pickFile.launch(
                        arrayOf(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/vnd.ms-excel",
                            "text/csv",
                            "text/comma-separated-values",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Upload, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick file from device")
            }

            if (state.isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Importing...")
                }
            }

            if (state.done) {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Result", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Imported: ${state.imported}")
                        if (state.skipped > 0) Text("Skipped (missing name/phone): ${state.skipped}")
                        if (state.errors.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text("Errors:", style = MaterialTheme.typography.labelLarge)
                            state.errors.forEach { Text("• $it") }
                        }
                    }
                }
            }
        }
    }
}

package com.viralhost.solarleads.ui.list

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.RoofType
import com.viralhost.solarleads.util.CsvExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadListScreen(
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onImport: () -> Unit,
    vm: LeadListViewModel = viewModel(
        factory = LeadListViewModel.factory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val exportedFile by vm.exportedFile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(exportedFile) {
        val f = exportedFile ?: return@LaunchedEffect
        val intent = CsvExporter.buildShareIntent(context, f)
        context.startActivity(Intent.createChooser(intent, "Share leads CSV"))
        vm.consumeExportedFile()
    }

    var menuOpen by remember { mutableStateOf(false) }
    var filtersOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solar Leads") },
                actions = {
                    IconButton(onClick = { filtersOpen = !filtersOpen }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.Download, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Import from Excel/CSV") },
                            leadingIcon = { Icon(Icons.Filled.Upload, null) },
                            onClick = { menuOpen = false; onImport() }
                        )
                        DropdownMenuItem(
                            text = { Text("Export all to CSV") },
                            leadingIcon = { Icon(Icons.Filled.Download, null) },
                            onClick = { menuOpen = false; vm.exportCsv() }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add Lead") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::setQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                placeholder = { Text("Search by name, phone, IVRS, address") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true
            )

            if (filtersOpen) {
                Spacer(Modifier.height(8.dp))
                FilterRow(
                    status = state.statusFilter,
                    roof = state.roofFilter,
                    onStatusChange = vm::setStatusFilter,
                    onRoofChange = vm::setRoofFilter
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "${state.leads.size} lead(s)",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(4.dp))

            if (state.leads.isEmpty()) {
                EmptyState(onImport = onImport, onAdd = onAdd)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.leads, key = { it.id }) { lead ->
                        LeadRow(lead = lead, onClick = { onOpen(lead.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    status: LeadStatus?,
    roof: RoofType?,
    onStatusChange: (LeadStatus?) -> Unit,
    onRoofChange: (RoofType?) -> Unit
) {
    Column {
        Text("Status", style = MaterialTheme.typography.labelMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AssistChip(
                onClick = { onStatusChange(null) },
                label = { Text("All") },
                leadingIcon = if (status == null) {
                    { Icon(Icons.Filled.FilterList, null) }
                } else null
            )
            LeadStatus.values().take(4).forEach { s ->
                AssistChip(
                    onClick = { onStatusChange(if (status == s) null else s) },
                    label = { Text(s.display) }
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            LeadStatus.values().drop(4).forEach { s ->
                AssistChip(
                    onClick = { onStatusChange(if (status == s) null else s) },
                    label = { Text(s.display) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Roof type", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistChip(onClick = { onRoofChange(null) }, label = { Text("All") })
            RoofType.values().forEach { r ->
                AssistChip(
                    onClick = { onRoofChange(if (roof == r) null else r) },
                    label = { Text(r.display) }
                )
            }
        }
    }
}

@Composable
private fun LeadRow(lead: Lead, onClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    lead.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = lead.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(lead.phone, style = MaterialTheme.typography.bodyMedium)
            if (!lead.ivrs.isNullOrBlank()) {
                Text("IVRS: ${lead.ivrs}", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    lead.address.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${lead.roofType.display}${lead.systemSizeKw?.let { " · ${it} kW" }.orEmpty()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(4.dp))
            androidx.compose.material3.TextButton(onClick = onClick) { Text("Open") }
        }
    }
}

@Composable
private fun StatusBadge(status: LeadStatus) {
    val (bg, fg) = when (status) {
        LeadStatus.NEW -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        LeadStatus.INTERESTED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        LeadStatus.NOT_INTERESTED, LeadStatus.LOST -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        LeadStatus.CONVERTED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    androidx.compose.material3.Surface(
        color = bg,
        contentColor = fg,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            status.display,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyState(onImport: () -> Unit, onAdd: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("No leads yet", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Add your first lead, or import from Excel/CSV.")
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.OutlinedButton(onClick = onImport) {
                Icon(Icons.Filled.Upload, null); Spacer(Modifier.width(6.dp)); Text("Import")
            }
            androidx.compose.material3.Button(onClick = onAdd) {
                Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text("Add lead")
            }
        }
    }
}

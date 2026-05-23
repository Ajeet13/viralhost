package com.viralhost.solarleads.ui.list

import android.content.Intent
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.viralhost.solarleads.ui.components.MessageChannel
import com.viralhost.solarleads.ui.components.TemplatePickerDialog
import com.viralhost.solarleads.util.CsvExporter
import com.viralhost.solarleads.util.MessageUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LeadListScreen(
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onImport: () -> Unit,
    onOpenTemplates: () -> Unit,
    vm: LeadListViewModel = viewModel(
        factory = LeadListViewModel.factory(LocalContext.current.applicationContext as android.app.Application)
    )
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val exportedFile by vm.exportedFile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(exportedFile) {
        val f = exportedFile ?: return@LaunchedEffect
        val intent = CsvExporter.buildShareIntent(context, f)
        context.startActivity(Intent.createChooser(intent, "Share leads CSV"))
        vm.consumeExportedFile()
    }

    var menuOpen by remember { mutableStateOf(false) }
    var filtersOpen by remember { mutableStateOf(false) }
    var showBulkPicker by remember { mutableStateOf(false) }
    var showSinglePickerFor by remember { mutableStateOf<Lead?>(null) }

    Scaffold(
        topBar = {
            if (state.selectionMode) {
                TopAppBar(
                    title = { Text("${state.selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = vm::clearSelection) {
                            Icon(Icons.Filled.Close, "Clear")
                        }
                    },
                    actions = {
                        IconButton(onClick = vm::selectAllVisible) {
                            Icon(Icons.Filled.SelectAll, "Select all")
                        }
                        IconButton(
                            enabled = state.selectedIds.isNotEmpty(),
                            onClick = { showBulkPicker = true }
                        ) {
                            Icon(Icons.Filled.Send, "Bulk send")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Solar Leads") },
                    actions = {
                        IconButton(onClick = { filtersOpen = !filtersOpen }) {
                            Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                        }
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
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
                            DropdownMenuItem(
                                text = { Text("Message templates") },
                                leadingIcon = { Icon(Icons.Filled.Description, null) },
                                onClick = { menuOpen = false; onOpenTemplates() }
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!state.selectionMode) {
                ExtendedFloatingActionButton(
                    onClick = onAdd,
                    icon = { Icon(Icons.Filled.Add, null) },
                    text = { Text("Add Lead") }
                )
            }
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
                        LeadRow(
                            lead = lead,
                            selected = lead.id in state.selectedIds,
                            selectionMode = state.selectionMode,
                            onClick = {
                                if (state.selectionMode) vm.toggleSelection(lead.id)
                                else onOpen(lead.id)
                            },
                            onLongClick = {
                                if (!state.selectionMode) vm.startSelection(lead.id)
                            },
                            onMessage = { showSinglePickerFor = lead }
                        )
                    }
                }
            }
        }
    }

    if (showBulkPicker && state.selectionMode) {
        TemplatePickerDialog(
            title = "Send to ${state.selectedIds.size} leads",
            templates = state.templates,
            onDismiss = { showBulkPicker = false },
            onSend = { channel, body ->
                showBulkPicker = false
                scope.launch {
                    val recipients = vm.selectedLeads()
                    when (channel) {
                        MessageChannel.WHATSAPP -> {
                            // WhatsApp doesn't support a true broadcast intent; we open
                            // each chat sequentially. The user taps Send on each.
                            recipients.forEach { lead ->
                                val rendered = body
                                    .replace("{name}", lead.name)
                                    .replace("{phone}", lead.phone)
                                    .replace("{address}", lead.address.orEmpty())
                                    .replace("{size}", lead.systemSizeKw?.toString().orEmpty())
                                MessageUtils.sendWhatsApp(context, lead.phone, rendered)
                            }
                        }
                        MessageChannel.SMS -> {
                            // SMS supports a multi-recipient compose intent. We send the
                            // body verbatim (placeholder substitution per-recipient is
                            // not possible because they share one message).
                            MessageUtils.sendSmsToMany(
                                context,
                                recipients.map { it.phone },
                                body
                            )
                        }
                    }
                    vm.clearSelection()
                }
            },
            previewMessage = { tpl ->
                // Show preview for a representative recipient (first selected)
                val first = state.leads.firstOrNull { it.id in state.selectedIds }
                first?.let { tpl.render(it) } ?: tpl.body
            }
        )
    }

    showSinglePickerFor?.let { lead ->
        TemplatePickerDialog(
            title = "Send to ${lead.name}",
            templates = state.templates,
            onDismiss = { showSinglePickerFor = null },
            onSend = { channel, body ->
                showSinglePickerFor = null
                when (channel) {
                    MessageChannel.WHATSAPP -> MessageUtils.sendWhatsApp(context, lead.phone, body)
                    MessageChannel.SMS -> MessageUtils.sendSms(context, lead.phone, body)
                }
            },
            previewMessage = { it.render(lead) }
        )
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun LeadRow(
    lead: Lead,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMessage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onClick() }
                )
                Spacer(Modifier.width(4.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
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
                        "${lead.roofType.display}${lead.systemSizeKw?.let { " · $it kW" }.orEmpty()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (!selectionMode) {
                IconButton(onClick = onMessage) {
                    Icon(Icons.Filled.Chat, contentDescription = "Send message")
                }
            }
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
    Surface(
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

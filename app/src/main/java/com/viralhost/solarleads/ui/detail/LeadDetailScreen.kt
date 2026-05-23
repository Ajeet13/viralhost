package com.viralhost.solarleads.ui.detail

import android.Manifest
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.viralhost.solarleads.data.model.CallLog
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.Reminder
import com.viralhost.solarleads.util.CallUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(
    leadId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val owner = LocalViewModelStoreOwner.current!!
    val factory = LeadDetailViewModel.factory(app)
    val extras = remember(leadId) {
        MutableCreationExtras().apply { set(LeadDetailViewModel.KEY, leadId) }
    }
    val vm: LeadDetailViewModel = remember(leadId) {
        ViewModelProvider(owner, factory, extras)[LeadDetailViewModel::class.java]
    }

    val state by vm.uiState.collectAsStateWithLifecycle()
    val lead = state.lead

    var pendingPhone by remember { mutableStateOf<String?>(null) }
    var showStatusPicker by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showOutcomeForCallId by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) }

    val callPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val phone = pendingPhone ?: return@rememberLauncherForActivityResult
        pendingPhone = null
        if (granted) CallUtils.call(context, phone) else CallUtils.dial(context, phone)
        // Whether direct or dialer, log the call
        vm.logCall()
    }

    LaunchedEffect(state.lead?.id) {
        // no-op, here for future deep linking actions
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lead?.name ?: "Lead") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { lead?.let { onEdit(it.id) } }) {
                        Icon(Icons.Filled.Edit, "Edit")
                    }
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, "More")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Add reminder") },
                            leadingIcon = { Icon(Icons.Filled.Alarm, null) },
                            onClick = { menuOpen = false; showReminderDialog = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete lead") },
                            leadingIcon = { Icon(Icons.Filled.Delete, null) },
                            onClick = { menuOpen = false; showDeleteConfirm = true }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (lead != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        pendingPhone = lead.phone
                        if (CallUtils.hasCallPermission(context)) {
                            CallUtils.call(context, lead.phone)
                            vm.logCall()
                        } else {
                            callPermission.launch(Manifest.permission.CALL_PHONE)
                        }
                    },
                    icon = { Icon(Icons.Filled.Call, null) },
                    text = { Text("Call") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { padding ->
        if (lead == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading...")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LeadHeader(lead = lead, onChangeStatus = { showStatusPicker = true })

            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Call History") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Reminders") })
            }

            when (tab) {
                0 -> CallHistoryList(
                    logs = state.callLogs,
                    onSetOutcome = { showOutcomeForCallId = it },
                    onDelete = { vm.deleteCallLog(it) }
                )
                1 -> ReminderList(
                    reminders = state.reminders,
                    onAdd = { showReminderDialog = true },
                    onDelete = { vm.deleteReminder(it) }
                )
            }
        }
    }

    if (showStatusPicker) {
        StatusPickerDialog(
            current = lead?.status ?: LeadStatus.NEW,
            onDismiss = { showStatusPicker = false },
            onPick = {
                vm.updateStatus(it)
                showStatusPicker = false
            }
        )
    }

    if (showReminderDialog && lead != null) {
        ReminderPickerDialog(
            onDismiss = { showReminderDialog = false },
            onConfirm = { triggerAt, message ->
                vm.addReminder(triggerAt, message)
                showReminderDialog = false
            }
        )
    }

    showOutcomeForCallId?.let { callId ->
        OutcomeDialog(
            onDismiss = { showOutcomeForCallId = null },
            onConfirm = { outcome, notes, status ->
                vm.updateCallOutcome(callId, outcome, notes)
                if (status != null) vm.updateStatus(status)
                showOutcomeForCallId = null
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this lead?") },
            text = { Text("This will also remove call history and reminders.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteLead()
                    showDeleteConfirm = false
                    onBack()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun LeadHeader(lead: Lead, onChangeStatus: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(lead.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Phone: ${lead.phone}")
            if (!lead.ivrs.isNullOrBlank()) Text("IVRS: ${lead.ivrs}")
            if (!lead.address.isNullOrBlank()) Text("Address: ${lead.address}")
            Text("Roof: ${lead.roofType.display}")
            lead.systemSizeKw?.let { Text("System size: $it kW") }
            if (!lead.notes.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("Notes: ${lead.notes}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Status:", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = onChangeStatus, label = { Text(lead.status.display) })
            }
        }
    }
}

@Composable
private fun CallHistoryList(
    logs: List<CallLog>,
    onSetOutcome: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No calls yet. Tap Call to start.")
        }
        return
    }
    val df = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(logs, key = { it.id }) { log ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(df.format(Date(log.calledAt)),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDelete(log.id) }) {
                            Icon(Icons.Filled.Delete, "Delete")
                        }
                    }
                    Text("Outcome: ${log.outcome ?: "—"}",
                        style = MaterialTheme.typography.bodyMedium)
                    if (!log.notes.isNullOrBlank()) {
                        Text("Notes: ${log.notes}",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { onSetOutcome(log.id) }) {
                        Text("Set / Update outcome")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderList(
    reminders: List<Reminder>,
    onAdd: () -> Unit,
    onDelete: (Long) -> Unit
) {
    val df = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)
    ) {
        OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text("Add reminder")
        }
        Spacer(Modifier.height(12.dp))
        if (reminders.isEmpty()) {
            Text("No reminders yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(reminders, key = { it.id }) { r ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(df.format(Date(r.triggerAt)),
                                    style = MaterialTheme.typography.titleSmall)
                                if (!r.message.isNullOrBlank()) Text(r.message)
                                Text(if (r.done) "Done" else "Pending",
                                    style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { onDelete(r.id) }) {
                                Icon(Icons.Filled.Delete, "Delete reminder")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPickerDialog(
    current: LeadStatus,
    onDismiss: () -> Unit,
    onPick: (LeadStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change status") },
        text = {
            Column {
                LeadStatus.values().forEach { s ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        FilterChip(
                            selected = s == current,
                            onClick = { onPick(s) },
                            label = { Text(s.display) }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun OutcomeDialog(
    onDismiss: () -> Unit,
    onConfirm: (outcome: String, notes: String, status: LeadStatus?) -> Unit
) {
    var outcome by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<LeadStatus?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Call outcome") },
        text = {
            Column {
                Text("Pick a result:")
                Spacer(Modifier.height(6.dp))
                val outcomes = listOf(
                    "Connected" to LeadStatus.INTERESTED,
                    "Not interested" to LeadStatus.NOT_INTERESTED,
                    "No answer" to null,
                    "Wrong number" to LeadStatus.LOST,
                    "Callback later" to LeadStatus.CALLBACK_SCHEDULED,
                    "Site visit booked" to LeadStatus.SITE_VISIT_BOOKED,
                    "Quoted" to LeadStatus.QUOTED,
                    "Converted" to LeadStatus.CONVERTED
                )
                outcomes.forEach { (label, s) ->
                    FilterChip(
                        selected = outcome == label,
                        onClick = { outcome = label; status = s },
                        label = { Text(label) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = outcome.isNotBlank(),
                onClick = { onConfirm(outcome, notes, status) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ReminderPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (triggerAt: Long, message: String?) -> Unit
) {
    val context = LocalContext.current
    val now = remember { Calendar.getInstance() }
    var dateMillis by remember { mutableStateOf<Long?>(null) }
    var hour by remember { mutableStateOf(now.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(now.get(Calendar.MINUTE)) }
    var message by remember { mutableStateOf("") }

    val df = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val tf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val triggerAt by remember {
        derivedStateOf {
            dateMillis?.let { d ->
                val c = Calendar.getInstance().apply {
                    timeInMillis = d
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }
                c.timeInMillis
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule callback") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = {
                        val c = Calendar.getInstance().apply { dateMillis?.let { timeInMillis = it } }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val cal = Calendar.getInstance().apply { set(y, m, d) }
                                dateMillis = cal.timeInMillis
                            },
                            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Text(dateMillis?.let { df.format(Date(it)) } ?: "Pick date")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m -> hour = h; minute = m },
                            hour, minute, true
                        ).show()
                    }) {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute)
                        }
                        Text(tf.format(cal.time))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = triggerAt != null && (triggerAt ?: 0L) > System.currentTimeMillis(),
                onClick = {
                    triggerAt?.let { onConfirm(it, message.ifBlank { null }) }
                }
            ) { Text("Schedule") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

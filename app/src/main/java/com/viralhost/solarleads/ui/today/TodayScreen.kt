package com.viralhost.solarleads.ui.today

import android.app.Application
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viralhost.solarleads.util.CallUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onOpenLead: (Long) -> Unit,
    vm: TodayViewModel = viewModel(
        factory = TodayViewModel.factory(LocalContext.current.applicationContext as Application)
    )
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val tf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Today's Callbacks") }) }
    ) { padding ->
        if (state.overdue.isEmpty() && state.today.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Schedule,
                        null,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text("No callbacks scheduled for today", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Schedule one from a lead's detail screen.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.overdue.isNotEmpty()) {
                item {
                    SectionHeader("Overdue", color = MaterialTheme.colorScheme.error)
                }
                items(state.overdue, key = { "ov-${it.reminder.id}" }) { item ->
                    ReminderCard(
                        item = item,
                        timeText = tf.format(Date(item.reminder.triggerAt)),
                        accent = MaterialTheme.colorScheme.errorContainer,
                        onCall = { item.lead?.let { CallUtils.call(context, it.phone) } },
                        onDone = { vm.markDone(item.reminder.id) },
                        onDelete = { vm.delete(item.reminder.id) },
                        onClick = { item.lead?.let { onOpenLead(it.id) } }
                    )
                }
            }
            if (state.today.isNotEmpty()) {
                item { SectionHeader("Today") }
                items(state.today, key = { "to-${it.reminder.id}" }) { item ->
                    ReminderCard(
                        item = item,
                        timeText = tf.format(Date(item.reminder.triggerAt)),
                        accent = MaterialTheme.colorScheme.primaryContainer,
                        onCall = { item.lead?.let { CallUtils.call(context, it.phone) } },
                        onDone = { vm.markDone(item.reminder.id) },
                        onDelete = { vm.delete(item.reminder.id) },
                        onClick = { item.lead?.let { onOpenLead(it.id) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun ReminderCard(
    item: TodayItem,
    timeText: String,
    accent: androidx.compose.ui.graphics.Color,
    onCall: () -> Unit,
    onDone: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val lead = item.lead
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = accent,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Text(
                    timeText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    lead?.name ?: "(deleted lead)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (lead != null) {
                    Text(lead.phone, style = MaterialTheme.typography.bodySmall)
                }
                if (!item.reminder.message.isNullOrBlank()) {
                    Text(
                        item.reminder.message,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
            }
            if (lead != null) {
                IconButton(onClick = onCall) { Icon(Icons.Filled.Call, "Call") }
            }
            IconButton(onClick = onDone) { Icon(Icons.Filled.Check, "Mark done") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Delete") }
        }
    }
}

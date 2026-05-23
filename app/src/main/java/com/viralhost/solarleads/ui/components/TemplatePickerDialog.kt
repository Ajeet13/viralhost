package com.viralhost.solarleads.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.viralhost.solarleads.data.model.MessageTemplate

enum class MessageChannel { WHATSAPP, SMS }

/**
 * Lets the user pick a [MessageTemplate] (or edit it inline) and choose a channel
 * (WhatsApp / SMS). The caller receives the rendered message text and the channel.
 *
 * @param previewMessage Optional function that returns how the template will render
 * for a single representative recipient. If null, the raw body is shown.
 */
@Composable
fun TemplatePickerDialog(
    title: String,
    templates: List<MessageTemplate>,
    onDismiss: () -> Unit,
    onSend: (channel: MessageChannel, body: String) -> Unit,
    previewMessage: ((MessageTemplate) -> String)? = null
) {
    var selectedId by remember { mutableStateOf(templates.firstOrNull()?.id) }
    val selected = templates.firstOrNull { it.id == selectedId }
    var customBody by remember(selectedId) { mutableStateOf<String?>(null) }
    val initialBody = selected?.let { previewMessage?.invoke(it) ?: it.body }.orEmpty()
    val effectiveBody = customBody ?: initialBody

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (templates.isEmpty()) {
                    Text(
                        "No templates yet. Open the Templates screen from the side menu to add one.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text("Pick a template:", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        items(templates, key = { it.id }) { t ->
                            val isSelected = selectedId == t.id
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedId = t.id
                                        customBody = null
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        t.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        t.body,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Preview:", style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(
                        value = effectiveBody,
                        onValueChange = { customBody = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        label = { Text("Edit before sending") }
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(
                    enabled = effectiveBody.isNotBlank(),
                    onClick = { onSend(MessageChannel.WHATSAPP, effectiveBody) }
                ) {
                    Icon(Icons.Filled.Chat, null)
                    Spacer(Modifier.width(4.dp))
                    Text("WhatsApp")
                }
                FilledTonalButton(
                    enabled = effectiveBody.isNotBlank(),
                    onClick = { onSend(MessageChannel.SMS, effectiveBody) }
                ) {
                    Icon(Icons.Filled.Sms, null)
                    Spacer(Modifier.width(4.dp))
                    Text("SMS")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

package com.viralhost.solarleads.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A reusable WhatsApp / SMS message template. The body may contain placeholders
 * such as {name}, {phone}, {address}, {size} which are substituted at send time.
 */
@Entity(tableName = "message_templates")
data class MessageTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun render(lead: Lead): String {
        return body
            .replace("{name}", lead.name)
            .replace("{phone}", lead.phone)
            .replace("{address}", lead.address.orEmpty())
            .replace("{size}", lead.systemSizeKw?.toString().orEmpty())
            .replace("{ivrs}", lead.ivrs.orEmpty())
            .replace("{roof}", lead.roofType.display)
    }
}

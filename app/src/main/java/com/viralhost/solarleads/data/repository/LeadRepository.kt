package com.viralhost.solarleads.data.repository

import com.viralhost.solarleads.cloud.CloudSync
import com.viralhost.solarleads.data.dao.CallLogDao
import com.viralhost.solarleads.data.dao.LeadDao
import com.viralhost.solarleads.data.dao.MessageTemplateDao
import com.viralhost.solarleads.data.dao.ReminderDao
import com.viralhost.solarleads.data.model.CallLog
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.MessageTemplate
import com.viralhost.solarleads.data.model.Reminder
import com.viralhost.solarleads.data.model.RoofType
import kotlinx.coroutines.flow.Flow

data class ReminderWithLead(val reminder: Reminder, val lead: Lead?)

class LeadRepository(
    private val leadDao: LeadDao,
    private val callLogDao: CallLogDao,
    private val reminderDao: ReminderDao,
    private val templateDao: MessageTemplateDao,
    private val cloudSync: CloudSync? = null
) {
    fun observeAll(): Flow<List<Lead>> = leadDao.observeAll()

    fun search(query: String, status: LeadStatus?, roof: RoofType?): Flow<List<Lead>> =
        leadDao.search(query, status, roof)

    fun observeLead(id: Long): Flow<Lead?> = leadDao.observeById(id)

    suspend fun getLead(id: Long): Lead? = leadDao.getById(id)

    suspend fun upsert(lead: Lead): Long {
        val now = System.currentTimeMillis()
        val saved: Lead
        val id: Long
        if (lead.id == 0L) {
            saved = lead.copy(updatedAt = now)
            id = leadDao.insert(saved)
        } else {
            saved = lead.copy(updatedAt = now)
            leadDao.update(saved)
            id = lead.id
        }
        // After local persist, push the canonical row (with assigned id) to the cloud.
        leadDao.getById(id)?.let { cloudSync?.pushLead(it) }
        return id
    }

    suspend fun insertAll(leads: List<Lead>): List<Long> {
        val ids = leadDao.insertAll(leads)
        // Push imported rows to the cloud
        ids.forEach { id -> leadDao.getById(id)?.let { cloudSync?.pushLead(it) } }
        return ids
    }

    suspend fun delete(lead: Lead) {
        leadDao.delete(lead)
        cloudSync?.deleteLead(lead.syncId)
    }

    suspend fun updateStatus(id: Long, status: LeadStatus) {
        leadDao.updateStatus(id, status)
        leadDao.getById(id)?.let { cloudSync?.pushLead(it) }
    }

    suspend fun all(): List<Lead> = leadDao.getAll()

    suspend fun phoneExists(phone: String): Boolean = leadDao.countByPhone(phone) > 0

    suspend fun getLeadsByIds(ids: List<Long>): List<Lead> =
        ids.mapNotNull { leadDao.getById(it) }

    // Call logs
    fun observeCallLogs(leadId: Long): Flow<List<CallLog>> = callLogDao.observeForLead(leadId)
    suspend fun logCall(leadId: Long, outcome: String? = null, notes: String? = null): Long {
        val id = callLogDao.insert(CallLog(leadId = leadId, outcome = outcome, notes = notes))
        val saved = callLogDao.getSince(0L).firstOrNull { it.id == id }
        val parent = leadDao.getById(leadId)
        if (saved != null && parent != null) {
            cloudSync?.pushCallLog(saved, parent.syncId)
        }
        return id
    }

    suspend fun updateCallOutcome(id: Long, outcome: String?, notes: String?) {
        callLogDao.updateOutcome(id, outcome, notes)
        // Optionally push the updated call log
        val saved = callLogDao.getSince(0L).firstOrNull { it.id == id } ?: return
        val parent = leadDao.getById(saved.leadId) ?: return
        cloudSync?.pushCallLog(saved, parent.syncId)
    }

    suspend fun deleteCallLog(id: Long) = callLogDao.delete(id)

    suspend fun allCallLogsSince(start: Long): List<CallLog> = callLogDao.getSince(start)

    // Reminders
    fun observeReminders(leadId: Long): Flow<List<Reminder>> = reminderDao.observeForLead(leadId)
    fun observeRemindersInRange(start: Long, end: Long): Flow<List<Reminder>> =
        reminderDao.observeInRange(start, end)

    fun observeOverdueReminders(now: Long): Flow<List<Reminder>> =
        reminderDao.observeOverdue(now)

    suspend fun addReminder(reminder: Reminder): Long = reminderDao.insert(reminder)
    suspend fun markReminderDone(id: Long) = reminderDao.markDone(id)
    suspend fun deleteReminder(id: Long) = reminderDao.delete(id)
    suspend fun getReminder(id: Long): Reminder? = reminderDao.getById(id)

    // Message templates
    fun observeTemplates(): Flow<List<MessageTemplate>> = templateDao.observeAll()
    suspend fun allTemplates(): List<MessageTemplate> = templateDao.getAll()

    suspend fun upsertTemplate(t: MessageTemplate): Long {
        val id = templateDao.insert(t)
        // Re-read to make sure we push the row with its db-assigned id.
        val saved = templateDao.getAll().firstOrNull { it.id == id } ?: return id
        cloudSync?.pushTemplate(saved)
        return id
    }

    suspend fun deleteTemplate(t: MessageTemplate) {
        templateDao.delete(t)
        cloudSync?.deleteTemplate(t.syncId)
    }

    suspend fun templateCount(): Int = templateDao.count()

    suspend fun seedDefaultTemplates() {
        if (templateDao.count() > 0) return
        val seeds = listOf(
            MessageTemplate(
                title = "Initial Outreach",
                body = "Hi {name}, this is from our solar team. Are you still interested in a solar setup for your place at {address}?",
                isDefault = true
            ),
            MessageTemplate(
                title = "Quote Follow-up",
                body = "Hi {name}, here's our quote for a {size} kW solar system. Let me know if you'd like to discuss the details.",
                isDefault = true
            ),
            MessageTemplate(
                title = "Site Visit Request",
                body = "Hi {name}, just following up on our call. When would be a good time to visit your site for measurements?",
                isDefault = true
            ),
            MessageTemplate(
                title = "Confirmation",
                body = "Thanks {name}! Your solar installation is confirmed. Our team will be in touch with the next steps soon.",
                isDefault = true
            )
        )
        templateDao.insertAll(seeds)
        // We don't immediately push to the cloud here; CloudSync.start() will do
        // a full reconciliation push on first sign-in.
    }
}

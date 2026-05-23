package com.viralhost.solarleads.data.repository

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
    private val templateDao: MessageTemplateDao
) {
    fun observeAll(): Flow<List<Lead>> = leadDao.observeAll()

    fun search(query: String, status: LeadStatus?, roof: RoofType?): Flow<List<Lead>> =
        leadDao.search(query, status, roof)

    fun observeLead(id: Long): Flow<Lead?> = leadDao.observeById(id)

    suspend fun getLead(id: Long): Lead? = leadDao.getById(id)

    suspend fun upsert(lead: Lead): Long {
        return if (lead.id == 0L) leadDao.insert(lead.copy(updatedAt = System.currentTimeMillis()))
        else {
            leadDao.update(lead.copy(updatedAt = System.currentTimeMillis()))
            lead.id
        }
    }

    suspend fun insertAll(leads: List<Lead>): List<Long> = leadDao.insertAll(leads)

    suspend fun delete(lead: Lead) = leadDao.delete(lead)

    suspend fun updateStatus(id: Long, status: LeadStatus) = leadDao.updateStatus(id, status)

    suspend fun all(): List<Lead> = leadDao.getAll()

    suspend fun phoneExists(phone: String): Boolean = leadDao.countByPhone(phone) > 0

    suspend fun getLeadsByIds(ids: List<Long>): List<Lead> =
        ids.mapNotNull { leadDao.getById(it) }

    // Call logs
    fun observeCallLogs(leadId: Long): Flow<List<CallLog>> = callLogDao.observeForLead(leadId)
    suspend fun logCall(leadId: Long, outcome: String? = null, notes: String? = null): Long =
        callLogDao.insert(CallLog(leadId = leadId, outcome = outcome, notes = notes))

    suspend fun updateCallOutcome(id: Long, outcome: String?, notes: String?) =
        callLogDao.updateOutcome(id, outcome, notes)

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
    suspend fun upsertTemplate(t: MessageTemplate): Long = templateDao.insert(t)
    suspend fun deleteTemplate(t: MessageTemplate) = templateDao.delete(t)
    suspend fun templateCount(): Int = templateDao.count()
    suspend fun seedDefaultTemplates() {
        if (templateDao.count() > 0) return
        templateDao.insertAll(
            listOf(
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
        )
    }
}

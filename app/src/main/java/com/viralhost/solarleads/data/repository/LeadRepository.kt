package com.viralhost.solarleads.data.repository

import com.viralhost.solarleads.data.dao.CallLogDao
import com.viralhost.solarleads.data.dao.LeadDao
import com.viralhost.solarleads.data.dao.ReminderDao
import com.viralhost.solarleads.data.model.CallLog
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.Reminder
import com.viralhost.solarleads.data.model.RoofType
import kotlinx.coroutines.flow.Flow

class LeadRepository(
    private val leadDao: LeadDao,
    private val callLogDao: CallLogDao,
    private val reminderDao: ReminderDao
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

    // Call logs
    fun observeCallLogs(leadId: Long): Flow<List<CallLog>> = callLogDao.observeForLead(leadId)
    suspend fun logCall(leadId: Long, outcome: String? = null, notes: String? = null): Long =
        callLogDao.insert(CallLog(leadId = leadId, outcome = outcome, notes = notes))

    suspend fun updateCallOutcome(id: Long, outcome: String?, notes: String?) =
        callLogDao.updateOutcome(id, outcome, notes)

    suspend fun deleteCallLog(id: Long) = callLogDao.delete(id)

    // Reminders
    fun observeReminders(leadId: Long): Flow<List<Reminder>> = reminderDao.observeForLead(leadId)
    suspend fun addReminder(reminder: Reminder): Long = reminderDao.insert(reminder)
    suspend fun markReminderDone(id: Long) = reminderDao.markDone(id)
    suspend fun deleteReminder(id: Long) = reminderDao.delete(id)
    suspend fun getReminder(id: Long): Reminder? = reminderDao.getById(id)
}

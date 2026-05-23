package com.viralhost.solarleads.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.viralhost.solarleads.data.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE leadId = :leadId ORDER BY triggerAt ASC")
    fun observeForLead(leadId: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE done = 0 ORDER BY triggerAt ASC")
    fun observeUpcoming(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE triggerAt BETWEEN :start AND :end ORDER BY triggerAt ASC")
    fun observeInRange(start: Long, end: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE done = 0 AND triggerAt < :now ORDER BY triggerAt ASC")
    fun observeOverdue(now: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Query("UPDATE reminders SET done = 1 WHERE id = :id")
    suspend fun markDone(id: Long)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Long)
}

package com.viralhost.solarleads.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.viralhost.solarleads.data.model.CallLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {

    @Query("SELECT * FROM call_logs WHERE leadId = :leadId ORDER BY calledAt DESC")
    fun observeForLead(leadId: Long): Flow<List<CallLog>>

    @Query("SELECT * FROM call_logs WHERE calledAt >= :start ORDER BY calledAt DESC")
    suspend fun getSince(start: Long): List<CallLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callLog: CallLog): Long

    @Query("UPDATE call_logs SET outcome = :outcome, notes = :notes WHERE id = :id")
    suspend fun updateOutcome(id: Long, outcome: String?, notes: String?)

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun delete(id: Long)
}

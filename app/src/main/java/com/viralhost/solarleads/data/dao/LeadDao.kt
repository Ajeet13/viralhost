package com.viralhost.solarleads.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.RoofType
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {

    @Query("SELECT * FROM leads ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE id = :id")
    fun observeById(id: Long): Flow<Lead?>

    @Query("SELECT * FROM leads WHERE id = :id")
    suspend fun getById(id: Long): Lead?

    @Query(
        """
        SELECT * FROM leads
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR ivrs LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%')
          AND (:status IS NULL OR status = :status)
          AND (:roof IS NULL OR roofType = :roof)
        ORDER BY updatedAt DESC
        """
    )
    fun search(query: String, status: LeadStatus?, roof: RoofType?): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: String): Lead?

    @Query("SELECT * FROM leads ORDER BY updatedAt DESC")
    suspend fun getAll(): List<Lead>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lead: Lead): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(leads: List<Lead>): List<Long>

    @Update
    suspend fun update(lead: Lead)

    @Query("UPDATE leads SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: LeadStatus, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(lead: Lead)

    @Query("DELETE FROM leads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM leads WHERE phone = :phone")
    suspend fun countByPhone(phone: String): Int
}

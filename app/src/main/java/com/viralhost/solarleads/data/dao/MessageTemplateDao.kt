package com.viralhost.solarleads.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.viralhost.solarleads.data.model.MessageTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageTemplateDao {

    @Query("SELECT * FROM message_templates ORDER BY isDefault DESC, createdAt ASC")
    fun observeAll(): Flow<List<MessageTemplate>>

    @Query("SELECT * FROM message_templates ORDER BY isDefault DESC, createdAt ASC")
    suspend fun getAll(): List<MessageTemplate>

    @Query("SELECT COUNT(*) FROM message_templates")
    suspend fun count(): Int

    @Query("SELECT * FROM message_templates WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: String): MessageTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: MessageTemplate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<MessageTemplate>)

    @Update
    suspend fun update(template: MessageTemplate)

    @Delete
    suspend fun delete(template: MessageTemplate)

    @Query("DELETE FROM message_templates WHERE id = :id")
    suspend fun deleteById(id: Long)
}

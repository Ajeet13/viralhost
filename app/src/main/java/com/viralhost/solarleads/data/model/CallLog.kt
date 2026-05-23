package com.viralhost.solarleads.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_logs",
    foreignKeys = [
        ForeignKey(
            entity = Lead::class,
            parentColumns = ["id"],
            childColumns = ["leadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("leadId")]
)
data class CallLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leadId: Long,
    val calledAt: Long = System.currentTimeMillis(),
    val outcome: String? = null,
    val notes: String? = null,
    val syncId: String = java.util.UUID.randomUUID().toString()
)

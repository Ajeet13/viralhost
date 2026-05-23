package com.viralhost.solarleads.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val ivrs: String? = null,
    val address: String? = null,
    val roofType: RoofType = RoofType.OTHER,
    val systemSizeKw: Double? = null,
    val status: LeadStatus = LeadStatus.NEW,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    /** Stable cross-device identity used for cloud sync. Generated on insert. */
    val syncId: String = java.util.UUID.randomUUID().toString()
)

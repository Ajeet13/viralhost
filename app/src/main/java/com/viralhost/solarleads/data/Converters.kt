package com.viralhost.solarleads.data

import androidx.room.TypeConverter
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.RoofType

class Converters {
    @TypeConverter
    fun fromLeadStatus(value: LeadStatus): String = value.name

    @TypeConverter
    fun toLeadStatus(value: String): LeadStatus = LeadStatus.fromName(value)

    @TypeConverter
    fun fromRoofType(value: RoofType): String = value.name

    @TypeConverter
    fun toRoofType(value: String): RoofType = RoofType.fromName(value)
}

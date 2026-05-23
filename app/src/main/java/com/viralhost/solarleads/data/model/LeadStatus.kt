package com.viralhost.solarleads.data.model

enum class LeadStatus(val display: String) {
    NEW("New"),
    INTERESTED("Interested"),
    NOT_INTERESTED("Not Interested"),
    CALLBACK_SCHEDULED("Callback Scheduled"),
    SITE_VISIT_BOOKED("Site Visit Booked"),
    QUOTED("Quoted"),
    CONVERTED("Converted"),
    LOST("Lost");

    companion object {
        fun fromName(name: String?): LeadStatus =
            values().firstOrNull { it.name.equals(name, ignoreCase = true) } ?: NEW
    }
}

enum class RoofType(val display: String) {
    RCC("RCC"),
    TIN("Tin"),
    OTHER("Other");

    companion object {
        fun fromName(name: String?): RoofType {
            if (name.isNullOrBlank()) return OTHER
            return values().firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
                ?: values().firstOrNull { it.display.equals(name.trim(), ignoreCase = true) }
                ?: OTHER
        }
    }
}

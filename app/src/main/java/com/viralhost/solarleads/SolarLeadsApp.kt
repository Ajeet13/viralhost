package com.viralhost.solarleads

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.viralhost.solarleads.data.AppDatabase
import com.viralhost.solarleads.data.repository.LeadRepository

class SolarLeadsApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val repository: LeadRepository by lazy {
        LeadRepository(database.leadDao(), database.callLogDao(), database.reminderDao())
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Callback Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled callbacks to leads"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "callback_reminders"
    }
}

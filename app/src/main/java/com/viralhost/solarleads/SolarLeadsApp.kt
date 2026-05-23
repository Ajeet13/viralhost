package com.viralhost.solarleads

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.viralhost.solarleads.cloud.CloudSync
import com.viralhost.solarleads.data.AppDatabase
import com.viralhost.solarleads.data.repository.LeadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SolarLeadsApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val cloudSync: CloudSync by lazy { CloudSync.get(this, database) }
    val repository: LeadRepository by lazy {
        LeadRepository(
            database.leadDao(),
            database.callLogDao(),
            database.reminderDao(),
            database.messageTemplateDao(),
            cloudSync
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        seedDefaultsAsync()
        cloudSync.start()
    }

    private fun seedDefaultsAsync() {
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                repository.seedDefaultTemplates()
            } catch (_: Throwable) { /* ignore */ }
        }
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

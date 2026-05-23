package com.viralhost.solarleads.reminders

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.Reminder
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private fun tag(reminderId: Long) = "reminder_$reminderId"

    fun schedule(context: Context, lead: Lead, reminder: Reminder) {
        val delay = (reminder.triggerAt - System.currentTimeMillis()).coerceAtLeast(0L)

        val data = Data.Builder()
            .putLong(ReminderWorker.KEY_REMINDER_ID, reminder.id)
            .putLong(ReminderWorker.KEY_LEAD_ID, lead.id)
            .putString(ReminderWorker.KEY_LEAD_NAME, lead.name)
            .putString(ReminderWorker.KEY_MESSAGE, reminder.message.orEmpty())
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tag(reminder.id))
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancel(context: Context, reminderId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tag(reminderId))
    }
}

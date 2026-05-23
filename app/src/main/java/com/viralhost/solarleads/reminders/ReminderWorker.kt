package com.viralhost.solarleads.reminders

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.viralhost.solarleads.SolarLeadsApp
import com.viralhost.solarleads.ui.MainActivity

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getLong(KEY_REMINDER_ID, -1L)
        val leadId = inputData.getLong(KEY_LEAD_ID, -1L)
        val leadName = inputData.getString(KEY_LEAD_NAME) ?: "Lead"
        val message = inputData.getString(KEY_MESSAGE).orEmpty()

        notify(reminderId.toInt().coerceAtLeast(1), leadId, leadName, message)

        // Mark reminder as done
        if (reminderId > 0) {
            val app = applicationContext as? SolarLeadsApp
            app?.repository?.markReminderDone(reminderId)
        }
        return Result.success()
    }

    private fun notify(notifId: Int, leadId: Long, leadName: String, message: String) {
        val ctx = applicationContext

        // Android 13+ requires runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val tapIntent = Intent(ctx, MainActivity::class.java).apply {
            putExtra("leadId", leadId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            ctx, notifId, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, SolarLeadsApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Callback: $leadName")
            .setContentText(message.ifBlank { "Time to call back $leadName" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.ifBlank { "Time to call back $leadName" }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        NotificationManagerCompat.from(ctx).notify(notifId, notification)
    }

    companion object {
        const val KEY_REMINDER_ID = "reminderId"
        const val KEY_LEAD_ID = "leadId"
        const val KEY_LEAD_NAME = "leadName"
        const val KEY_MESSAGE = "message"
    }
}

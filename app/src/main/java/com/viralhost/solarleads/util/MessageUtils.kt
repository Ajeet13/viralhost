package com.viralhost.solarleads.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

object MessageUtils {

    private fun normalisePhone(phone: String): String =
        phone.replace(" ", "").replace("-", "").replace("(", "").replace(")", "")

    fun isWhatsAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            try {
                context.packageManager.getPackageInfo("com.whatsapp.w4b", 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    /**
     * Open WhatsApp chat with [phone] and pre-filled [message]. Falls back to opening
     * WhatsApp via the universal wa.me URL, which works even if WhatsApp Business is the
     * only one installed.
     */
    fun sendWhatsApp(context: Context, phone: String, message: String) {
        val cleaned = normalisePhone(phone)
        val text = Uri.encode(message)
        // wa.me works as a universal handler for both WhatsApp and WhatsApp Business
        val uri = Uri.parse("https://wa.me/$cleaned?text=$text")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Open the system SMS app with [phone] pre-selected and [message] pre-filled.
     * Android does not allow silent bulk SMS – the user must tap Send for each message.
     */
    fun sendSms(context: Context, phone: String, message: String) {
        val cleaned = normalisePhone(phone)
        val uri = Uri.parse("smsto:$cleaned")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No SMS app found", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * For "broadcast" style mass-sending via SMS: opens the SMS composer with multiple
     * recipients (semicolon-separated, which is what most Android SMS apps accept).
     * Note: per Google policy, the user still confirms send.
     */
    fun sendSmsToMany(context: Context, phones: List<String>, message: String) {
        if (phones.isEmpty()) return
        val recipients = phones.joinToString(";") { normalisePhone(it) }
        val uri = Uri.parse("smsto:$recipients")
        val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No SMS app found", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.viralhost.solarleads.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat

object CallUtils {

    fun hasCallPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Places a direct call (requires CALL_PHONE). Falls back to opening the dialer
     * if permission is not granted.
     */
    fun call(context: Context, phone: String) {
        val cleaned = phone.replace(" ", "").replace("-", "")
        val uri = Uri.fromParts("tel", cleaned, null)
        val intent = if (hasCallPermission(context)) {
            Intent(Intent.ACTION_CALL, uri)
        } else {
            Intent(Intent.ACTION_DIAL, uri)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun dial(context: Context, phone: String) {
        val cleaned = phone.replace(" ", "").replace("-", "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", cleaned, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

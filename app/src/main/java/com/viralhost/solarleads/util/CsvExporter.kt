package com.viralhost.solarleads.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.opencsv.CSVWriter
import com.viralhost.solarleads.data.model.Lead
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    private val HEADER = arrayOf(
        "Name", "Phone", "IVRS", "Address/City", "Roof Type",
        "System Size (kW)", "Status", "Notes", "Created At", "Updated At"
    )

    /**
     * Writes [leads] to a CSV file in the app's external cache and returns the file.
     */
    fun exportToFile(context: Context, leads: List<Lead>): File {
        val dir = File(context.externalCacheDir ?: context.cacheDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "solar_leads_$timestamp.csv")
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

        CSVWriter(FileWriter(file)).use { writer ->
            writer.writeNext(HEADER)
            leads.forEach { lead ->
                writer.writeNext(
                    arrayOf(
                        lead.name,
                        lead.phone,
                        lead.ivrs.orEmpty(),
                        lead.address.orEmpty(),
                        lead.roofType.display,
                        lead.systemSizeKw?.toString().orEmpty(),
                        lead.status.display,
                        lead.notes.orEmpty(),
                        df.format(Date(lead.createdAt)),
                        df.format(Date(lead.updatedAt))
                    )
                )
            }
        }
        return file
    }

    /** Returns a share Intent for the produced CSV file. */
    fun buildShareIntent(context: Context, file: File): Intent {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

package com.viralhost.solarleads.util

import android.content.Context
import android.net.Uri
import com.viralhost.solarleads.data.model.Lead
import com.viralhost.solarleads.data.model.LeadStatus
import com.viralhost.solarleads.data.model.RoofType
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory

/**
 * Imports leads from an Excel (.xlsx / .xls) or CSV file selected via the Storage Access Framework.
 *
 * Expected columns (header row, case-insensitive). Column order is detected from the header:
 *   - Name
 *   - Phone (or Phone Number / Mobile)
 *   - IVRS
 *   - Address (or Address/City / City)
 *   - Roof Type
 *   - System Size (kW)
 */
object ExcelImporter {

    data class Result(val leads: List<Lead>, val skipped: Int, val errors: List<String>)

    private val NAME_HEADERS = listOf("name", "customer name", "full name")
    private val PHONE_HEADERS = listOf("phone", "phone number", "mobile", "contact", "contact no")
    private val IVRS_HEADERS = listOf("ivrs", "ivr")
    private val ADDRESS_HEADERS = listOf("address", "city", "address/city", "location")
    private val ROOF_HEADERS = listOf("roof", "roof type", "rooftop")
    private val SIZE_HEADERS = listOf("size", "system size", "kw", "system size (kw)", "system size kw")

    fun import(context: Context, uri: Uri): Result {
        val errors = mutableListOf<String>()
        var skipped = 0
        val leads = mutableListOf<Lead>()

        val resolver = context.contentResolver
        val input = resolver.openInputStream(uri)
            ?: return Result(emptyList(), 0, listOf("Could not open file"))

        input.use { stream ->
            val workbook = try {
                WorkbookFactory.create(stream)
            } catch (e: Exception) {
                return Result(emptyList(), 0, listOf("Unsupported file: ${e.message}"))
            }

            workbook.use { wb ->
                val sheet = wb.getSheetAt(0)
                    ?: return Result(emptyList(), 0, listOf("Workbook has no sheets"))
                val headerRow = sheet.getRow(sheet.firstRowNum)
                    ?: return Result(emptyList(), 0, listOf("Header row missing"))

                val idx = mapHeaders(headerRow)
                if (idx.name == -1 || idx.phone == -1) {
                    return Result(
                        emptyList(),
                        0,
                        listOf("Required columns 'Name' and 'Phone' not found in header row")
                    )
                }

                val lastRow = sheet.lastRowNum
                for (r in (sheet.firstRowNum + 1)..lastRow) {
                    val row = sheet.getRow(r) ?: continue
                    val name = cellString(row, idx.name).trim()
                    val phone = cellString(row, idx.phone).trim()
                    if (name.isBlank() || phone.isBlank()) {
                        skipped++
                        continue
                    }
                    leads.add(
                        Lead(
                            name = name,
                            phone = phone,
                            ivrs = cellString(row, idx.ivrs).trim().ifBlank { null },
                            address = cellString(row, idx.address).trim().ifBlank { null },
                            roofType = RoofType.fromName(cellString(row, idx.roof)),
                            systemSizeKw = cellDouble(row, idx.size),
                            status = LeadStatus.NEW
                        )
                    )
                }
            }
        }

        return Result(leads, skipped, errors)
    }

    private data class Indexes(
        val name: Int,
        val phone: Int,
        val ivrs: Int,
        val address: Int,
        val roof: Int,
        val size: Int
    )

    private fun mapHeaders(headerRow: Row): Indexes {
        var name = -1; var phone = -1; var ivrs = -1
        var address = -1; var roof = -1; var size = -1
        for (c in headerRow.firstCellNum until headerRow.lastCellNum) {
            val cell = headerRow.getCell(c.toInt()) ?: continue
            val raw = cell.toString().trim().lowercase()
            when {
                NAME_HEADERS.any { raw == it } && name == -1 -> name = c.toInt()
                PHONE_HEADERS.any { raw == it } && phone == -1 -> phone = c.toInt()
                IVRS_HEADERS.any { raw == it } && ivrs == -1 -> ivrs = c.toInt()
                ADDRESS_HEADERS.any { raw == it } && address == -1 -> address = c.toInt()
                ROOF_HEADERS.any { raw == it } && roof == -1 -> roof = c.toInt()
                SIZE_HEADERS.any { raw == it } && size == -1 -> size = c.toInt()
            }
        }
        return Indexes(name, phone, ivrs, address, roof, size)
    }

    private fun cellString(row: Row, idx: Int): String {
        if (idx < 0) return ""
        val cell: Cell = row.getCell(idx) ?: return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) cell.dateCellValue.toString()
                else {
                    val d = cell.numericCellValue
                    if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> try { cell.stringCellValue } catch (_: Exception) {
                cell.numericCellValue.toString()
            }
            else -> ""
        }
    }

    private fun cellDouble(row: Row, idx: Int): Double? {
        if (idx < 0) return null
        val cell: Cell = row.getCell(idx) ?: return null
        return when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue
            CellType.STRING -> cell.stringCellValue.trim().toDoubleOrNull()
            else -> null
        }
    }
}

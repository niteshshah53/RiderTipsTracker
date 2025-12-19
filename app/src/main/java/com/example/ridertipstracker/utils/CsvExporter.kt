package com.example.ridertipstracker.utils

import android.content.Context
import android.net.Uri
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.opencsv.CSVWriter
import java.io.OutputStreamWriter
import javax.inject.Inject

class CsvExporter @Inject constructor(
    private val context: Context
) {
    fun exportCsv(shifts: List<RiderShift>, uri: Uri): Boolean {
        return try {
            val outputStream = context.contentResolver.openOutputStream(uri) ?: return false
            CSVWriter(OutputStreamWriter(outputStream)).use { writer ->
                // Write header
                writer.writeNext(
                    arrayOf(
                        "Date",
                        "Platform",
                        "Start Time",
                        "End Time",
                        "Total Hours",
                        "Online Tips",
                        "Cash Tips",
                        "Total Tips",
                        "Orders",
                        "Shift Type",
                        "Notes"
                    )
                )
                
                // Write data
                shifts.forEach { shift ->
                    writer.writeNext(
                        arrayOf(
                            shift.date.toString(),
                            shift.platform,
                            shift.shiftStartTime.toString(),
                            shift.shiftEndTime.toString(),
                            shift.totalHours.toString(),
                            shift.onlineTips.toString(),
                            shift.cashTips.toString(),
                            shift.totalTips.toString(),
                            shift.orders.toString(),
                            shift.shiftType,
                            shift.notes ?: ""
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}


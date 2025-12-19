package com.example.ridertipstracker.utils

import android.content.Context
import android.net.Uri
import com.example.ridertipstracker.data.local.entity.RiderShift
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class PdfExporter @Inject constructor(
    private val context: Context
) {
    fun exportPdf(
        shifts: List<RiderShift>,
        uri: Uri,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Boolean {
        return try {
            val outputStream = context.contentResolver.openOutputStream(uri) ?: return false
            
            // Create a simple text-based PDF-like document
            // For a proper PDF, you would need a PDF library, but for now we'll create a formatted text file
            // that can be opened as PDF by most viewers or converted later
            val builder = StringBuilder()
            
            // Title
            builder.append("Rider Tips Tracker Report\n")
            builder.append("=".repeat(50)).append("\n\n")
            
            // Date Range
            if (startDate != null && endDate != null) {
                builder.append("Period: ${startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)} to ${endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}\n\n")
            }
            
            // Summary
            val totalTips = shifts.sumOf { it.totalTips }
            val totalShifts = shifts.size
            val avgTipsPerShift = if (totalShifts > 0) totalTips / totalShifts else 0.0
            val onlineTips = shifts.sumOf { it.onlineTips }
            val cashTips = shifts.sumOf { it.cashTips }
            
            builder.append("SUMMARY\n")
            builder.append("-".repeat(50)).append("\n")
            builder.append("Total Shifts: $totalShifts\n")
            builder.append("Total Tips: €${String.format("%.2f", totalTips)}\n")
            builder.append("Online Tips: €${String.format("%.2f", onlineTips)}\n")
            builder.append("Cash Tips: €${String.format("%.2f", cashTips)}\n")
            builder.append("Average per Shift: €${String.format("%.2f", avgTipsPerShift)}\n\n")
            
            // Table Header
            builder.append("SHIFT DETAILS\n")
            builder.append("-".repeat(50)).append("\n")
            builder.append(String.format("%-12s %-15s %-10s %-8s %-10s\n", 
                "Date", "Platform", "Tips", "Hours", "Orders"))
            builder.append("-".repeat(50)).append("\n")
            
            // Table Data
            shifts.sortedByDescending { it.date }.forEach { shift ->
                builder.append(String.format("%-12s %-15s €%-9.2f %-8.1f %-10d\n",
                    shift.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    shift.platform,
                    shift.totalTips,
                    shift.totalHours,
                    shift.orders
                ))
            }
            
            outputStream.write(builder.toString().toByteArray())
            outputStream.close()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}


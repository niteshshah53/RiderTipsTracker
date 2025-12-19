package com.example.ridertipstracker.utils

import android.content.Context
import android.net.Uri
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.opencsv.CSVReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CsvImporter @Inject constructor(
    private val context: Context
) {
    fun importCsv(uri: Uri, defaultPlatform: String = ""): List<RiderShift> {
        val shifts = mutableListOf<RiderShift>()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()
        
        CSVReader(InputStreamReader(inputStream)).use { reader ->
            val header = reader.readNext() ?: return emptyList()
            
            // Normalize header names (trim and lowercase for matching)
            val normalizedHeaders = header.map { it.trim().lowercase() }
            
            // Map header names to indices - flexible matching for your CSV format
            val dateIdx = normalizedHeaders.indexOfFirst { 
                it.contains("date", ignoreCase = true) 
            }.takeIf { it >= 0 } ?: -1
            val platformIdx = normalizedHeaders.indexOfFirst { 
                it.contains("platform", ignoreCase = true) 
            }.takeIf { it >= 0 } ?: -1
            val startIdx = normalizedHeaders.indexOfFirst { 
                it.contains("shift start") || it.contains("start time") || it.contains("start", ignoreCase = true)
            }.takeIf { it >= 0 } ?: -1
            val endIdx = normalizedHeaders.indexOfFirst { 
                it.contains("shift end") || it.contains("end time") || it.contains("end", ignoreCase = true)
            }.takeIf { it >= 0 } ?: -1
            val hoursWorkedIdx = normalizedHeaders.indexOfFirst { 
                it.contains("hours worked") || it.contains("hours", ignoreCase = true) || it.contains("total hours", ignoreCase = true)
            }.takeIf { it >= 0 } ?: -1
            val onlineTipsIdx = normalizedHeaders.indexOfFirst { 
                it.contains("online tips") || it.contains("online", ignoreCase = true) || it.contains("digital", ignoreCase = true)
            }.takeIf { it >= 0 } ?: -1
            val cashTipsIdx = normalizedHeaders.indexOfFirst { 
                it.contains("cash tips") || it.contains("cash", ignoreCase = true) || it.contains("physical", ignoreCase = true)
            }.takeIf { it >= 0 } ?: -1
            val totalTipsIdx = normalizedHeaders.indexOfFirst { 
                it.contains("total tips") || (it.contains("total") && it.contains("tip"))
            }.takeIf { it >= 0 } ?: -1
            val ordersIdx = normalizedHeaders.indexOfFirst { 
                it.contains("total orders") || it.contains("orders", ignoreCase = true) || it.contains("order", ignoreCase = true) || it.contains("deliveries", ignoreCase = true)
            }.takeIf { it >= 0 } ?: -1
            val shiftTypeIdx = normalizedHeaders.indexOfFirst { 
                it.contains("shift type") || it.contains("type", ignoreCase = true)
            }.takeIf { it >= 0 } ?: -1
            
            // Validate required columns (Platform is optional, will use default)
            if (dateIdx < 0 || startIdx < 0 || endIdx < 0) {
                throw IllegalArgumentException("Missing required columns: Date, Shift Start Time, Shift End Time")
            }

            var line: Array<String>?
            var rowNumber = 1
            var successCount = 0
            var errorCount = 0
            
            while (reader.readNext().also { line = it } != null) {
                try {
                    val row = line!!
                    if (row.isEmpty() || row.all { it.isBlank() }) {
                        continue // Skip empty rows
                    }
                    
                    if (row.size <= maxOf(dateIdx, startIdx, endIdx)) {
                        errorCount++
                        continue // Skip rows with insufficient columns
                    }
                    
                    // Parse date - try multiple formats
                    val date = parseDate(row[dateIdx].trim())
                    
                    // Parse times - handle AM/PM format
                    val startTime = parseTime(row[startIdx].trim())
                    val endTime = parseTime(row[endIdx].trim())
                    
                    // Get platform (use default if not in CSV)
                    val platform = if (platformIdx >= 0 && row.size > platformIdx && row[platformIdx].trim().isNotEmpty()) {
                        row[platformIdx].trim()
                    } else {
                        defaultPlatform
                    }
                    
                    // Get hours worked (use from CSV if available, otherwise calculate)
                    val totalHours = if (hoursWorkedIdx >= 0 && row.size > hoursWorkedIdx) {
                        row[hoursWorkedIdx].trim().toDoubleOrNull() ?: calculateHours(startTime, endTime)
                    } else {
                        calculateHours(startTime, endTime)
                    }
                    
                    // Get tips
                    val onlineTips = if (onlineTipsIdx >= 0 && row.size > onlineTipsIdx) {
                        row[onlineTipsIdx].trim().toDoubleOrNull() ?: 0.0
                    } else 0.0
                    
                    val cashTips = if (cashTipsIdx >= 0 && row.size > cashTipsIdx) {
                        row[cashTipsIdx].trim().toDoubleOrNull() ?: 0.0
                    } else 0.0
                    
                    // Get total tips (use from CSV if available, otherwise calculate)
                    val totalTips = if (totalTipsIdx >= 0 && row.size > totalTipsIdx && row[totalTipsIdx].trim().isNotEmpty()) {
                        row[totalTipsIdx].trim().toDoubleOrNull() ?: (onlineTips + cashTips)
                    } else {
                        onlineTips + cashTips
                    }
                    
                    // Get orders
                    val orders = if (ordersIdx >= 0 && row.size > ordersIdx) {
                        row[ordersIdx].trim().toIntOrNull() ?: 0
                    } else 0
                    
                    // Get shift type
                    val shiftType = if (shiftTypeIdx >= 0 && row.size > shiftTypeIdx && row[shiftTypeIdx].trim().isNotEmpty()) {
                        val type = row[shiftTypeIdx].trim()
                        if (type.equals("Full", ignoreCase = true) || type.equals("Half", ignoreCase = true)) {
                            type
                        } else {
                            "Full" // Default
                        }
                    } else {
                        "Full" // Default
                    }
                    
                    shifts.add(
                        RiderShift(
                            date = date,
                            platform = platform,
                            shiftStartTime = startTime,
                            shiftEndTime = endTime,
                            totalHours = totalHours,
                            onlineTips = onlineTips,
                            cashTips = cashTips,
                            totalTips = totalTips,
                            orders = orders,
                            shiftType = shiftType,
                            notes = "Imported via CSV"
                        )
                    )
                    successCount++
                    rowNumber++
                } catch (e: Exception) {
                    errorCount++
                    // Skip malformed rows - log but continue
                    println("Skipping row $rowNumber: ${e.message}")
                    rowNumber++
                }
            }
            
            println("CSV Import completed: $successCount successful, $errorCount errors")
        }
        return shifts
    }
    
    private fun parseDate(dateStr: String): LocalDate {
        // Try multiple date formats
        val formats = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
        )
        
        for (formatter in formats) {
            try {
                return LocalDate.parse(dateStr.trim(), formatter)
            } catch (_: Exception) {
                // Try next format
            }
        }
        throw IllegalArgumentException("Unable to parse date: $dateStr")
    }
    
    private fun parseTime(timeStr: String): LocalTime {
        val trimmed = timeStr.trim()
        
        // Try AM/PM format first (e.g., "5:00 PM", "8:30 AM")
        try {
            val formatter = DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH)
            return LocalTime.parse(trimmed, formatter)
        } catch (_: Exception) {
            // Try 24-hour format
        }
        
        // Try standard 24-hour formats
        val formats = listOf(
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss")
        )
        
        for (formatter in formats) {
            try {
                return LocalTime.parse(trimmed, formatter)
            } catch (_: Exception) {
                // Try next format
            }
        }
        
        throw IllegalArgumentException("Unable to parse time: $timeStr")
    }

    private fun calculateHours(start: LocalTime, end: LocalTime): Double {
        val duration = java.time.Duration.between(start, end)
        val minutes = if (duration.isNegative) {
            duration.plusDays(1).toMinutes()
        } else {
            duration.toMinutes()
        }
        return minutes / 60.0
    }
}

package com.example.ridertipstracker.data.model

import java.time.LocalDate

/**
 * Data class for weekly summary results.
 */
data class WeeklySummary(
    val weekStartDate: LocalDate,
    val weekEndDate: LocalDate,
    val totalShifts: Int,
    val totalHours: Double,
    val totalTips: Double,
    val totalOrders: Int,
    val averageTipsPerHour: Double
)

/**
 * Data class for monthly summary results.
 */
data class MonthlySummary(
    val year: Int,
    val month: Int,
    val totalShifts: Int,
    val totalHours: Double,
    val totalTips: Double,
    val totalOrders: Int,
    val averageTipsPerHour: Double
)

/**
 * Raw data class for weekly summary from Room query.
 */
data class WeeklySummaryRaw(
    val totalShifts: Int,
    val totalHours: Double,
    val totalTips: Double,
    val totalOrders: Int
)

/**
 * Raw data class for monthly summary from Room query.
 */
data class MonthlySummaryRaw(
    val totalShifts: Int,
    val totalHours: Double,
    val totalTips: Double,
    val totalOrders: Int
)

/**
 * Data class for weekday tips grouping.
 */
data class WeekdayTips(
    val weekday: String,
    val totalTips: Double,
    val shiftCount: Int,
    val averageTips: Double
)

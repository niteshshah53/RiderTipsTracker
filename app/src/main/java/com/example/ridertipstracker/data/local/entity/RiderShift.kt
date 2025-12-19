package com.example.ridertipstracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

/**
 * Enhanced Entity representing a delivery rider shift.
 */
@Entity(tableName = "rider_shifts")
data class RiderShift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val platform: String, // Uber Eats / Lieferando / Flink
    val shiftStartTime: LocalTime,
    val shiftEndTime: LocalTime,
    val totalHours: Double,
    val onlineTips: Double,
    val cashTips: Double,
    val totalTips: Double,
    val orders: Int,
    val shiftType: String, // Full / Half
    val notes: String?
)

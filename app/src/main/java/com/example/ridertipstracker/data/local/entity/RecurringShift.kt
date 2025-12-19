package com.example.ridertipstracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "recurring_shifts")
data class RecurringShift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // DayOfWeek enum value (1=Monday, 7=Sunday)
    val startTime: LocalTime,
    val endTime: LocalTime,
    val shiftType: String, // "Full" / "Half"
    val isActive: Boolean = true,
    val createdAt: LocalDate = LocalDate.now(),
    val notes: String? = null
)


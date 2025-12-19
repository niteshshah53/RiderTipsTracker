package com.example.ridertipstracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "daily", "weekly", "monthly"
    val targetAmount: Double,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val isActive: Boolean = true,
    val createdAt: LocalDate = LocalDate.now()
)


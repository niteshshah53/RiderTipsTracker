package com.example.ridertipstracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_preferences")
data class NotificationPreference(
    @PrimaryKey
    val id: String, // "shift_reminder", "goal_achievement", "weekly_summary", etc.
    val isEnabled: Boolean = true,
    val time: String? = null, // For time-based notifications (e.g., "09:00")
    val days: String? = null // Comma-separated days (e.g., "1,2,3" for Mon, Tue, Wed)
)


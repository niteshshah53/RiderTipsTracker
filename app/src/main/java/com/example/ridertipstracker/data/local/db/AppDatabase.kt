package com.example.ridertipstracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ridertipstracker.data.local.dao.*
import com.example.ridertipstracker.data.local.entity.*

@Database(
    entities = [
        RiderShift::class,
        Goal::class,
        RecurringShift::class,
        NotificationPreference::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(com.example.ridertipstracker.data.local.db.DateTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun riderShiftDao(): RiderShiftDao
    abstract fun goalDao(): GoalDao
    abstract fun recurringShiftDao(): RecurringShiftDao
    abstract fun notificationPreferenceDao(): NotificationPreferenceDao

    companion object {
        const val DATABASE_NAME = "rider_tips_tracker_db"
    }
}

package com.example.ridertipstracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ridertipstracker.data.local.dao.RiderShiftDao
import com.example.ridertipstracker.data.local.entity.RiderShift

@Database(entities = [RiderShift::class], version = 2, exportSchema = false)
@TypeConverters(com.example.ridertipstracker.data.local.db.DateTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun riderShiftDao(): RiderShiftDao

    companion object {
        const val DATABASE_NAME = "rider_tips_tracker_db"
    }
}

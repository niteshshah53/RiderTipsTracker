package com.example.ridertipstracker.data.local.dao

import androidx.room.*
import com.example.ridertipstracker.data.local.entity.RecurringShift
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringShiftDao {
    @Query("SELECT * FROM recurring_shifts WHERE isActive = 1 ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllActiveRecurringShifts(): Flow<List<RecurringShift>>

    @Query("SELECT * FROM recurring_shifts ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllRecurringShifts(): Flow<List<RecurringShift>>

    @Query("SELECT * FROM recurring_shifts WHERE dayOfWeek = :dayOfWeek AND isActive = 1")
    fun getRecurringShiftsByDay(dayOfWeek: Int): Flow<List<RecurringShift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringShift(recurringShift: RecurringShift)

    @Update
    suspend fun updateRecurringShift(recurringShift: RecurringShift)

    @Delete
    suspend fun deleteRecurringShift(recurringShift: RecurringShift)
}


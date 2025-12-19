package com.example.ridertipstracker.repository

import com.example.ridertipstracker.data.local.dao.RecurringShiftDao
import com.example.ridertipstracker.data.local.entity.RecurringShift
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringShiftRepository @Inject constructor(
    private val recurringShiftDao: RecurringShiftDao
) {
    fun getAllActiveRecurringShifts(): Flow<List<RecurringShift>> = recurringShiftDao.getAllActiveRecurringShifts()
    fun getAllRecurringShifts(): Flow<List<RecurringShift>> = recurringShiftDao.getAllRecurringShifts()
    fun getRecurringShiftsByDay(dayOfWeek: Int): Flow<List<RecurringShift>> = recurringShiftDao.getRecurringShiftsByDay(dayOfWeek)
    suspend fun insertRecurringShift(recurringShift: RecurringShift) = recurringShiftDao.insertRecurringShift(recurringShift)
    suspend fun updateRecurringShift(recurringShift: RecurringShift) = recurringShiftDao.updateRecurringShift(recurringShift)
    suspend fun deleteRecurringShift(recurringShift: RecurringShift) = recurringShiftDao.deleteRecurringShift(recurringShift)
}


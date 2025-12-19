package com.example.ridertipstracker.repository

import com.example.ridertipstracker.data.local.dao.RiderShiftDao
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.example.ridertipstracker.data.model.MonthlySummaryRaw
import com.example.ridertipstracker.data.model.Platform
import com.example.ridertipstracker.data.model.WeekdayTips
import com.example.ridertipstracker.data.model.WeeklySummaryRaw
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiderShiftRepository @Inject constructor(
    private val riderShiftDao: RiderShiftDao
) {
    suspend fun insertShift(shift: RiderShift) = riderShiftDao.insertShift(shift)

    suspend fun insertShifts(shifts: List<RiderShift>) = riderShiftDao.insertShifts(shifts)

    suspend fun updateShift(shift: RiderShift) = riderShiftDao.updateShift(shift)

    suspend fun deleteShift(shift: RiderShift) = riderShiftDao.deleteShift(shift)

    suspend fun getShiftById(id: Long): RiderShift? = riderShiftDao.getShiftById(id)

    fun getShiftsBetweenDates(start: LocalDate, end: LocalDate): Flow<List<RiderShift>> =
        riderShiftDao.getShiftsBetweenDates(start, end)

    fun getTotalTipsBetweenDates(start: LocalDate, end: LocalDate): Flow<Double?> =
        riderShiftDao.getTotalTipsBetweenDates(start, end)

    fun getAllShifts(): Flow<List<RiderShift>> = riderShiftDao.getAllShifts()

    suspend fun getWeeklySummary(start: LocalDate, end: LocalDate): WeeklySummaryRaw =
        riderShiftDao.getWeeklySummary(start, end)

    suspend fun getMonthlySummary(start: LocalDate, end: LocalDate): MonthlySummaryRaw =
        riderShiftDao.getMonthlySummary(start, end)

    fun getAverageTipsPerHour(): Flow<Double?> = riderShiftDao.getAverageTipsPerHour()

    fun getTotalTipsByWeekday(): Flow<List<WeekdayTips>> = riderShiftDao.getTotalTipsByWeekday()

    // Additional methods for ViewModels
    fun getShiftsByDateRange(startDate: Long, endDate: Long): Flow<List<RiderShift>> {
        val start = LocalDate.ofEpochDay(startDate)
        val end = LocalDate.ofEpochDay(endDate)
        return riderShiftDao.getShiftsBetweenDates(start, end)
    }

    fun getShiftsByPlatform(platform: Platform): Flow<List<RiderShift>> {
        val platformString = when (platform) {
            Platform.UBER_EATS -> "Uber Eats"
            Platform.LIEFERANDO -> "Lieferando"
            Platform.FLINK -> "Flink"
        }
        return getAllShifts().map { shifts ->
            shifts.filter { it.platform == platformString }
        }
    }

    fun getShiftsByPlatformAndDateRange(
        platform: Platform,
        startDate: Long,
        endDate: Long
    ): Flow<List<RiderShift>> {
        val platformString = when (platform) {
            Platform.UBER_EATS -> "Uber Eats"
            Platform.LIEFERANDO -> "Lieferando"
            Platform.FLINK -> "Flink"
        }
        val start = LocalDate.ofEpochDay(startDate)
        val end = LocalDate.ofEpochDay(endDate)
        return riderShiftDao.getShiftsBetweenDates(start, end).map { shifts ->
            shifts.filter { it.platform == platformString }
        }
    }

    fun getTotalEarnings(): Flow<Double?> = getAllShifts().map { shifts ->
        shifts.sumOf { it.totalTips } // Using totalTips as earnings for now
    }

    fun getTotalTips(): Flow<Double?> = getAllShifts().map { shifts ->
        shifts.sumOf { it.totalTips }
    }

    fun getTotalEarningsByPlatform(platform: Platform): Flow<Double?> {
        return getShiftsByPlatform(platform).map { shifts ->
            shifts.sumOf { it.totalTips }
        }
    }

    fun getTotalTipsByPlatform(platform: Platform): Flow<Double?> {
        return getShiftsByPlatform(platform).map { shifts ->
            shifts.sumOf { it.totalTips }
        }
    }

    fun getUpcomingShifts(): Flow<List<RiderShift>> {
        val today = LocalDate.now()
        return riderShiftDao.getUpcomingShifts(today)
    }
}

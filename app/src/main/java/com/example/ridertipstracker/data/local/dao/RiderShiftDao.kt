package com.example.ridertipstracker.data.local.dao

import androidx.room.*
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.example.ridertipstracker.data.model.MonthlySummaryRaw
import com.example.ridertipstracker.data.model.WeekdayTips
import com.example.ridertipstracker.data.model.WeeklySummaryRaw
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RiderShiftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: RiderShift)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShifts(shifts: List<RiderShift>)

    @Update
    suspend fun updateShift(shift: RiderShift)

    @Delete
    suspend fun deleteShift(shift: RiderShift)

    @Query("SELECT * FROM rider_shifts WHERE date >= :start AND date <= :end ORDER BY date DESC")
    fun getShiftsBetweenDates(start: LocalDate, end: LocalDate): Flow<List<RiderShift>>

    @Query("SELECT SUM(totalTips) FROM rider_shifts WHERE date >= :start AND date <= :end")
    fun getTotalTipsBetweenDates(start: LocalDate, end: LocalDate): Flow<Double?>

    @Query("SELECT * FROM rider_shifts ORDER BY date DESC")
    fun getAllShifts(): Flow<List<RiderShift>>

    @Query("SELECT * FROM rider_shifts WHERE id = :id")
    suspend fun getShiftById(id: Long): RiderShift?

    @Query("SELECT * FROM rider_shifts WHERE date > :today OR (date = :today AND (totalTips = 0 OR orders = 0)) ORDER BY date ASC, shiftStartTime ASC")
    fun getUpcomingShifts(today: LocalDate): Flow<List<RiderShift>>

    @Query("""
        SELECT 
            COUNT(*) as totalShifts,
            IFNULL(SUM(totalHours), 0.0) as totalHours,
            IFNULL(SUM(totalTips), 0.0) as totalTips,
            IFNULL(SUM(orders), 0) as totalOrders
        FROM rider_shifts
        WHERE date >= :start AND date <= :end
    """)
    suspend fun getWeeklySummary(start: LocalDate, end: LocalDate): WeeklySummaryRaw

    @Query("""
        SELECT 
            COUNT(*) as totalShifts,
            IFNULL(SUM(totalHours), 0.0) as totalHours,
            IFNULL(SUM(totalTips), 0.0) as totalTips,
            IFNULL(SUM(orders), 0) as totalOrders
        FROM rider_shifts
        WHERE date >= :start AND date <= :end
    """)
    suspend fun getMonthlySummary(start: LocalDate, end: LocalDate): MonthlySummaryRaw

    @Query("SELECT CASE WHEN SUM(totalHours) > 0 THEN SUM(totalTips) / SUM(totalHours) ELSE 0.0 END FROM rider_shifts")
    fun getAverageTipsPerHour(): Flow<Double?>

    @Query("""
        SELECT 
            CASE strftime('%w', date)
                WHEN '0' THEN 'Sunday' WHEN '1' THEN 'Monday' WHEN '2' THEN 'Tuesday'
                WHEN '3' THEN 'Wednesday' WHEN '4' THEN 'Thursday' WHEN '5' THEN 'Friday'
                WHEN '6' THEN 'Saturday'
            END as weekday, 
            SUM(totalTips) as totalTips, 
            COUNT(*) as shiftCount,
            SUM(totalTips) / COUNT(*) as averageTips
        FROM rider_shifts 
        GROUP BY weekday
        ORDER BY strftime('%w', date) ASC
    """)
    fun getTotalTipsByWeekday(): Flow<List<WeekdayTips>>
}

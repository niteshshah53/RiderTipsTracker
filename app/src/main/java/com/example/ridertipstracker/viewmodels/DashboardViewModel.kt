package com.example.ridertipstracker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.example.ridertipstracker.data.model.WeekdayTips
import com.example.ridertipstracker.repository.RiderShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: RiderShiftRepository
) : ViewModel() {

    private val today = LocalDate.now()
    private val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth())
    private val lastWeekStart = startOfWeek.minusWeeks(1)
    private val lastWeekEnd = startOfWeek.minusDays(1)
    private val lastMonthStart = startOfMonth.minusMonths(1)
    private val lastMonthEnd = startOfMonth.minusDays(1)

    val todayTips: Flow<Double> = repository.getTotalTipsBetweenDates(today, today)
        .map { it ?: 0.0 }
    
    val weeklyTips: Flow<Double> = repository.getTotalTipsBetweenDates(startOfWeek, today)
        .map { it ?: 0.0 }

    val monthlyTips: Flow<Double> = repository.getTotalTipsBetweenDates(startOfMonth, today)
        .map { it ?: 0.0 }

    val avgTipsPerHour: Flow<Double> = repository.getAverageTipsPerHour()
        .map { it ?: 0.0 }

    val bestWeekday: Flow<String?> = repository.getTotalTipsByWeekday()
        .map { list -> list.maxByOrNull { it.averageTips }?.weekday }

    val allShifts: Flow<List<RiderShift>> = repository.getAllShifts()

    val bestShiftType: Flow<String?> = allShifts.map { shifts ->
        if (shifts.isEmpty()) null else {
            val fullShifts = shifts.filter { it.shiftType == "Full" }
            val halfShifts = shifts.filter { it.shiftType == "Half" }
            val fullAvg = if (fullShifts.isNotEmpty()) fullShifts.map { it.totalTips }.average() else 0.0
            val halfAvg = if (halfShifts.isNotEmpty()) halfShifts.map { it.totalTips }.average() else 0.0
            when {
                fullAvg > halfAvg && fullShifts.isNotEmpty() -> "Full"
                halfAvg > fullAvg && halfShifts.isNotEmpty() -> "Half"
                else -> null
            }
        }
    }

    val totalShifts: Flow<Int> = allShifts.map { it.size }

    val averageTipsPerShift: Flow<Double> = allShifts.map { shifts ->
        if (shifts.isEmpty()) 0.0 else shifts.map { it.totalTips }.average()
    }

    val weeklySummary: Flow<WeeklySummaryData> = combine(
        repository.getShiftsBetweenDates(startOfWeek, today),
        repository.getShiftsBetweenDates(lastWeekStart, lastWeekEnd)
    ) { currentWeek, lastWeek ->
        val currentTotal = currentWeek.sumOf { it.totalTips }
        val lastTotal = lastWeek.sumOf { it.totalTips }
        val trend = if (lastTotal > 0) {
            ((currentTotal - lastTotal) / lastTotal * 100).toInt()
        } else 0
        WeeklySummaryData(
            totalTips = currentTotal,
            totalShifts = currentWeek.size,
            trend = trend
        )
    }

    val monthlySummary: Flow<MonthlySummaryData> = combine(
        repository.getShiftsBetweenDates(startOfMonth, today),
        repository.getShiftsBetweenDates(lastMonthStart, lastMonthEnd)
    ) { currentMonth, lastMonth ->
        val currentTotal = currentMonth.sumOf { it.totalTips }
        val lastTotal = lastMonth.sumOf { it.totalTips }
        val trend = if (lastTotal > 0) {
            ((currentTotal - lastTotal) / lastTotal * 100).toInt()
        } else 0
        MonthlySummaryData(
            totalTips = currentTotal,
            totalShifts = currentMonth.size,
            trend = trend
        )
    }

    val upcomingShifts: Flow<List<RiderShift>> = repository.getUpcomingShifts()

    // Daily tips breakdown for current week
    val weeklyDailyTips: Flow<List<DailyTips>> = repository.getShiftsBetweenDates(startOfWeek, today)
        .map { shifts ->
            val dailyMap = shifts.groupBy { it.date }
            (0..6).map { dayOffset ->
                val date = startOfWeek.plusDays(dayOffset.toLong())
                val dayShifts = dailyMap[date] ?: emptyList()
                val totalTips = dayShifts.sumOf { it.totalTips }
                val dayName = when (date.dayOfWeek) {
                    DayOfWeek.MONDAY -> "Mon"
                    DayOfWeek.TUESDAY -> "Tue"
                    DayOfWeek.WEDNESDAY -> "Wed"
                    DayOfWeek.THURSDAY -> "Thu"
                    DayOfWeek.FRIDAY -> "Fri"
                    DayOfWeek.SATURDAY -> "Sat"
                    DayOfWeek.SUNDAY -> "Sun"
                }
                DailyTips(
                    date = date,
                    dayName = dayName,
                    tips = totalTips,
                    hasData = dayShifts.isNotEmpty()
                )
            }
        }

    // Last 3 days tips
    val last3DaysTips: Flow<List<DailyTips>> = repository.getShiftsBetweenDates(today.minusDays(2), today)
        .map { shifts ->
            val dailyMap = shifts.groupBy { it.date }
            (2 downTo 0).map { dayOffset ->
                val date = today.minusDays(dayOffset.toLong())
                val dayShifts = dailyMap[date] ?: emptyList()
                val totalTips = dayShifts.sumOf { it.totalTips }
                val dayName = when {
                    date == today -> "Today"
                    date == today.minusDays(1) -> "Yesterday"
                    else -> when (date.dayOfWeek) {
                        DayOfWeek.MONDAY -> "Mon"
                        DayOfWeek.TUESDAY -> "Tue"
                        DayOfWeek.WEDNESDAY -> "Wed"
                        DayOfWeek.THURSDAY -> "Thu"
                        DayOfWeek.FRIDAY -> "Fri"
                        DayOfWeek.SATURDAY -> "Sat"
                        DayOfWeek.SUNDAY -> "Sun"
                    }
                }
                DailyTips(
                    date = date,
                    dayName = dayName,
                    tips = totalTips,
                    hasData = dayShifts.isNotEmpty()
                )
            }
        }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

data class WeeklySummaryData(
    val totalTips: Double,
    val totalShifts: Int,
    val trend: Int
)

data class MonthlySummaryData(
    val totalTips: Double,
    val totalShifts: Int,
    val trend: Int
)

data class DailyTips(
    val date: LocalDate,
    val dayName: String,
    val tips: Double,
    val hasData: Boolean
)

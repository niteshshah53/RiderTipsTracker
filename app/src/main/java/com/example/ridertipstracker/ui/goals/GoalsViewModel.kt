package com.example.ridertipstracker.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridertipstracker.data.local.entity.Goal
import com.example.ridertipstracker.repository.GoalRepository
import com.example.ridertipstracker.repository.RiderShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val shiftRepository: RiderShiftRepository
) : ViewModel() {

    val goals: StateFlow<List<Goal>> = goalRepository.getAllActiveGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addGoal(type: String, targetAmount: Double, endDate: LocalDate?) {
        viewModelScope.launch {
            val startDate = when (type) {
                "daily" -> LocalDate.now()
                "weekly" -> LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                "monthly" -> LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
                else -> LocalDate.now()
            }
            
            goalRepository.insertGoal(
                Goal(
                    type = type,
                    targetAmount = targetAmount,
                    startDate = startDate,
                    endDate = endDate,
                    isActive = true
                )
            )
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goal)
        }
    }

    fun getGoalProgress(goal: Goal): Flow<Double> {
        // Calculate progress based on goal type
        return when (goal.type) {
            "daily" -> {
                shiftRepository.getShiftsBetweenDates(
                    LocalDate.now(),
                    LocalDate.now()
                ).map { shifts -> shifts.sumOf { it.totalTips } }
            }
            "weekly" -> {
                val weekStart = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                shiftRepository.getShiftsBetweenDates(
                    weekStart,
                    LocalDate.now()
                ).map { shifts -> shifts.sumOf { it.totalTips } }
            }
            "monthly" -> {
                val monthStart = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
                shiftRepository.getShiftsBetweenDates(
                    monthStart,
                    LocalDate.now()
                ).map { shifts -> shifts.sumOf { it.totalTips } }
            }
            else -> flowOf(0.0)
        }
    }
}


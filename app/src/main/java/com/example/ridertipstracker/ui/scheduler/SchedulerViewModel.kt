package com.example.ridertipstracker.ui.scheduler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridertipstracker.data.local.entity.RecurringShift
import com.example.ridertipstracker.repository.RecurringShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SchedulerViewModel @Inject constructor(
    private val repository: RecurringShiftRepository
) : ViewModel() {

    val recurringShifts: StateFlow<List<RecurringShift>> = repository.getAllRecurringShifts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addRecurringShift(dayOfWeek: Int, startTime: LocalTime, endTime: LocalTime, shiftType: String) {
        viewModelScope.launch {
            repository.insertRecurringShift(
                RecurringShift(
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    shiftType = shiftType
                )
            )
        }
    }

    fun toggleShift(shift: RecurringShift) {
        viewModelScope.launch {
            repository.updateRecurringShift(shift.copy(isActive = !shift.isActive))
        }
    }

    fun deleteShift(shift: RecurringShift) {
        viewModelScope.launch {
            repository.deleteRecurringShift(shift)
        }
    }
}


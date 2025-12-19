package com.example.ridertipstracker.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.example.ridertipstracker.repository.RiderShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: RiderShiftRepository
) : ViewModel() {

    private val _startDate = MutableStateFlow<LocalDate>(LocalDate.now().minusDays(30))
    private val _endDate = MutableStateFlow<LocalDate>(LocalDate.now())

    val filteredShifts: StateFlow<List<RiderShift>> = combine(
        _startDate, 
        _endDate
    ) { start, end ->
        repository.getAllShifts().map { shifts ->
            shifts.filter { it.date >= start && it.date <= end }
        }
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTips: StateFlow<Double> = filteredShifts.map { shifts ->
        shifts.sumOf { it.totalTips }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun updateDateRange(start: LocalDate, end: LocalDate) {
        _startDate.value = start
        _endDate.value = end
    }
}

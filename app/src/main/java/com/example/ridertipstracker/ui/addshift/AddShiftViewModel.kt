package com.example.ridertipstracker.ui.addshift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.example.ridertipstracker.repository.RiderShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddShiftViewModel @Inject constructor(
    private val repository: RiderShiftRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddShiftUiState())
    val uiState: StateFlow<AddShiftUiState> = _uiState.asStateFlow()

    fun loadShift(shiftId: Long) {
        viewModelScope.launch {
            repository.getShiftById(shiftId)?.let { shift ->
                _uiState.update {
                    it.copy(
                        date = shift.date,
                        platform = shift.platform,
                        startTime = shift.shiftStartTime,
                        endTime = shift.shiftEndTime,
                        totalHours = shift.totalHours,
                        onlineTips = shift.onlineTips,
                        cashTips = shift.cashTips,
                        totalTips = shift.totalTips,
                        orders = shift.orders,
                        shiftType = shift.shiftType,
                        notes = shift.notes ?: "",
                        shiftId = shift.id,
                        saveSuccess = false,
                        error = null
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AddShiftUiState()
    }

    fun updateDate(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun updatePlatform(platform: String) {
        _uiState.update { it.copy(platform = platform) }
    }

    fun updateStartTime(time: LocalTime) {
        _uiState.update { 
            val newState = it.copy(startTime = time)
            newState.copy(totalHours = calculateHours(time, newState.endTime))
        }
    }

    fun updateEndTime(time: LocalTime) {
        _uiState.update { 
            val newState = it.copy(endTime = time)
            newState.copy(totalHours = calculateHours(newState.startTime, time))
        }
    }

    fun updateOnlineTips(tips: String) {
        val value = tips.toDoubleOrNull() ?: 0.0
        _uiState.update { 
            val total = value + it.cashTips
            it.copy(onlineTips = value, totalTips = total) 
        }
    }

    fun updateCashTips(tips: String) {
        val value = tips.toDoubleOrNull() ?: 0.0
        _uiState.update { 
            val total = it.onlineTips + value
            it.copy(cashTips = value, totalTips = total) 
        }
    }

    fun updateOrders(orders: String) {
        val value = orders.toIntOrNull() ?: 0
        _uiState.update { it.copy(orders = value) }
    }

    fun updateShiftType(type: String) {
        _uiState.update { it.copy(shiftType = type) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun saveShift() {
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val shift = RiderShift(
                    id = state.shiftId ?: 0L,
                    date = state.date ?: LocalDate.now(),
                    platform = state.platform,
                    shiftStartTime = state.startTime,
                    shiftEndTime = state.endTime,
                    totalHours = state.totalHours,
                    onlineTips = state.onlineTips,
                    cashTips = state.cashTips,
                    totalTips = state.totalTips,
                    orders = state.orders,
                    shiftType = state.shiftType,
                    notes = state.notes
                )
                if (state.shiftId != null) {
                    repository.updateShift(shift)
                } else {
                    repository.insertShift(shift)
                }
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    private fun calculateHours(start: LocalTime, end: LocalTime): Double {
        val duration = Duration.between(start, end)
        val minutes = if (duration.isNegative) {
            duration.plusDays(1).toMinutes()
        } else {
            duration.toMinutes()
        }
        return String.format("%.2f", minutes / 60.0).toDouble()
    }
}

data class AddShiftUiState(
    val shiftId: Long? = null,
    val date: LocalDate? = LocalDate.now(),
    val platform: String = "", // Platform removed - kept for database compatibility
    val startTime: LocalTime = LocalTime.of(17, 0),
    val endTime: LocalTime = LocalTime.of(22, 0),
    val totalHours: Double = 5.0,
    val onlineTips: Double = 0.0,
    val cashTips: Double = 0.0,
    val totalTips: Double = 0.0,
    val orders: Int = 0,
    val shiftType: String = "Full",
    val notes: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

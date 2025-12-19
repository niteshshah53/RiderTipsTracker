package com.example.ridertipstracker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridertipstracker.data.model.Platform
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.example.ridertipstracker.repository.RiderShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel for the Add Shift screen.
 * 
 * Manages the state and business logic for creating and editing rider shifts.
 */
@HiltViewModel
class AddShiftViewModel @Inject constructor(
    private val repository: RiderShiftRepository
) : ViewModel() {
    
    /**
     * UI state for the add shift screen.
     */
    private val _uiState = MutableStateFlow(AddShiftUiState())
    val uiState: StateFlow<AddShiftUiState> = _uiState.asStateFlow()
    
    /**
     * Save the current shift.
     */
    fun saveShift() {
        val currentState = _uiState.value
        val platformString = when (currentState.selectedPlatform) {
            Platform.UBER_EATS -> "Uber Eats"
            Platform.LIEFERANDO -> "Lieferando"
            Platform.FLINK -> "Flink"
            null -> return
        }
        
        val shift = RiderShift(
            id = currentState.shiftId ?: 0,
            date = currentState.date ?: return,
            platform = platformString,
            shiftStartTime = currentState.startTime ?: return,
            shiftEndTime = currentState.endTime ?: return,
            totalHours = currentState.totalHours ?: 0.0,
            onlineTips = currentState.onlineTips ?: 0.0,
            cashTips = currentState.cashTips ?: 0.0,
            totalTips = currentState.totalTips ?: 0.0,
            orders = currentState.orders ?: 0,
            shiftType = currentState.shiftType ?: "Full",
            notes = currentState.notes
        )
        
        viewModelScope.launch {
            try {
                if (currentState.shiftId != null) {
                    repository.updateShift(shift)
                } else {
                    repository.insertShift(shift)
                }
                _uiState.value = _uiState.value.copy(
                    isSaved = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to save shift"
                )
            }
        }
    }
    
    /**
     * Update the selected platform.
     * @param platform The platform to select
     */
    fun updatePlatform(platform: Platform) {
        _uiState.value = _uiState.value.copy(selectedPlatform = platform)
    }
    
    /**
     * Update the date.
     * @param date The date
     */
    fun updateDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(date = date)
    }
    
    /**
     * Update the start time.
     * @param startTime The start time
     */
    fun updateStartTime(startTime: LocalTime) {
        _uiState.value = _uiState.value.copy(startTime = startTime)
    }
    
    /**
     * Update the end time.
     * @param endTime The end time
     */
    fun updateEndTime(endTime: LocalTime) {
        _uiState.value = _uiState.value.copy(endTime = endTime)
    }
    
    /**
     * Update online tips.
     * @param tips The online tips amount
     */
    fun updateOnlineTips(tips: Double) {
        _uiState.value = _uiState.value.copy(onlineTips = tips)
        updateTotalTips()
    }
    
    /**
     * Update cash tips.
     * @param tips The cash tips amount
     */
    fun updateCashTips(tips: Double) {
        _uiState.value = _uiState.value.copy(cashTips = tips)
        updateTotalTips()
    }
    
    /**
     * Update total tips (calculated from online + cash).
     */
    private fun updateTotalTips() {
        val total = (_uiState.value.onlineTips ?: 0.0) + (_uiState.value.cashTips ?: 0.0)
        _uiState.value = _uiState.value.copy(totalTips = total)
    }
    
    /**
     * Update total hours.
     * @param hours The total hours worked
     */
    fun updateTotalHours(hours: Double) {
        _uiState.value = _uiState.value.copy(totalHours = hours)
    }
    
    /**
     * Update orders count.
     * @param orders The number of orders
     */
    fun updateOrders(orders: Int) {
        _uiState.value = _uiState.value.copy(orders = orders)
    }
    
    /**
     * Update shift type.
     * @param shiftType The shift type (Full or Half)
     */
    fun updateShiftType(shiftType: String) {
        _uiState.value = _uiState.value.copy(shiftType = shiftType)
    }
    
    /**
     * Update the notes.
     * @param notes The notes text
     */
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    /**
     * Load an existing shift for editing.
     * @param shiftId The ID of the shift to load
     */
    fun loadShift(shiftId: Long) {
        viewModelScope.launch {
            val shift = repository.getShiftById(shiftId)
            shift?.let {
                val platform = when (it.platform) {
                    "Uber Eats" -> Platform.UBER_EATS
                    "Lieferando" -> Platform.LIEFERANDO
                    "Flink" -> Platform.FLINK
                    else -> Platform.UBER_EATS
                }
                _uiState.value = AddShiftUiState(
                    shiftId = it.id,
                    selectedPlatform = platform,
                    date = it.date,
                    startTime = it.shiftStartTime,
                    endTime = it.shiftEndTime,
                    totalHours = it.totalHours,
                    onlineTips = it.onlineTips,
                    cashTips = it.cashTips,
                    totalTips = it.totalTips,
                    orders = it.orders,
                    shiftType = it.shiftType,
                    notes = it.notes
                )
            }
        }
    }
    
    /**
     * Reset the form to initial state.
     */
    fun resetForm() {
        _uiState.value = AddShiftUiState()
    }
}

/**
 * UI state for the Add Shift screen.
 */
data class AddShiftUiState(
    val shiftId: Long? = null,
    val selectedPlatform: Platform? = null,
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val totalHours: Double? = null,
    val onlineTips: Double? = null,
    val cashTips: Double? = null,
    val totalTips: Double? = null,
    val orders: Int? = null,
    val shiftType: String? = null,
    val notes: String? = null,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)


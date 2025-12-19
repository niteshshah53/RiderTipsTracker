package com.example.ridertipstracker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridertipstracker.data.model.Platform
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.example.ridertipstracker.repository.RiderShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for the Reports screen.
 * 
 * Manages the state and business logic for generating and displaying
 * reports on rider shift data, including statistics and analytics.
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: RiderShiftRepository
) : ViewModel() {
    
    /**
     * UI state for the reports screen.
     */
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()
    
    /**
     * Get shifts filtered by the current report parameters.
     */
    fun getFilteredShifts(): Flow<List<RiderShift>> {
        val state = _uiState.value
        return when {
            state.selectedPlatform != null && state.startDate != null && state.endDate != null -> {
                repository.getShiftsByPlatformAndDateRange(
                    state.selectedPlatform!!,
                    state.startDate!!,
                    state.endDate!!
                )
            }
            state.selectedPlatform != null -> {
                repository.getShiftsByPlatform(state.selectedPlatform!!)
            }
            state.startDate != null && state.endDate != null -> {
                repository.getShiftsByDateRange(state.startDate!!, state.endDate!!)
            }
            else -> {
                repository.getAllShifts()
            }
        }
    }
    
    /**
     * Get total earnings for the current filter.
     */
    fun getFilteredTotalEarnings(): Flow<Double?> {
        val state = _uiState.value
        return if (state.selectedPlatform != null) {
            repository.getTotalEarningsByPlatform(state.selectedPlatform!!)
        } else {
            repository.getTotalEarnings()
        }
    }
    
    /**
     * Get total tips for the current filter.
     */
    fun getFilteredTotalTips(): Flow<Double?> {
        val state = _uiState.value
        return if (state.selectedPlatform != null) {
            repository.getTotalTipsByPlatform(state.selectedPlatform!!)
        } else {
            repository.getTotalTips()
        }
    }
    
    /**
     * Update the selected platform filter.
     * @param platform The platform to filter by, or null to clear filter
     */
    fun updatePlatformFilter(platform: Platform?) {
        _uiState.value = _uiState.value.copy(selectedPlatform = platform)
    }
    
    /**
     * Update the date range filter.
     * @param startDate Start date, or null to clear
     * @param endDate End date, or null to clear
     */
    fun updateDateRange(startDate: LocalDate?, endDate: LocalDate?) {
        _uiState.value = _uiState.value.copy(
            startDate = startDate?.toEpochDay(),
            endDate = endDate?.toEpochDay()
        )
    }
    
    /**
     * Clear all filters.
     */
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedPlatform = null,
            startDate = null,
            endDate = null
        )
    }
    
    /**
     * Set the report type.
     * @param reportType The type of report to generate
     */
    fun setReportType(reportType: ReportType) {
        _uiState.value = _uiState.value.copy(reportType = reportType)
    }
    
    /**
     * Export report data.
     */
    fun exportReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                // Implementation for exporting report
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = e.message ?: "Failed to export report"
                )
            }
        }
    }
}

/**
 * UI state for the Reports screen.
 */
data class ReportsUiState(
    val selectedPlatform: Platform? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val reportType: ReportType = ReportType.SUMMARY,
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Types of reports available.
 */
enum class ReportType {
    SUMMARY,
    BY_PLATFORM,
    BY_DATE_RANGE,
    DETAILED
}


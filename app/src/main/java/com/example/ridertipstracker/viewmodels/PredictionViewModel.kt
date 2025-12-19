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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Prediction screen.
 * 
 * Manages the state and business logic for predicting future earnings
 * and tips based on historical shift data.
 */
@HiltViewModel
class PredictionViewModel @Inject constructor(
    private val repository: RiderShiftRepository
) : ViewModel() {
    
    /**
     * UI state for the prediction screen.
     */
    private val _uiState = MutableStateFlow(PredictionUiState())
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()
    
    /**
     * Get all shifts for analysis.
     */
    val allShifts: Flow<List<RiderShift>> = repository.getAllShifts()
    
    /**
     * Get shifts by platform for platform-specific predictions.
     * @param platform The platform to analyze
     */
    fun getShiftsByPlatform(platform: Platform): Flow<List<RiderShift>> =
        repository.getShiftsByPlatform(platform)
    
    /**
     * Calculate predictions based on historical data.
     * @param platform Optional platform filter
     * @param days Number of days to predict ahead
     */
    fun calculatePredictions(platform: Platform? = null, days: Int = 7) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCalculating = true, errorMessage = null)
            
            try {
                // Get historical data
                val shiftsFlow = if (platform != null) {
                    repository.getShiftsByPlatform(platform)
                } else {
                    repository.getAllShifts()
                }
                
                // Get current value from flow for analysis
                val shifts = shiftsFlow.first()
                
                if (shifts.isNotEmpty()) {
                    val predictions = analyzeShiftsAndPredict(shifts, days)
                    _uiState.value = _uiState.value.copy(
                        isCalculating = false,
                        predictions = predictions
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCalculating = false,
                        errorMessage = "No historical data available for predictions"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCalculating = false,
                    errorMessage = e.message ?: "Failed to calculate predictions"
                )
            }
        }
    }
    
    /**
     * Analyze shifts and generate predictions.
     * @param shifts Historical shift data
     * @param days Number of days to predict
     * @return Prediction results
     */
    private fun analyzeShiftsAndPredict(shifts: List<RiderShift>, days: Int): Predictions {
        // Calculate averages
        val avgTipsPerShift = shifts.map { it.totalTips }.average()
        val avgHoursPerShift = shifts.map { it.totalHours }.average()
        val avgShiftsPerDay = calculateAverageShiftsPerDay(shifts)
        
        // Simple prediction: average * number of days * average shifts per day
        // Using totalTips as earnings for now
        val predictedEarnings = avgTipsPerShift * days * avgShiftsPerDay
        val predictedTips = avgTipsPerShift * days * avgShiftsPerDay
        
        return Predictions(
            predictedEarnings = predictedEarnings,
            predictedTips = predictedTips,
            predictedShifts = (avgShiftsPerDay * days).toInt(),
            averageEarningsPerShift = avgTipsPerShift,
            averageTipsPerShift = avgTipsPerShift,
            averageShiftsPerDay = avgShiftsPerDay,
            daysPredicted = days
        )
    }
    
    /**
     * Calculate average number of shifts per day from historical data.
     * @param shifts Historical shift data
     * @return Average shifts per day
     */
    private fun calculateAverageShiftsPerDay(shifts: List<RiderShift>): Double {
        if (shifts.isEmpty()) return 0.0
        
        val uniqueDates = shifts.map { it.date }.distinct()
        if (uniqueDates.isEmpty()) return 0.0
        
        val totalDays = uniqueDates.size
        return shifts.size.toDouble() / totalDays
    }
    
    /**
     * Update the prediction period.
     * @param days Number of days to predict ahead
     */
    fun updatePredictionPeriod(days: Int) {
        _uiState.value = _uiState.value.copy(predictionPeriodDays = days)
    }
    
    /**
     * Update the selected platform filter.
     * @param platform The platform to filter by, or null for all platforms
     */
    fun updatePlatformFilter(platform: Platform?) {
        _uiState.value = _uiState.value.copy(selectedPlatform = platform)
    }
    
    /**
     * Clear predictions.
     */
    fun clearPredictions() {
        _uiState.value = _uiState.value.copy(
            predictions = null,
            errorMessage = null
        )
    }
}

/**
 * UI state for the Prediction screen.
 */
data class PredictionUiState(
    val selectedPlatform: Platform? = null,
    val predictionPeriodDays: Int = 7,
    val predictions: Predictions? = null,
    val isCalculating: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Prediction results data class.
 */
data class Predictions(
    val predictedEarnings: Double,
    val predictedTips: Double,
    val predictedShifts: Int,
    val averageEarningsPerShift: Double,
    val averageTipsPerShift: Double,
    val averageShiftsPerDay: Double,
    val daysPredicted: Int
)


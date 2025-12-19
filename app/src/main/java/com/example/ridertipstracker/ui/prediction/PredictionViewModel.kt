package com.example.ridertipstracker.ui.prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.example.ridertipstracker.repository.RiderShiftRepository
import com.example.ridertipstracker.utils.PredictionEngine
import com.example.ridertipstracker.utils.PredictionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PredictionViewModel @Inject constructor(
    private val repository: RiderShiftRepository,
    private val predictionEngine: PredictionEngine
) : ViewModel() {

    private val _predictionResult = MutableStateFlow<PredictionResult?>(null)
    val predictionResult: StateFlow<PredictionResult?> = _predictionResult.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    init {
        calculatePredictions()
    }

    fun calculatePredictions() {
        _isCalculating.value = true
        
        repository.getAllShifts()
            .onEach { shifts ->
                _predictionResult.value = predictionEngine.predictNextWeek(shifts)
                _isCalculating.value = false
            }
            .catch { e ->
                _isCalculating.value = false
                // Handle error
            }
            .launchIn(viewModelScope)
    }
}

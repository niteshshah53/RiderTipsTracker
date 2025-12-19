package com.example.ridertipstracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridertipstracker.data.local.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    val uiState: StateFlow<SettingsUiState> = preferencesManager.uiTheme
        .map { isDarkMode ->
            SettingsUiState(isDarkMode = isDarkMode)
        }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState(isDarkMode = false)
        )
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            preferencesManager.updateTheme(!uiState.value.isDarkMode)
        }
    }
    
    fun navigateToImport() {
        // Navigation will be handled by composable
    }
    
    fun navigateToExport() {
        // Navigation will be handled by composable
    }
}

data class SettingsUiState(
    val isDarkMode: Boolean = false
)


package com.example.ridertipstracker.ui.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridertipstracker.utils.BackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupService: BackupService
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBackingUp = true, backupError = null, backupSuccess = false)
            try {
                val success = backupService.createBackup(uri)
                _uiState.value = _uiState.value.copy(
                    isBackingUp = false,
                    backupSuccess = success,
                    backupError = if (!success) "Failed to create backup" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isBackingUp = false,
                    backupError = e.message ?: "Failed to create backup"
                )
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true, restoreError = null, restoreSuccess = false)
            try {
                val success = backupService.restoreBackup(uri)
                _uiState.value = _uiState.value.copy(
                    isRestoring = false,
                    restoreSuccess = success,
                    restoreError = if (!success) "Failed to restore backup" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRestoring = false,
                    restoreError = e.message ?: "Failed to restore backup"
                )
            }
        }
    }
}

data class BackupRestoreUiState(
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val backupSuccess: Boolean = false,
    val restoreSuccess: Boolean = false,
    val backupError: String? = null,
    val restoreError: String? = null
)


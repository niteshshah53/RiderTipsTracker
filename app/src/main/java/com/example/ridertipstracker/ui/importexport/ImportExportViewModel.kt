package com.example.ridertipstracker.ui.importexport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.example.ridertipstracker.repository.RiderShiftRepository
import com.example.ridertipstracker.utils.CsvExporter
import com.example.ridertipstracker.utils.CsvImporter
import com.example.ridertipstracker.utils.PdfExporter
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import javax.inject.Inject

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val repository: RiderShiftRepository,
    private val csvImporter: CsvImporter,
    private val csvExporter: CsvExporter,
    private val pdfExporter: PdfExporter
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ImportExportUiState())
    val uiState: StateFlow<ImportExportUiState> = _uiState.asStateFlow()
    
    fun importCsv(uri: Uri, defaultPlatform: String = "Uber Eats") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, importError = null)
            try {
                val shifts = csvImporter.importCsv(uri, defaultPlatform)
                if (shifts.isNotEmpty()) {
                    repository.insertShifts(shifts)
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importSuccess = true,
                        importedCount = shifts.size
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importError = "No valid shifts found in CSV file"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importError = e.message ?: "Failed to import CSV"
                )
            }
        }
    }
    
    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
            try {
                repository.getAllShifts().collect { shifts ->
                    val success = csvExporter.exportCsv(shifts, uri)
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportSuccess = success,
                        exportError = if (!success) "Failed to export CSV" else null,
                        exportedCount = if (success) shifts.size else 0
                    )
                    return@collect
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = e.message ?: "Failed to export CSV"
                )
            }
        }
    }
    
    fun clearImportStatus() {
        _uiState.value = _uiState.value.copy(
            importSuccess = false,
            importError = null,
            importedCount = 0
        )
    }
    
    fun exportPdf(uri: Uri, startDate: LocalDate? = null, endDate: LocalDate? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
            try {
                repository.getAllShifts().collect { shifts ->
                    val filteredShifts = if (startDate != null && endDate != null) {
                        shifts.filter { it.date >= startDate && it.date <= endDate }
                    } else {
                        shifts
                    }
                    val success = pdfExporter.exportPdf(filteredShifts, uri, startDate, endDate)
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportSuccess = success,
                        exportError = if (!success) "Failed to export PDF" else null,
                        exportedCount = if (success) filteredShifts.size else 0
                    )
                    return@collect
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = e.message ?: "Failed to export PDF"
                )
            }
        }
    }
    
    fun clearExportStatus() {
        _uiState.value = _uiState.value.copy(
            exportSuccess = false,
            exportError = null,
            exportedCount = 0
        )
    }
}

data class ImportExportUiState(
    val isImporting: Boolean = false,
    val isExporting: Boolean = false,
    val importSuccess: Boolean = false,
    val exportSuccess: Boolean = false,
    val importError: String? = null,
    val exportError: String? = null,
    val importedCount: Int = 0,
    val exportedCount: Int = 0
)


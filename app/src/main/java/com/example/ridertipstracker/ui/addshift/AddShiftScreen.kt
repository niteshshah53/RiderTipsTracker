package com.example.ridertipstracker.ui.addshift

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShiftScreen(
    shiftId: Long? = null,
    onShiftSaved: () -> Unit,
    viewModel: AddShiftViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // Load shift if editing, reset if adding new
    LaunchedEffect(shiftId) {
        if (shiftId != null) {
            viewModel.loadShift(shiftId)
        } else {
            viewModel.resetState()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onShiftSaved()
        }
    }

    val isEditing = shiftId != null

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (isEditing) "Edit Shift" else "Add Shift") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Picker Field
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Date: ${uiState.date?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "Select Date"}")
            }


            // Time Pickers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showStartTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start: ${uiState.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                }
                OutlinedButton(
                    onClick = { showEndTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("End: ${uiState.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                }
            }

            // Summary Info (Auto-calculated)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Hours", style = MaterialTheme.typography.labelMedium)
                        Text("${uiState.totalHours}h", fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Tips", style = MaterialTheme.typography.labelMedium)
                        Text("€${String.format("%.2f", uiState.totalTips)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Tip Inputs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (uiState.onlineTips == 0.0) "" else uiState.onlineTips.toString(),
                    onValueChange = { viewModel.updateOnlineTips(it) },
                    label = { Text("Online Tips (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (uiState.cashTips == 0.0) "" else uiState.cashTips.toString(),
                    onValueChange = { viewModel.updateCashTips(it) },
                    label = { Text("Cash Tips (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            // Orders Input
            OutlinedTextField(
                value = if (uiState.orders == 0) "" else uiState.orders.toString(),
                onValueChange = { viewModel.updateOrders(it) },
                label = { Text("Number of Orders") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Shift Type Selector
            Column {
                Text("Shift Type", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.shiftType == "Full",
                        onClick = { viewModel.updateShiftType("Full") }
                    )
                    Text("Full Shift")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = uiState.shiftType == "Half",
                        onClick = { viewModel.updateShiftType("Half") }
                    )
                    Text("Half Shift")
                }
            }

            // Notes
            OutlinedTextField(
                value = uiState.notes ?: "",
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Error Message
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Save Button
            Button(
                onClick = { viewModel.saveShift() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Shift")
                }
            }
        }
    }

    // Dialogs
    if (showDatePicker) {
        com.example.ridertipstracker.ui.components.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                viewModel.updateDate(date)
            },
            initialDate = uiState.date ?: java.time.LocalDate.now()
        )
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            onTimeSelected = { hour, minute ->
                viewModel.updateStartTime(LocalTime.of(hour, minute))
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            onTimeSelected = { hour, minute ->
                viewModel.updateEndTime(LocalTime.of(hour, minute))
                showEndTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onTimeSelected(state.hour, state.minute) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
        },
        text = {
            TimePicker(state = state)
        }
    )
}

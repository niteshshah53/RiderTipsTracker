package com.example.ridertipstracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate = LocalDate.now()
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant
                            .ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                    onDismissRequest()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = {
            DatePicker(state = datePickerState)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismissRequest: () -> Unit,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    initialStartDate: LocalDate = LocalDate.now().minusDays(30),
    initialEndDate: LocalDate = LocalDate.now()
) {
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    var showStartPicker by remember { mutableStateOf(true) }
    
    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            onDateSelected = { date ->
                startDate = date
                showStartPicker = false
            },
            initialDate = startDate
        )
    } else {
        DatePickerDialog(
            onDismissRequest = {
                showStartPicker = true
            },
            onDateSelected = { date ->
                endDate = date
                if (endDate.isBefore(startDate)) {
                    // Swap if end is before start
                    val temp = startDate
                    startDate = endDate
                    endDate = temp
                }
                onDateRangeSelected(startDate, endDate)
                onDismissRequest()
            },
            initialDate = endDate
        )
    }
}


package com.example.ridertipstracker.ui.reports

import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ridertipstracker.data.local.entity.RiderShift
import com.example.ridertipstracker.ui.components.DateRangePickerDialog
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val shifts by viewModel.filteredShifts.collectAsState()
    val totalTips by viewModel.totalTips.collectAsState()
    val scrollState = rememberScrollState()
    var selectedRange by remember { mutableStateOf("Last 30 Days") }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Performance Reports") },
                actions = {
                    IconButton(onClick = { /* Export functionality */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Export")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Filter Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("7 Days", "30 Days", "Custom").forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { 
                            selectedRange = range
                            if (range == "Custom") {
                                showCustomDatePicker = true
                            } else {
                                val start = when(range) {
                                    "7 Days" -> LocalDate.now().minusDays(7)
                                    "30 Days" -> LocalDate.now().minusDays(30)
                                    else -> LocalDate.now().minusMonths(3)
                                }
                                viewModel.updateDateRange(start, LocalDate.now())
                            }
                        },
                        label = { Text(range) }
                    )
                }
            }

            if (shifts.isEmpty()) {
                EmptyState()
            } else {
                // Numeric Summary Card
                SummaryCard(totalTips, shifts.size)

                // Charts
                ChartSection("Tips Over Time") {
                    TipsLineChart(shifts)
                }

                ChartSection("Weekday Performance") {
                    WeekdayBarChart(shifts)
                }
                
                ChartSection("Cash vs Online Tips") {
                    CashVsOnlineChart(shifts)
                }
            }
        }
    }
    
    // Custom Date Range Picker
    if (showCustomDatePicker) {
        DateRangePickerDialog(
            onDismissRequest = { showCustomDatePicker = false },
            onDateRangeSelected = { start, end ->
                viewModel.updateDateRange(start, end)
                showCustomDatePicker = false
            },
            initialStartDate = LocalDate.now().minusDays(30),
            initialEndDate = LocalDate.now()
        )
    }
}

@Composable
fun SummaryCard(total: Double, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Total Tips", style = MaterialTheme.typography.labelMedium)
                Text("€${String.format("%.2f", total)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Column {
                Text("Total Shifts", style = MaterialTheme.typography.labelMedium)
                Text("$count", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ChartSection(title: String, chart: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Card(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            chart()
        }
    }
}

@Composable
fun TipsLineChart(shifts: List<RiderShift>) {
    val entries = shifts.sortedBy { it.date }.mapIndexed { index, shift ->
        Entry(index.toFloat(), shift.totalTips.toFloat())
    }
    AndroidView(factory = { context ->
        LineChart(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            data = LineData(LineDataSet(entries, "Tips (€)").apply {
                color = Color.Blue.toArgb()
                setDrawCircles(true)
                lineWidth = 2f
                valueTextSize = 10f
            })
            invalidate()
        }
    }, update = { chart ->
        chart.data = LineData(LineDataSet(entries, "Tips (€)"))
        chart.invalidate()
    }, modifier = Modifier.fillMaxSize())
}

@Composable
fun PlatformPieChart(shifts: List<RiderShift>) {
    val platformTotals = shifts.groupBy { it.platform }.mapValues { it.value.sumOf { s -> s.totalTips } }
    val entries = platformTotals.map { PieEntry(it.value.toFloat(), it.key) }
    
    AndroidView(factory = { context ->
        PieChart(context).apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.White.toArgb())
            data = PieData(PieDataSet(entries, "Platforms").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 12f
            })
            invalidate()
        }
    }, modifier = Modifier.fillMaxSize())
}

@Composable
fun WeekdayBarChart(shifts: List<RiderShift>) {
    val weekdayAvg = shifts.groupBy { it.date.dayOfWeek }.mapValues { it.value.map { s -> s.totalTips }.average() }
    val entries = weekdayAvg.entries.mapIndexed { index, entry -> 
        BarEntry(index.toFloat(), entry.value.toFloat()) 
    }
    
    AndroidView(factory = { context ->
        BarChart(context).apply {
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = IndexAxisValueFormatter(weekdayAvg.keys.map { it.name.take(3) })
            axisRight.isEnabled = false
            data = BarData(BarDataSet(entries, "Avg Tips").apply {
                color = Color.Green.toArgb()
            })
            invalidate()
        }
    }, modifier = Modifier.fillMaxSize())
}

@Composable
fun CashVsOnlineChart(shifts: List<RiderShift>) {
    val onlineTotal = shifts.sumOf { it.onlineTips }
    val cashTotal = shifts.sumOf { it.cashTips }
    
    val entries = listOf(
        BarEntry(0f, onlineTotal.toFloat()),
        BarEntry(1f, cashTotal.toFloat())
    )
    
    AndroidView(factory = { context ->
        BarChart(context).apply {
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Online", "Cash"))
            axisRight.isEnabled = false
            data = BarData(BarDataSet(entries, "Tips").apply {
                colors = listOf(Color.Blue.toArgb(), Color.Green.toArgb())
                valueTextSize = 12f
            })
            invalidate()
        }
    }, update = { chart ->
        chart.data = BarData(BarDataSet(entries, "Tips").apply {
            colors = listOf(Color.Blue.toArgb(), Color.Green.toArgb())
        })
        chart.invalidate()
    }, modifier = Modifier.fillMaxSize())
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No data available for this range. Start tracking shifts!", color = Color.Gray)
    }
}

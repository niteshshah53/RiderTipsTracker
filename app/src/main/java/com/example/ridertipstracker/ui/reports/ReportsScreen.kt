package com.example.ridertipstracker.ui.reports

import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
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
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onMenuClick: () -> Unit = {},
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
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
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
                ChartSection("Tips & Orders Over Time") {
                    TipsOrdersGroupedBarChart(shifts)
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
        Card(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            chart()
        }
    }
}

@Composable
fun TipsBarChart(shifts: List<RiderShift>) {
    val sorted = shifts.sortedBy { it.date }
    val entries = sorted.mapIndexed { index, shift ->
        BarEntry(index.toFloat(), shift.totalTips.toFloat())
    }
    val labels = sorted.map { it.date.toString() }

    AndroidView(factory = { context ->
        BarChart(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true
            xAxis.setLabelCount(labels.size, true)
            xAxis.setAvoidFirstLastClipping(true)
            xAxis.labelRotationAngle = -75f
            xAxis.setDrawGridLines(false)
            xAxis.labelRotationAngle = -75f
            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = (labels.size - 0.5f)
            axisRight.isEnabled = false
            setVisibleXRangeMinimum(labels.size.toFloat())
            setVisibleXRangeMaximum(labels.size.toFloat())
            setScaleEnabled(false)
            isDoubleTapToZoomEnabled = false
            setFitBars(true)
            data = BarData(BarDataSet(entries, "Tips (€)").apply {
                color = Color.Blue.toArgb()
                valueTextSize = 10f
                setDrawValues(true)
            })
            (data as BarData).barWidth = 0.9f
            invalidate()
        }
    }, update = { chart ->
        val dataSet = BarDataSet(entries, "Tips (€)").apply {
            color = Color.Blue.toArgb()
            valueTextSize = 10f
            setDrawValues(true)
        }
        chart.data = BarData(dataSet).apply { barWidth = 0.9f }
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.granularity = 1f
        chart.xAxis.isGranularityEnabled = true
        chart.xAxis.setLabelCount(labels.size, true)
        chart.xAxis.setAvoidFirstLastClipping(true)
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.labelRotationAngle = -75f
        chart.xAxis.axisMinimum = -0.5f
        chart.xAxis.axisMaximum = (labels.size - 0.5f)
        chart.setVisibleXRangeMinimum(labels.size.toFloat())
        chart.setVisibleXRangeMaximum(labels.size.toFloat())
        chart.setScaleEnabled(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.invalidate()
    }, modifier = Modifier.fillMaxSize())
}

@Composable
fun TipsOrdersGroupedBarChart(shifts: List<RiderShift>) {
    if (shifts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data for the selected range.")
        }
        return
    }

    val sorted = shifts.sortedBy { it.date }
    val labels = sorted.map { it.date.format(DateTimeFormatter.ofPattern("MM-dd")) }
    val tipMax = sorted.maxOf { it.totalTips }.coerceAtLeast(1.0)
    val orderMax = sorted.maxOf { it.orders }.coerceAtLeast(1)
    val maxCombined = maxOf(tipMax, orderMax.toDouble())
    val ticks = generateTicks(maxCombined)
    val barMaxHeight = 180.dp
    val tipColor = MaterialTheme.colorScheme.primary
    val orderColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Legend
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(color = tipColor, label = "Tips (€)")
            LegendItem(color = orderColor, label = "Orders")
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(barMaxHeight + 120.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Y-axis labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    ticks.forEach { tick ->
                        Text(
                            text = tick,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Bars + grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barMaxHeight + 80.dp)
                ) {
                    Canvas(modifier = Modifier
                        .matchParentSize()
                        .padding(bottom = 48.dp)) {
                        val h = size.height
                        ticks.forEach { tick ->
                            val ratio = tick.toDouble() / maxCombined
                            val y = h - (h * ratio.toFloat())
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.5f
                            )
                        }
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(bottom = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        items(sorted.indices.toList()) { idx ->
                            val shift = sorted[idx]
                            val tipRatio = (shift.totalTips / tipMax).toFloat().coerceIn(0f, 1f)
                            val orderRatio = (shift.orders / orderMax.toFloat()).coerceIn(0f, 1f)
                            val tipHeight = (barMaxHeight * tipRatio).coerceAtLeast(6.dp)
                            val orderHeight = (barMaxHeight * orderRatio).coerceAtLeast(6.dp)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                // Value labels above bars
                                Column(
                                    modifier = Modifier.padding(bottom = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "€${String.format("%.1f", shift.totalTips)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tipColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${shift.orders}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = orderColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.height(barMaxHeight),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    BarBlock(height = tipHeight, color = tipColor)
                                    BarBlock(height = orderHeight, color = orderColor)
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    labels[idx],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarBlock(height: Dp, color: Color) {
    Box(
        modifier = Modifier
            .width(18.dp)
            .height(height)
            .background(color, shape = RoundedCornerShape(4.dp))
    )
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = MaterialTheme.shapes.extraSmall)
        )
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

private fun generateTicks(maxValue: Double): List<String> {
    if (maxValue <= 0.0) return listOf("0")
    val stepRaw = maxValue / 4.0
    val magnitude = 10.0.pow(floor(log10(stepRaw.coerceAtLeast(1e-6))))
    val step = ceil(stepRaw / magnitude) * magnitude
    val ticks = (0..4).map { (it * step).coerceAtMost(maxValue) }
    return ticks.map { if (it >= 10) it.toInt().toString() else String.format("%.1f", it) }
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

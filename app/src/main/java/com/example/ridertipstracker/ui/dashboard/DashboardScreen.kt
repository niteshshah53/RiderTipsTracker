package com.example.ridertipstracker.ui.dashboard

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ridertipstracker.viewmodels.DashboardViewModel
import com.example.ridertipstracker.viewmodels.DailyTips
import com.example.ridertipstracker.viewmodels.RecentShift
import com.example.ridertipstracker.viewmodels.MonthlySummaryData
import com.example.ridertipstracker.viewmodels.WeeklySummaryData
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddShiftClick: () -> Unit,
    onShiftClick: (Long) -> Unit = {},
    onViewAllShiftsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val todayTips by viewModel.todayTips.collectAsState(initial = 0.0)
    val weeklyTips by viewModel.weeklyTips.collectAsState(initial = 0.0)
    val monthlyTips by viewModel.monthlyTips.collectAsState(initial = 0.0)
    val avgTipsPerHour by viewModel.avgTipsPerHour.collectAsState(initial = 0.0)
    val fullDayCount by viewModel.fullDayCount.collectAsState(initial = 0)
    val halfDayCount by viewModel.halfDayCount.collectAsState(initial = 0)
    val totalOrders by viewModel.totalOrders.collectAsState(initial = 0)
    val avgOrdersPerShift by viewModel.avgOrdersPerShift.collectAsState(initial = 0.0)
    val totalShifts by viewModel.totalShifts.collectAsState(initial = 0)
    val averageTipsPerShift by viewModel.averageTipsPerShift.collectAsState(initial = 0.0)
    val weeklySummary by viewModel.weeklySummary.collectAsState(initial = WeeklySummaryData(0.0, 0, 0))
    val monthlySummary by viewModel.monthlySummary.collectAsState(initial = MonthlySummaryData(0.0, 0, 0))
    val upcomingShifts by viewModel.upcomingShifts.collectAsState(initial = emptyList())
    val last3Shifts by viewModel.last3Shifts.collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()

    // Initialize achievements on first load
    LaunchedEffect(Unit) {
        // Achievements are initialized automatically
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TipFlow", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddShiftClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Shift")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("This Month's Earnings", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("€${"%.2f".format(monthlyTips)}", color = MaterialTheme.colorScheme.onPrimary, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // Last 3 Shifts - Always show
            Last3ShiftsCard(last3Shifts, onShiftClick, onViewAllShiftsClick)
            
            // Weekly Summary Card with Trend
            WeeklySummaryCard(weeklySummary)
            
            // Monthly Summary Card with Trend
            MonthlySummaryCard(monthlySummary)

            // Upcoming Shifts (max 3)
            UpcomingShiftsShortCard(upcomingShifts.take(3)) { id -> onShiftClick(id) }
            
            StatsTable(
                avgTipsPerHour = avgTipsPerHour,
                totalShifts = totalShifts,
                fullDayCount = fullDayCount,
                halfDayCount = halfDayCount,
                totalOrders = totalOrders,
                avgOrdersPerShift = avgOrdersPerShift
            )
            
            // Quick Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Avg per Shift",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "€${String.format("%.2f", averageTipsPerShift)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Total Shifts",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$totalShifts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "You're doing great! Every tip counts towards your goal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun StatsTable(
    avgTipsPerHour: Double,
    totalShifts: Int,
    fullDayCount: Int,
    halfDayCount: Int,
    totalOrders: Int,
    avgOrdersPerShift: Double
) {
    val stats = listOf(
        "Avg Tips/h" to "€${String.format("%.2f", avgTipsPerHour)}",
        "Full Days" to "$fullDayCount",
        "Half Days" to "$halfDayCount",
        "Total Shifts" to "$totalShifts",
        "Total Orders" to "$totalOrders",
        "Avg Orders/Shift" to String.format("%.1f", avgOrdersPerShift)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            stats.forEachIndexed { index, stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stat.first,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stat.second,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (index != stats.lastIndex) {
                    Divider()
                }
            }
        }
    }
}

@Composable
fun WeeklySummaryCard(summary: WeeklySummaryData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "This Week",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "€${String.format("%.2f", summary.totalTips)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "${summary.totalShifts} shifts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            if (summary.trend != 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        if (summary.trend > 0) "↑" else "↓",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.trend > 0) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                    Text(
                        "${kotlin.math.abs(summary.trend)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.trend > 0) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlySummaryCard(summary: MonthlySummaryData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "This Month",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    "€${String.format("%.2f", summary.totalTips)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    "${summary.totalShifts} shifts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            if (summary.trend != 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        if (summary.trend > 0) "↑" else "↓",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.trend > 0) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                    Text(
                        "${kotlin.math.abs(summary.trend)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.trend > 0) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingShiftItem(
    shift: com.example.ridertipstracker.data.local.entity.RiderShift,
    onClick: () -> Unit
) {
    val dateFormatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter.ofPattern("MMM dd, yyyy")
    } else {
        TODO("VERSION.SDK_INT < O")
    }
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val isToday = shift.date == LocalDate.now()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        if (isToday) "Today" else shift.date.format(dateFormatter),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "${shift.shiftStartTime.format(timeFormatter)} - ${shift.shiftEndTime.format(timeFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (shift.shiftType.isNotEmpty()) {
                    Text(
                        shift.shiftType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun Last3ShiftsCard(
    shifts: List<RecentShift>,
    onShiftClick: (Long) -> Unit = {},
    onViewAllShiftsClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "Last 3 Shifts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (shifts.isEmpty()) {
                Text(
                    "No recent shifts recorded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    shifts.forEach { shift ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onShiftClick(shift.id) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    shift.dateLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    shift.timeRange,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    shift.shiftType,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "€${"%.2f".format(shift.tips)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    TextButton(
                        onClick = onViewAllShiftsClick,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("View all shifts")
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingShiftsShortCard(
    shifts: List<com.example.ridertipstracker.data.local.entity.RiderShift>,
    onShiftClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Upcoming Shifts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (shifts.isEmpty()) {
                Text(
                    "No upcoming shifts. Add a shift with a future date to see it here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    shifts.forEach { shift ->
                        UpcomingShiftItem(
                            shift = shift,
                            onClick = { onShiftClick(shift.id) }
                        )
                    }
                }
                if (shifts.size == 3) {
                    Text(
                        "Showing next 3 shifts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TipsOverMonthCard(days: List<DailyTips>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Last 30 Days Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (days.isEmpty()) {
                Text(
                    "No data for the last 30 days.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxTips = days.maxOf { it.tips }.coerceAtLeast(1.0)
                val barMaxHeight = 160.dp
                val barColor = MaterialTheme.colorScheme.primary
                val barBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barMaxHeight),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEach { day ->
                        val heightRatio = (day.tips / maxTips).toFloat().coerceIn(0f, 1f)
                        val barHeight = (barMaxHeight * heightRatio).coerceAtLeast(3.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(barHeight)
                                .background(barBg)
                                .padding(horizontal = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(barColor)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val firstDay = days.first().date.dayOfMonth
                    val lastDay = days.last().date.dayOfMonth
                    Text(
                        "$firstDay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$lastDay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DailyTipItem(day: DailyTips, isCompact: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (day.hasData) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (day.hasData) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(if (isCompact) 8.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                day.dayName,
                style = if (isCompact) 
                    MaterialTheme.typography.labelSmall 
                else 
                    MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (day.hasData) 
                    MaterialTheme.colorScheme.onSurface 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                if (day.hasData) "€${String.format("%.2f", day.tips)}" else "-",
                style = if (isCompact) 
                    MaterialTheme.typography.bodySmall 
                else 
                    MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (day.hasData) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

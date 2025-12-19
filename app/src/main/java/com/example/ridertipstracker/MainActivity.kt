package com.example.ridertipstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ridertipstracker.data.local.datastore.PreferencesManager
import com.example.ridertipstracker.ui.addshift.AddShiftScreen
import com.example.ridertipstracker.ui.dashboard.DashboardScreen
import com.example.ridertipstracker.ui.prediction.PredictionScreen
import com.example.ridertipstracker.ui.reports.ReportsScreen
import com.example.ridertipstracker.ui.settings.SettingsScreen
import com.example.ridertipstracker.ui.theme.RiderTipsTrackerTheme
import androidx.compose.runtime.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by preferencesManager.uiTheme.collectAsState(initial = false)
            RiderTipsTrackerTheme(darkTheme = isDarkMode) {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val navItems = listOf(
        BottomNavItem("dashboard", "Dashboard", Icons.Default.Home),
        BottomNavItem("reports", "Reports", Icons.AutoMirrored.Filled.List),
        BottomNavItem("prediction", "Prediction", Icons.Default.Star),
        BottomNavItem("settings", "Settings", Icons.Default.Settings)
    )
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        "Rider Tips Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Divider()
                    Spacer(Modifier.height(8.dp))
                    
                    navItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("dashboard") {
                DashboardScreen(
                    onAddShiftClick = { navController.navigate("add_shift") },
                    onShiftClick = { shiftId ->
                        navController.navigate("edit_shift/$shiftId")
                    },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable("add_shift") {
                AddShiftScreen(
                    onShiftSaved = { navController.popBackStack() }
                )
            }
            composable("edit_shift/{shiftId}") { backStackEntry ->
                val shiftId = backStackEntry.arguments?.getString("shiftId")?.toLongOrNull()
                AddShiftScreen(
                    shiftId = shiftId,
                    onShiftSaved = { navController.popBackStack() }
                )
            }
            composable("reports") {
                ReportsScreen()
            }
            composable("prediction") {
                PredictionScreen()
            }
            composable("settings") {
                SettingsScreen(
                    onImportClick = { navController.navigate("import_export") },
                    onExportClick = { navController.navigate("import_export") }
                )
            }
            composable("import_export") {
                com.example.ridertipstracker.ui.importexport.ImportExportScreen()
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

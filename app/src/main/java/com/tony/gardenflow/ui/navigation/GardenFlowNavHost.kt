package com.tony.gardenflow.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tony.gardenflow.ui.addplant.AddPlantScreen
import com.tony.gardenflow.ui.components.GardenIcon
import com.tony.gardenflow.ui.components.GardenLineIcon
import com.tony.gardenflow.ui.components.rememberGardenMetrics
import com.tony.gardenflow.ui.home.HomeScreen
import com.tony.gardenflow.ui.plants.GrowthRecordScreen
import com.tony.gardenflow.ui.plants.PlantDetailScreen
import com.tony.gardenflow.ui.settings.SettingsScreen
import com.tony.gardenflow.util.GardenText

@Composable
fun GardenFlowNavHost() {
    val nav = rememberNavController()
    val metrics = rememberGardenMetrics()
    val backStackEntry = nav.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == "home" || currentRoute == "settings"
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(color = androidx.compose.ui.graphics.Color.Transparent) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                            .padding(horizontal = 22.dp, vertical = 8.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .widthIn(max = metrics.bottomNavMaxWidth)
                                .align(Alignment.Center)
                                .height(58.dp)
                                .clip(RoundedCornerShape(31.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(7.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BottomTextItem(
                                text = GardenText.s("Garden", "花园"),
                                icon = GardenIcon.CheckGrowth,
                                selected = currentRoute == "home",
                                onClick = {
                                    nav.navigate("home") {
                                        launchSingleTop = true
                                        popUpTo("home") { inclusive = false }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            BottomTextItem(
                                text = GardenText.settings,
                                icon = GardenIcon.Settings,
                                selected = currentRoute == "settings",
                                onClick = {
                                    nav.navigate("settings") {
                                        launchSingleTop = true
                                        popUpTo("home") { inclusive = false }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") {
                HomeScreen(
                    onAdd = { nav.navigate("add") },
                    onPlant = { nav.navigate("plant/$it") }
                )
            }
            composable("add") { AddPlantScreen(onBack = { nav.popBackStack() }) }
            composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
            composable("plant/{plantId}", arguments = listOf(navArgument("plantId") { type = NavType.StringType })) {
                PlantDetailScreen(
                    onBack = { nav.popBackStack() },
                    onGrowthRecord = { nav.navigate("plant/$it/records") }
                )
            }
            composable("plant/{plantId}/records", arguments = listOf(navArgument("plantId") { type = NavType.StringType })) {
                GrowthRecordScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun BottomTextItem(text: String, icon: GardenIcon, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(50.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GardenLineIcon(icon, modifier = Modifier.size(22.dp), tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
            Text(
                text,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
            )
        }
    }
}

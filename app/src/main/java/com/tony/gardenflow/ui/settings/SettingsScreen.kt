package com.tony.gardenflow.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.tony.gardenflow.BuildConfig
import com.tony.gardenflow.ui.components.GardenCard
import com.tony.gardenflow.ui.components.GardenIcon
import com.tony.gardenflow.ui.components.GardenIconBadge
import com.tony.gardenflow.ui.components.GardenScreen
import com.tony.gardenflow.ui.components.rememberGardenMetrics
import com.tony.gardenflow.util.GardenText
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val metrics = rememberGardenMetrics()
    val settings by vm.settings.collectAsState()
    val candidates by vm.locationCandidates.collectAsState()
    val isSearchingLocation by vm.isSearchingLocation.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var name by remember(settings.locationName) { mutableStateOf(settings.locationName.orEmpty()) }
    var lat by remember(settings.latitude) { mutableStateOf(settings.latitude?.toString().orEmpty()) }
    var lon by remember(settings.longitude) { mutableStateOf(settings.longitude?.toString().orEmpty()) }
    var reminder by remember(settings.dailyReminderHour, settings.dailyReminderMinute) { mutableStateOf("%02d:%02d".format(settings.dailyReminderHour, settings.dailyReminderMinute)) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true || grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            vm.usePhoneLocation()
        }
    }
    LaunchedEffect(Unit) {
        vm.events.collect { snackbarHostState.showSnackbar(it) }
    }

    GardenScreen {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Row(
                    Modifier.fillMaxWidth().padding(start = 8.dp, top = 12.dp, end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = GardenText.back) }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .fillMaxSize()
                    .widthIn(max = metrics.maxContentWidth)
                    .padding(horizontal = metrics.screenPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(metrics.listGap)
            ) {
                Text(GardenText.settings, style = MaterialTheme.typography.displayLarge)
                SectionTitle(GardenText.s("Garden", "花园"))
                GardenCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(metrics.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingRow(GardenIcon.Location, GardenText.s("Garden location", "花园位置"), name.ifBlank { GardenText.s("No location set", "未设置位置") }, trailing = "")
                        OutlinedTextField(
                            name,
                            {
                                name = it
                                lat = ""
                                lon = ""
                                vm.clearLocationCandidates()
                            },
                            label = { Text(GardenText.s("City or suburb", "城市或区域")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(lat, { lat = it }, label = { Text(GardenText.s("Latitude", "纬度")) }, modifier = Modifier.weight(1f))
                            OutlinedTextField(lon, { lon = it }, label = { Text(GardenText.s("Longitude", "经度")) }, modifier = Modifier.weight(1f))
                        }
                        Button(
                            onClick = { vm.searchLocationCandidates(name) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = !isSearchingLocation
                        ) {
                            if (isSearchingLocation) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text(GardenText.s("Search location", "搜索位置"))
                            }
                        }
                        if (candidates.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(GardenText.s("Choose the correct place", "选择正确的位置"), style = MaterialTheme.typography.titleMedium)
                                candidates.forEach { candidate ->
                                    LocationCandidateRow(
                                        name = candidate.name,
                                        coordinates = "%.4f, %.4f".format(candidate.latitude, candidate.longitude),
                                        onClick = {
                                            name = candidate.name
                                            lat = candidate.latitude.toString()
                                            lon = candidate.longitude.toString()
                                            vm.selectLocation(candidate)
                                        }
                                    )
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasFine || hasCoarse) {
                                    vm.usePhoneLocation()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text(GardenText.s("Use phone location", "使用手机定位"))
                        }
                        OutlinedButton(onClick = { vm.updateLocation(name, lat.toDoubleOrNull(), lon.toDoubleOrNull()) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Text(GardenText.s("Save typed coordinates", "保存手动经纬度"))
                        }
                    }
                }
                SectionTitle(GardenText.s("Reminders", "提醒"))
                GardenCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(metrics.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            SettingRow(GardenIcon.Notifications, GardenText.taskReminder, if (settings.notificationsEnabled) GardenText.taskReminderHint else GardenText.s("Reminder checks are off.", "提醒检查已关闭。"), trailing = "", modifier = Modifier.weight(1f))
                            Switch(checked = settings.notificationsEnabled, onCheckedChange = vm::updateNotifications)
                        }
                        if (settings.notificationsEnabled) {
                            SettingRow(GardenIcon.DailyReminder, GardenText.taskReminderTime, displayTime(reminder), trailing = "")
                            OutlinedTextField(reminder, { reminder = it }, label = { Text("HH:mm") }, modifier = Modifier.fillMaxWidth())
                            Button(onClick = { parseTime(reminder)?.let { vm.updateReminder(it.first, it.second) } }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                                Text(GardenText.s("Save reminder", "保存提醒"))
                            }
                        }
                    }
                }
                SectionTitle(GardenText.s("Weather", "天气"))
                GardenCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(metrics.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingRow(GardenIcon.HighTemperature, GardenText.s("High temperature rule", "高温规则"), GardenText.s("30°C shortens watering interval", "30°C 会缩短浇水间隔"), trailing = "")
                    }
                }
                SectionTitle("AI")
                GardenCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(metrics.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val aiWorking = vm.aiStatus == "AI configured"
                        SettingRow(
                            GardenIcon.DeepSeek,
                            GardenText.s("AI detector", "AI 检测仪"),
                            if (aiWorking) GardenText.s("AI configured", "AI 已接入") else GardenText.s("Not working", "未正常接入"),
                            trailing = if (aiWorking) "✓" else "!"
                        )
                    }
                }
                SectionTitle(GardenText.s("App", "应用"))
                GardenCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(metrics.cardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingRow(GardenIcon.About, GardenText.s("Language", "语言"), languageLabel(settings.languageCode), trailing = "")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = settings.languageCode == null,
                                onClick = { vm.updateLanguage(null) },
                                label = { Text(GardenText.s("System", "跟随系统")) }
                            )
                            FilterChip(
                                selected = settings.languageCode == "zh",
                                onClick = { vm.updateLanguage("zh") },
                                label = { Text("中文") }
                            )
                            FilterChip(
                                selected = settings.languageCode == "en",
                                onClick = { vm.updateLanguage("en") },
                                label = { Text("English") }
                            )
                        }
                        SettingRow(GardenIcon.ExportData, GardenText.s("Export data", "导出数据"), GardenText.s("Coming soon", "即将推出"), trailing = "")
                        SettingRow(
                            GardenIcon.About,
                            GardenText.s("Privacy Policy", "隐私政策"),
                            if (BuildConfig.PRIVACY_POLICY_URL.isBlank()) {
                                GardenText.s("Not configured yet", "尚未配置")
                            } else {
                                GardenText.s("Open in browser", "在浏览器中打开")
                            },
                            trailing = "›",
                            modifier = Modifier.clickable {
                                val url = BuildConfig.PRIVACY_POLICY_URL.trim()
                                if (url.isBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            GardenText.s(
                                                "Privacy Policy URL is not configured in this build.",
                                                "这个版本尚未配置隐私政策链接。"
                                            )
                                        )
                                    }
                                } else {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            }
                        )
                        SettingRow(GardenIcon.About, GardenText.s("About GardenFlow", "关于 GardenFlow"), GardenText.s("Version 1.0.0", "版本 1.0.0"), trailing = "")
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun SettingRow(icon: GardenIcon, title: String, value: String, trailing: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        GardenIconBadge(icon, modifier = Modifier.size(42.dp))
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
        }
        if (trailing.isNotBlank()) Text(trailing, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun LocationCandidateRow(name: String, coordinates: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GardenIconBadge(GardenIcon.Location, modifier = Modifier.size(38.dp))
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(coordinates, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f))
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
}

private fun parseTime(raw: String): Pair<Int, Int>? {
    val parts = raw.split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    return if (h in 0..23 && m in 0..59) h to m else null
}

private fun displayTime(raw: String): String = parseTime(raw)?.let { (h, m) ->
    val hour = if (h % 12 == 0) 12 else h % 12
    "%d:%02d %s".format(hour, m, if (h < 12) "AM" else "PM")
} ?: raw

private fun languageLabel(code: String?): String = when (code) {
    "zh" -> "中文"
    "en" -> "English"
    else -> GardenText.s("System default", "跟随系统")
}

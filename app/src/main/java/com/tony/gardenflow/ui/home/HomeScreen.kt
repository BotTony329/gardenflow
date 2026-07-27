package com.tony.gardenflow.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tony.gardenflow.domain.model.CareHistory
import com.tony.gardenflow.domain.model.GardenTask
import com.tony.gardenflow.domain.model.Plant
import com.tony.gardenflow.domain.model.TaskType
import com.tony.gardenflow.ui.components.GardenIcon
import com.tony.gardenflow.ui.components.GardenLineIcon
import com.tony.gardenflow.ui.components.GardenCard
import com.tony.gardenflow.ui.components.GardenScreen
import com.tony.gardenflow.ui.components.PlantPhotoOrIcon
import com.tony.gardenflow.ui.components.gardenIcon
import com.tony.gardenflow.ui.components.label
import com.tony.gardenflow.ui.components.rememberGardenMetrics
import com.tony.gardenflow.ui.components.weatherGardenIcon
import com.tony.gardenflow.util.GardenPlantText
import com.tony.gardenflow.util.GardenText
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(onAdd: () -> Unit, onPlant: (String) -> Unit, vm: HomeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val metrics = rememberGardenMetrics()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(vm) {
        vm.events.collect { snackbarHostState.showSnackbar(it) }
    }
    GardenScreen {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = { FloatingActionButton(onClick = onAdd) { Text("+") } }
        ) { padding ->
            if (metrics.isTablet) {
                TabletHomeContent(
                    state = state,
                    metrics = metrics,
                    padding = padding,
                    onPlant = onPlant,
                    onWater = vm::recordWatering,
                    onFertilise = vm::recordFertilising
                )
                return@Scaffold
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = metrics.screenPadding,
                    top = padding.calculateTopPadding() + 32.dp,
                    end = metrics.screenPadding,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(metrics.listGap)
            ) {
                item {
                    Text(
                        state.heroTitle,
                        style = MaterialTheme.typography.displaySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                item {
                    WeatherSummaryCard(state)
                }
                item { Text(GardenText.yourPlants, style = MaterialTheme.typography.titleLarge) }
                items(state.plants, key = { it.id }) { plant ->
                    PlantHomeCard(
                        plant = plant,
                        history = state.history,
                        tasks = state.tasks.filter { it.plantId == plant.id },
                        onClick = { onPlant(plant.id) },
                        onWater = { vm.recordWatering(plant.id) },
                        onFertilise = { vm.recordFertilising(plant.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabletHomeContent(
    state: HomeUiState,
    metrics: com.tony.gardenflow.ui.components.GardenMetrics,
    padding: PaddingValues,
    onPlant: (String) -> Unit,
    onWater: (String) -> Unit,
    onFertilise: (String) -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().widthIn(max = metrics.maxContentWidth),
            contentPadding = PaddingValues(
                start = metrics.screenPadding,
                top = padding.calculateTopPadding() + 36.dp,
                end = metrics.screenPadding,
                bottom = 112.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(metrics.listGap),
            verticalArrangement = Arrangement.spacedBy(metrics.listGap)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    state.heroTitle,
                    style = MaterialTheme.typography.displayLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                WeatherSummaryCard(state)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(GardenText.yourPlants, style = MaterialTheme.typography.headlineSmall)
            }
            items(state.plants, key = { it.id }) { plant ->
                PlantHomeCard(
                    plant = plant,
                    history = state.history,
                    tasks = state.tasks.filter { it.plantId == plant.id },
                    onClick = { onPlant(plant.id) },
                    onWater = { onWater(plant.id) },
                    onFertilise = { onFertilise(plant.id) }
                )
            }
        }
    }
}

@Composable
private fun WeatherSummaryCard(state: HomeUiState) {
    val compact = LocalConfiguration.current.screenWidthDp < 390
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GardenLineIcon(
                    weatherGardenIcon(state.weatherIcon),
                    modifier = Modifier.size(if (compact) 44.dp else 54.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        state.weatherLocation,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = if (compact) 1 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!compact) {
                        Text(
                            state.weatherSubtitle,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    state.temperatureText,
                    modifier = Modifier.widthIn(min = 64.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    softWrap = false
                )
            }
            if (compact) {
                Text(
                    state.weatherSubtitle,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                state.weatherAdvice,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlantHomeCard(
    plant: Plant,
    history: List<CareHistory>,
    tasks: List<GardenTask>,
    onClick: () -> Unit,
    onWater: () -> Unit,
    onFertilise: () -> Unit
) {
    val metrics = rememberGardenMetrics()
    GardenCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(metrics.cardPadding), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            androidx.compose.material3.Surface(onClick = onClick, color = androidx.compose.ui.graphics.Color.Transparent) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    PlantPhotoOrIcon(plant = plant, modifier = Modifier.size(72.dp))
                    Column(Modifier.padding(start = 14.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(plant.name, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        plant.variety?.takeIf { it.isNotBlank() }?.let {
                            Text(it, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(plant.stagePillText(), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("• ${plant.nextWaterHint(history)}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            PlantTaskSummary(tasks)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onWater, modifier = Modifier.weight(1f).height(48.dp)) {
                    GardenLineIcon(GardenIcon.Water, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    Text("  ${GardenText.water}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = onFertilise,
                    modifier = Modifier.weight(1f).height(48.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    GardenLineIcon(GardenIcon.Fertilise, modifier = Modifier.size(20.dp))
                    Text("  ${GardenText.fertilise}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun PlantTaskSummary(tasks: List<GardenTask>) {
    if (tasks.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        tasks.forEach { task ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GardenLineIcon(task.type.gardenIcon(), modifier = Modifier.size(20.dp))
                Text(
                    GardenText.dueToday(task.type.label()),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            task.reason?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f))
            }
        }
    }
}

private fun Plant.stageSummary(): String {
    val day = sowingDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).coerceAtLeast(0) }
    val stage = displayStage(day)
    return if (day == null) GardenText.notPlantedYet else GardenText.stageSummary(stage?.let(GardenPlantText::stageLabel) ?: GardenText.estimatedGrowth(), day)
}

private fun Plant.stagePillText(): String {
    val day = sowingDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).coerceAtLeast(0) }
    val stage = displayStage(day)
    val label = stage?.let(GardenPlantText::stageLabel) ?: GardenText.estimatedGrowth()
    return when {
        day != null -> GardenText.s("$label · $day d", "$label · $day 天")
        confirmedStageDate != null -> GardenText.s("$label · calibrated", "$label · 已校准")
        else -> GardenText.notPlantedYet
    }
}

private fun Plant.displayStage(day: Long?) =
    confirmedStageKey
        ?.let { key -> growthStages.firstOrNull { it.key == key } }
        ?: day?.let { d -> growthStages.firstOrNull { d.toInt() in it.startDay..it.endDay } }

private fun Plant.nextWaterHint(history: List<CareHistory>): String {
    val today = LocalDate.now()
    val latestWaterDate = history
        .filter { it.plantId == id && it.actionType == TaskType.WATER }
        .maxByOrNull { it.performedAt }
        ?.performedAt
        ?.atZone(ZoneId.systemDefault())
        ?.toLocalDate()
    val anchorDate = latestWaterDate
        ?: sowingDate
        ?: createdAt.atZone(ZoneId.systemDefault()).toLocalDate()
    val daysSince = ChronoUnit.DAYS.between(anchorDate, today).coerceAtLeast(0)
    val remaining = wateringIntervalDays.coerceAtLeast(1).toLong() - daysSince
    return GardenText.waterIn(remaining)
}

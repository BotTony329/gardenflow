package com.tony.gardenflow.ui.plants

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tony.gardenflow.domain.model.CareHistory
import com.tony.gardenflow.domain.model.GrowthStage
import com.tony.gardenflow.domain.model.Plant
import com.tony.gardenflow.domain.model.TaskType
import com.tony.gardenflow.ui.components.ConfirmDeleteDialog
import com.tony.gardenflow.ui.components.GardenCard
import com.tony.gardenflow.ui.components.GardenIcon
import com.tony.gardenflow.ui.components.GardenIconBadge
import com.tony.gardenflow.ui.components.GardenLineIcon
import com.tony.gardenflow.ui.components.GardenScreen
import com.tony.gardenflow.ui.components.PlantPhotoOrIcon
import com.tony.gardenflow.ui.components.rememberGardenMetrics
import com.tony.gardenflow.util.GardenPlantText
import com.tony.gardenflow.util.GardenText
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun PlantDetailScreen(
    onBack: () -> Unit,
    onGrowthRecord: (String) -> Unit,
    vm: PlantDetailViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val metrics = rememberGardenMetrics()
    var showDelete by remember { mutableStateOf(false) }
    val plant = state.plant
    if (plant == null) {
        Text(GardenText.s("Plant not found", "找不到植物"), modifier = Modifier.padding(24.dp))
        return
    }
    if (showDelete) ConfirmDeleteDialog(onDismiss = { showDelete = false }, onConfirm = { vm.delete(); onBack() })

    GardenScreen {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Row(
                    Modifier.fillMaxWidth().padding(start = 8.dp, top = 12.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = GardenText.back) }
                    Text("GardenFlow", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showDelete = true }) { Icon(Icons.Filled.MoreVert, contentDescription = GardenText.s("More", "更多")) }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                Modifier.fillMaxSize().widthIn(max = metrics.maxContentWidth),
                contentPadding = PaddingValues(horizontal = metrics.screenPadding, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item { DetailPhotoBlock(plant = plant, onClick = { onGrowthRecord(plant.id) }) }
                item { PlantTitleBlock(plant, state.currentStage) }
                item { Text(GardenText.s("Growth timeline", "生长时间线"), style = MaterialTheme.typography.headlineSmall) }
                item { GrowthTimeline(plant, state.currentStage, onStageSelected = vm::confirmStage) }
                item { Text(GardenText.s("Care plan", "护理计划"), style = MaterialTheme.typography.headlineSmall) }
                item { CarePlanCard(plant) }
                item {
                    GardenCard(Modifier.fillMaxWidth().clickable { onGrowthRecord(plant.id) }) {
                        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            GardenIconBadge(GardenIcon.Camera, modifier = Modifier.size(54.dp))
                            Column(Modifier.padding(start = 14.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(GardenText.s("Growth album", "成长相册"), style = MaterialTheme.typography.titleLarge)
                                Text(
                                    GardenText.s("Record or replace this plant's photo.", "记录或更换这株植物的照片。"),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                                )
                            }
                        }
                    }
                }
                item { Text(GardenText.careHistory, style = MaterialTheme.typography.headlineSmall) }
                if (state.history.isEmpty()) {
                    item { Text(GardenText.s("No care actions yet.", "还没有护理记录。"), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)) }
                } else {
                    items(state.history) { history ->
                        GardenCard(Modifier.fillMaxWidth()) {
                            Text(history.displayText(), Modifier.padding(16.dp))
                        }
                    }
                }
                item {
                    OutlinedButton(onClick = { showDelete = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(GardenText.s("Delete plant", "删除植物"), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun DetailPhotoBlock(plant: Plant, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.extraLarge
    val photoUri = plant.photoUri?.takeIf { it.isNotBlank() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.55f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = plant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PlantPhotoOrIcon(plant = plant, modifier = Modifier.size(76.dp))
                Text(GardenText.s("Add plant photo", "上传植物照片"), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun PlantTitleBlock(plant: Plant, currentStage: GrowthStage?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            plant.name,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            plant.variety?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f))
            }
            Box(
                Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    "${currentStage?.let(GardenPlantText::stageLabel) ?: GardenText.estimatedGrowth()} · ${plant.dayLabel()}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun GrowthTimeline(
    plant: Plant,
    currentStage: GrowthStage?,
    onStageSelected: (String) -> Unit
) {
    GardenCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                GardenText.s("Tap a stage to correct the prediction.", "点击阶段即可校准当前预测。"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
            )
            plant.growthStages.forEach { stage ->
                val current = stage.key == currentStage?.key
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .clickable { onStageSelected(stage.key) }
                        .background(if (current) MaterialTheme.colorScheme.primary.copy(alpha = 0.11f) else androidx.compose.ui.graphics.Color.Transparent)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(if (current) 22.dp else 12.dp)
                            .clip(CircleShape)
                            .background(if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f))
                    )
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(
                            GardenPlantText.stageLabel(stage),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            if (current) plant.dayLabel() else GardenText.s("Day ${stage.startDay}-${stage.endDay}", "第 ${stage.startDay}-${stage.endDay} 天"),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarePlanCard(plant: Plant) {
    GardenCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CarePlanRow(
                icon = GardenIcon.Water,
                title = GardenText.s("Water every ${plant.wateringIntervalDays} days", "每 ${plant.wateringIntervalDays} 天浇水"),
                body = buildString {
                    plant.wateringAmountMm?.takeIf { it > 0.0 }?.let {
                        append(GardenText.s("About ${it.formatNumber()} mm each time", "每次约 ${it.formatNumber()} mm"))
                        append(" · ")
                    }
                    append(GardenText.s("Skip after ${plant.rainSkipThresholdMm.formatNumber()} mm rain", "降雨 ${plant.rainSkipThresholdMm.formatNumber()} mm 后跳过"))
                }
            )
            CarePlanRow(
                icon = GardenIcon.Fertilise,
                title = GardenText.s("Fertilise every ${plant.fertilisingIntervalDays} days", "每 ${plant.fertilisingIntervalDays} 天施肥"),
                body = plant.fertilisingAdvice?.takeIf { it.isNotBlank() }?.let(GardenPlantText::careAdvice)
                    ?: GardenText.s("Use gentle fertiliser during active growth.", "生长期使用温和肥料。")
            )
            CarePlanRow(
                icon = GardenIcon.HighTemperature,
                title = GardenText.s("Preferred temperature", "适宜温度"),
                body = plant.temperatureText()
            )
            CarePlanRow(
                icon = GardenIcon.Harvest,
                title = GardenText.s("Estimated harvest", "预计收获"),
                body = plant.harvestText()
            )
        }
    }
}

@Composable
private fun CarePlanRow(icon: GardenIcon, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        GardenIconBadge(icon, modifier = Modifier.size(42.dp))
        Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
        }
    }
}

private fun Plant.dayLabel(): String {
    val date = sowingDate ?: return if (confirmedStageDate != null) {
        GardenText.s("Calibrated", "已校准")
    } else {
        GardenText.notPlantedYet
    }
    val day = ChronoUnit.DAYS.between(date, LocalDate.now()).coerceAtLeast(0)
    return GardenText.day(day)
}

private fun Plant.temperatureText(): String {
    val min = preferredTempMinC
    val max = preferredTempMaxC
    return if (min != null && max != null && max >= min) {
        "${min.formatNumber()}-${max.formatNumber()}°C"
    } else {
        GardenText.notEnoughData
    }
}

private fun Plant.harvestText(): String {
    val min = harvestMinDays
    val max = harvestMaxDays
    return if (min != null && max != null && min > 0 && max >= min) {
        GardenText.s("$min-$max days", "$min-$max 天")
    } else {
        GardenText.notEnoughData
    }
}

private fun Double.formatNumber(): String =
    if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)

private fun CareHistory.displayText(): String {
    val local = performedAt.atZone(ZoneId.systemDefault())
    val action = when (actionType) {
        TaskType.WATER -> GardenText.water
        TaskType.FERTILISE -> GardenText.fertilise
        TaskType.CHECK_GROWTH -> GardenText.s("Growth check", "生长检查")
        TaskType.HARVEST -> GardenText.s("Harvest", "收获")
        TaskType.CUSTOM -> GardenText.s("Custom", "自定义")
    }
    return "%s · %s %02d:%02d".format(action, local.toLocalDate(), local.hour, local.minute)
}

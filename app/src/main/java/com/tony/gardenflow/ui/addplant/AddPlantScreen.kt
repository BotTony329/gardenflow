package com.tony.gardenflow.ui.addplant

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.tony.gardenflow.domain.model.PlantProfile
import com.tony.gardenflow.ui.components.ErrorText
import com.tony.gardenflow.ui.components.GardenCard
import com.tony.gardenflow.ui.components.GardenIcon
import com.tony.gardenflow.ui.components.GardenIconBadge
import com.tony.gardenflow.ui.components.GardenLineIcon
import com.tony.gardenflow.ui.components.GardenScreen
import com.tony.gardenflow.ui.components.rememberGardenMetrics
import com.tony.gardenflow.util.GardenPlantText
import com.tony.gardenflow.util.GardenText
import com.tony.gardenflow.util.createCameraImageUri
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeParseException

@Composable
fun AddPlantScreen(onBack: () -> Unit, vm: AddPlantViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val metrics = rememberGardenMetrics()
    val context = LocalContext.current
    var manualMode by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(AddPlantStep.Identity) }
    var pendingCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) vm.recognize(uri)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = pendingCameraUri
        if (ok && uri != null) vm.recognize(uri)
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createCameraImageUri(context, "packet")
            pendingCameraUri = uri
            camera.launch(uri)
        } else {
            vm.setError(GardenText.s("Camera permission is needed to take a photo.", "拍照需要相机权限。"))
        }
    }
    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val uri = createCameraImageUri(context, "packet")
            pendingCameraUri = uri
            camera.launch(uri)
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }
    LaunchedEffect(state.saved) { if (state.saved) onBack() }
    LaunchedEffect(state.packageText) {
        if (state.packageText.isNotBlank() && step == AddPlantStep.Identity) step = AddPlantStep.PacketText
    }

    GardenScreen {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Row(
                    Modifier.fillMaxWidth().padding(start = 8.dp, top = 12.dp, end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = GardenText.back) }
                    Text("GardenFlow", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .fillMaxSize()
                    .widthIn(max = metrics.maxContentWidth)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(Modifier.padding(horizontal = metrics.screenPadding), verticalArrangement = Arrangement.spacedBy(metrics.listGap)) {
                    if (!state.isLoading && state.profile == null) {
                        StepHeader(step)
                    }
                    when {
                        state.isLoading -> AiLoadingCard()
                        state.profile != null -> CarePlanReview(profile = state.profile!!, state = state, onSave = vm::save, onRegenerate = vm::generate)
                        else -> when (step) {
                            AddPlantStep.Identity -> {
                                AddPlantStart(
                                    manualMode = manualMode,
                                    plantName = state.plantName,
                                    onPlantName = vm::setPlantName,
                                    onManualMode = { manualMode = true },
                                    onAlbum = { picker.launch("image/*") },
                                    onCamera = { launchCamera() }
                                )
                                if (state.plantName.isNotBlank() || state.packageText.isNotBlank()) {
                                    Button(onClick = { step = if (state.packageText.isNotBlank()) AddPlantStep.PacketText else AddPlantStep.PlantState }, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                                        Text(GardenText.s("Continue", "继续"))
                                    }
                                }
                            }
                            AddPlantStep.PacketText -> {
                                OcrReview(state = state, onText = vm::setPackageText, onRetake = { picker.launch("image/*") })
                                StepButtons(
                                    canGoBack = true,
                                    primaryText = GardenText.s("Continue", "继续"),
                                    onBack = { step = AddPlantStep.Identity },
                                    onPrimary = { step = AddPlantStep.PlantState }
                                )
                            }
                            AddPlantStep.PlantState -> {
                                PlantStateQuestion(
                                    selected = state.plantStartStatus,
                                    onSelected = vm::setPlantStartStatus
                                )
                                StepButtons(
                                    canGoBack = true,
                                    primaryText = GardenText.s("Continue", "继续"),
                                    onBack = { step = if (state.packageText.isNotBlank()) AddPlantStep.PacketText else AddPlantStep.Identity },
                                    onPrimary = { step = AddPlantStep.Dates }
                                )
                            }
                            AddPlantStep.Dates -> {
                                PlantDatesEditor(
                                    sowingDate = state.sowingDate,
                                    lastWateredDate = state.lastWateredDate,
                                    currentStatus = state.plantStartStatus,
                                    onSowingDate = vm::setSowingDate,
                                    onLastWateredDate = vm::setLastWateredDate,
                                    onError = vm::setError
                                )
                                StepButtons(
                                    canGoBack = true,
                                    primaryText = GardenText.createCarePlan,
                                    onBack = { step = AddPlantStep.PlantState },
                                    onPrimary = vm::generate
                                )
                            }
                        }
                    }
                    ErrorText(state.error)
                    Spacer(Modifier.height(32.dp))
                }
            }
            }
        }
    }
}

private enum class AddPlantStep { Identity, PacketText, PlantState, Dates }

@Composable
private fun StepHeader(step: AddPlantStep) {
    val index = when (step) {
        AddPlantStep.Identity -> 1
        AddPlantStep.PacketText -> 2
        AddPlantStep.PlantState -> 3
        AddPlantStep.Dates -> 4
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            GardenText.s("Step $index / 4", "步骤 $index / 4"),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )
        LinearProgressIndicator(
            progress = { index / 4f },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(MaterialTheme.shapes.extraLarge),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun AddPlantStart(
    manualMode: Boolean,
    plantName: String,
    onPlantName: (String) -> Unit,
    onManualMode: () -> Unit,
    onAlbum: () -> Unit,
    onCamera: () -> Unit
) {
    Text(GardenText.s("Add a new plant", "添加一株\n新植物"), style = MaterialTheme.typography.displaySmall)
    Text(
        GardenText.s("Choose how to add it. AI will finish the rest.", "选择添加方式，AI 会帮你完成剩下的事。"),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
    )
    ActionCard(
        icon = GardenIcon.Camera,
        title = GardenText.s("Take packet photo", "拍照扫描包装"),
        body = GardenText.s("Open camera and run OCR", "打开相机并识别文字"),
        onClick = onCamera,
        highlighted = false
    )
    ActionCard(
        icon = GardenIcon.Camera,
        title = GardenText.s("Choose packet image", "从相册选择包装"),
        body = GardenText.s("Pick an existing photo and run OCR", "选择已有照片并识别文字"),
        onClick = onAlbum,
        highlighted = false
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        border = if (manualMode) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        shadowElevation = 1.dp
    ) {
        Surface(onClick = onManualMode, color = androidx.compose.ui.graphics.Color.Transparent) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    GardenIconBadge(GardenIcon.Keyboard, modifier = Modifier.size(62.dp))
                    Column(Modifier.padding(start = 18.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(GardenText.s("Enter plant name", "手动输入名称"), style = MaterialTheme.typography.titleLarge)
                        Text(
                            GardenText.s("For example: tomato, lemon, mint", "例如：番茄、柠檬、薄荷"),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!manualMode) {
                        Text("›", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
                    }
                }
                if (manualMode) {
                    OutlinedTextField(
                        value = plantName,
                        onValueChange = onPlantName,
                        label = { Text(GardenText.plantName) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCard(icon: GardenIcon, title: String, body: String, onClick: () -> Unit, highlighted: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        border = if (highlighted) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        shadowElevation = 1.dp
    ) {
        Surface(onClick = onClick, color = androidx.compose.ui.graphics.Color.Transparent) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                GardenIconBadge(icon, modifier = Modifier.size(62.dp))
                Column(Modifier.padding(start = 18.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(body, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Text("›", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f))
            }
        }
    }
}

@Composable
private fun PlantStateQuestion(
    selected: PlantStartStatus,
    onSelected: (PlantStartStatus) -> Unit
) {
    Text(GardenText.s("What state is this plant in?", "这株植物目前\n是什么状态？"), style = MaterialTheme.typography.displaySmall)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PlantStartStatus.entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { status ->
                    StatusCard(
                        status = status,
                        selected = selected == status,
                        onClick = { onSelected(status) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatusCard(status: PlantStartStatus, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(118.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        shadowElevation = 1.dp
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GardenLineIcon(status.icon(), modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                status.label(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StepButtons(
    canGoBack: Boolean,
    primaryText: String,
    onBack: () -> Unit,
    onPrimary: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        if (canGoBack) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(54.dp)) {
                Text(GardenText.back)
            }
        }
        Button(onClick = onPrimary, modifier = Modifier.weight(1f).height(54.dp)) {
            Text(primaryText)
        }
    }
}

@Composable
private fun OcrReview(state: AddPlantUiState, onText: (String) -> Unit, onRetake: () -> Unit) {
    Text(GardenText.s("Review packet text", "检查包装文字"), style = MaterialTheme.typography.displaySmall)
    Text(GardenText.s("Check the text before AI creates the plan.", "AI 生成计划前，请先检查识别文字。"), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f))
    OutlinedTextField(
        value = state.packageText,
        onValueChange = onText,
        minLines = 8,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(GardenText.s("Packet text", "包装文字")) }
    )
    OutlinedButton(onClick = onRetake, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text(GardenText.s("Choose another image", "选择另一张图片")) }
}

@Composable
private fun AiLoadingCard() {
    Column(
        Modifier.fillMaxWidth().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        GardenIconBadge(GardenIcon.Seedling, modifier = Modifier.size(88.dp))
        Text("GardenFlow", style = MaterialTheme.typography.displaySmall)
        Text(GardenText.s("AI is building your care plan", "AI 正在生成护理计划"), color = MaterialTheme.colorScheme.primary)
        LoadingStep(true, GardenText.s("Finding care fields", "整理护理字段"))
        LoadingStep(true, GardenText.s("Reading packet information", "读取包装信息"))
        LoadingStep(true, GardenText.s("Understanding the plant", "判断植物类型"))
        LoadingStep(false, GardenText.s("Creating watering schedule", "生成浇水计划"))
        LoadingStep(false, GardenText.s("Building growth timeline", "生成生长时间线"))
        Text(GardenText.s("This may take a few seconds.", "这可能需要几秒钟。"), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f))
    }
}

@Composable
private fun LoadingStep(done: Boolean, label: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            if (done) GardenIconBadge(GardenIcon.Check, modifier = Modifier.size(40.dp)) else CircularProgressIndicator(Modifier.size(30.dp), strokeWidth = 3.dp)
        }
        Text(label, Modifier.padding(start = 16.dp), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun CarePlanReview(profile: PlantProfile, state: AddPlantUiState, onSave: () -> Unit, onRegenerate: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(GardenText.s("Personal care plan", "专属护理计划"), style = MaterialTheme.typography.displaySmall)
        Text(
            GardenText.s("Based on your input, AI generated this plan. You can regenerate before saving.", "基于你的输入，AI 已生成以下方案；保存前可以重新生成。"),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
        )
        GardenCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                GardenIconBadge(GardenIcon.Seedling, modifier = Modifier.size(58.dp))
                Column(Modifier.padding(start = 14.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(profile.plantName, style = MaterialTheme.typography.headlineSmall, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    profile.variety?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                    Text(state.plantStartStatus.label(), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        GardenCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(GardenText.s("AI planting guide", "AI 种植指南"), style = MaterialTheme.typography.titleLarge)
                profile.sourceSummary?.takeIf { it.isNotBlank() }?.let {
                        Text(localizedPlanText(it), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
                }
                GuideRow(GardenIcon.Water, GardenText.s("Watering", "浇水"), profile.wateringText())
                GuideRow(GardenIcon.Fertilise, GardenText.s("Fertilising", "施肥"), profile.fertilisingText())
                GuideRow(GardenIcon.HighTemperature, GardenText.s("Preferred temperature", "适宜温度"), profile.temperatureText())
                GuideRow(GardenIcon.Harvest, GardenText.s("Estimated harvest", "预计收获"), profile.harvestText())
            }
        }
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text(GardenText.addToGarden) }
        OutlinedButton(onClick = onRegenerate, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text(GardenText.regenerate) }
    }
}

@Composable
private fun GuideRow(icon: GardenIcon, title: String, body: String) {
    Row(verticalAlignment = Alignment.Top) {
        GardenIconBadge(icon, modifier = Modifier.size(42.dp))
        Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    GardenCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f))
        }
    }
}

private fun PlantProfile.harvestText(): String {
    val min = harvestMinDays
    val max = harvestMaxDays
    return if (min != null && max != null && min > 0 && max >= min) {
        GardenText.s("$min-$max days", "$min-$max 天")
    } else {
        GardenText.notEnoughData
    }
}

private fun PlantProfile.temperatureText(): String {
    val min = preferredTempMinC
    val max = preferredTempMaxC
    return if (min != null && max != null && max >= min) {
        "${min.formatNumber()}-${max.formatNumber()}°C"
    } else {
        GardenText.notEnoughData
    }
}

private fun PlantProfile.wateringText(): String = buildString {
    append(GardenText.s("Every $wateringIntervalDays days", "每 $wateringIntervalDays 天"))
    wateringAmountMm?.takeIf { it > 0.0 }?.let {
        append("\n${GardenText.s("Recommended amount", "推荐水量")}: ${it.formatNumber()} mm")
    }
    append("\n${GardenText.s("Skip when rain is above", "降雨超过时跳过")}: ${rainSkipThresholdMm.formatNumber()} mm")
}

private fun PlantProfile.fertilisingText(): String = buildString {
    append(GardenText.s("Every $fertilisingIntervalDays days", "每 $fertilisingIntervalDays 天"))
    fertilisingAdvice?.takeIf { it.isNotBlank() }?.let {
        append("\n${localizedPlanText(it)}")
    }
}

private fun localizedPlanText(raw: String): String {
    if (!GardenText.isZh) return raw
    val text = raw.trim()
    val lower = text.lowercase()
    val looksEnglish = text.any { it in 'A'..'Z' || it in 'a'..'z' } && text.none { it in '\u4e00'..'\u9fff' }
    return when {
        lower == "offline starter plan" -> "离线初始计划"
        lower == "manual starter plan" -> "手动初始计划"
        "fertilis" in lower || "fertiliz" in lower || "6-4-6" in lower -> GardenPlantText.careAdvice(text)
        "assumes" in lower || "based on" in lower -> "AI 基于你的输入生成了这份护理计划。"
        looksEnglish -> "AI 已生成护理建议，可根据植物状态微调。"
        else -> text
    }
}

private fun Double.formatNumber(): String =
    if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)

@Composable
private fun PlantDatesEditor(
    sowingDate: LocalDate?,
    lastWateredDate: LocalDate?,
    currentStatus: PlantStartStatus,
    onSowingDate: (LocalDate?) -> Unit,
    onLastWateredDate: (LocalDate?) -> Unit,
    onError: (String?) -> Unit
) {
    GardenCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(GardenText.s("Plant dates", "植物日期"), style = MaterialTheme.typography.titleLarge)
            if (currentStatus == PlantStartStatus.ESTABLISHED_PLANT || currentStatus == PlantStartStatus.MATURE_PLANT) {
                Text(
                    GardenText.s(
                        "For established plants, leave planted date blank if you do not know it. GardenFlow will use your selected current stage instead.",
                        "成熟或已定植的植物如果不知道种植日期，可以留空；GardenFlow 会优先使用你选择的当前状态。"
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                )
            }
            DateField(
                title = GardenText.s("When was it planted?", "什么时候种下的？"),
                value = sowingDate,
                allowBlank = true,
                blankLabel = GardenText.notPlantedYet,
                onValue = onSowingDate,
                onError = onError
            )
            DateField(
                title = GardenText.s("Last watered", "上次浇水"),
                value = lastWateredDate,
                allowBlank = true,
                blankLabel = GardenText.notRecorded,
                onValue = onLastWateredDate,
                onError = onError
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    title: String,
    value: LocalDate?,
    allowBlank: Boolean,
    blankLabel: String,
    onValue: (LocalDate?) -> Unit,
    onError: (String?) -> Unit
) {
    var text by remember(value) { mutableStateOf(value?.toString().orEmpty()) }
    var showPicker by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = text,
            onValueChange = { raw ->
                text = raw
                val trimmed = raw.trim()
                when {
                    trimmed.isBlank() && allowBlank -> {
                        onValue(null)
                        onError(null)
                    }
                    trimmed.length >= 10 -> {
                        val parsed = parseDate(trimmed)
                        if (parsed == null) {
                            onError(GardenText.s("Use date format YYYY-MM-DD, for example 2026-07-23.", "请使用 YYYY-MM-DD 格式，例如 2026-07-23。"))
                        } else {
                            onValue(parsed)
                            onError(null)
                        }
                    }
                }
            },
            label = { Text("YYYY-MM-DD") },
            trailingIcon = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Filled.DateRange, contentDescription = GardenText.chooseDate)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (showPicker) {
            val pickerState = rememberDatePickerState(initialSelectedDateMillis = value?.toEpochMillis())
            DatePickerDialog(
                onDismissRequest = { showPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val d = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            text = d.toString()
                            onValue(d)
                            onError(null)
                        }
                        showPicker = false
                    }) { Text(GardenText.select) }
                },
                dismissButton = {
                    TextButton(onClick = { showPicker = false }) { Text(GardenText.cancel) }
                }
            ) {
                DatePicker(state = pickerState)
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        ) {
            DateChip(GardenText.today, value == LocalDate.now()) {
                val d = LocalDate.now()
                text = d.toString()
                onValue(d)
                onError(null)
            }
            DateChip(GardenText.yesterday, value == LocalDate.now().minusDays(1)) {
                val d = LocalDate.now().minusDays(1)
                text = d.toString()
                onValue(d)
                onError(null)
            }
            DateChip(GardenText.threeDaysAgo, value == LocalDate.now().minusDays(3)) {
                val d = LocalDate.now().minusDays(3)
                text = d.toString()
                onValue(d)
                onError(null)
            }
            if (allowBlank) {
                DateChip(blankLabel, value == null) {
                    text = ""
                    onValue(null)
                    onError(null)
                }
            }
        }
    }
}

private fun LocalDate.toEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun parseDate(raw: String): LocalDate? = try {
    LocalDate.parse(raw)
} catch (_: DateTimeParseException) {
    null
}

@Composable
private fun DateChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

private fun PlantStartStatus.label(): String = when (this) {
    PlantStartStatus.SEED -> GardenText.s("Seed packet / seed", "种子 / 种子包装")
    PlantStartStatus.SEEDLING -> GardenText.s("Seedling", "幼苗")
    PlantStartStatus.YOUNG_PLANT -> GardenText.s("Young nursery plant", "苗圃小植株")
    PlantStartStatus.ESTABLISHED_PLANT -> GardenText.s("Established plant", "已定植植株")
    PlantStartStatus.MATURE_PLANT -> GardenText.s("Mature plant", "成熟植株")
    PlantStartStatus.NOT_SURE -> GardenText.s("Not sure", "不确定")
}

private fun PlantStartStatus.icon(): GardenIcon = when (this) {
    PlantStartStatus.SEED -> GardenIcon.Custom
    PlantStartStatus.SEEDLING -> GardenIcon.Seedling
    PlantStartStatus.YOUNG_PLANT -> GardenIcon.CheckGrowth
    PlantStartStatus.ESTABLISHED_PLANT -> GardenIcon.Harvest
    PlantStartStatus.MATURE_PLANT -> GardenIcon.Sunflower
    PlantStartStatus.NOT_SURE -> GardenIcon.About
}

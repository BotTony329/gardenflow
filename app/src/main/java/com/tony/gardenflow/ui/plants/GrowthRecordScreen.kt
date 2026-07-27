package com.tony.gardenflow.ui.plants

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.tony.gardenflow.domain.model.Plant
import com.tony.gardenflow.domain.model.PlantPhoto
import com.tony.gardenflow.ui.components.PlantPhotoOrIcon
import com.tony.gardenflow.ui.components.rememberGardenMetrics
import com.tony.gardenflow.util.GardenText
import com.tony.gardenflow.util.createCameraImageUri
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun GrowthRecordScreen(onBack: () -> Unit, vm: PlantDetailViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val plant = state.plant
    val photos = state.photos
    val metrics = rememberGardenMetrics()
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                runCatching {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            vm.addPhotos(uris.map { it.toString() })
        }
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = pendingCameraUri
        if (ok && uri != null) {
            vm.addPhotos(listOf(uri.toString()))
        }
        pendingCameraUri = null
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createCameraImageUri(context, "plant")
            pendingCameraUri = uri
            camera.launch(uri)
        }
    }
    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val uri = createCameraImageUri(context, "plant")
            pendingCameraUri = uri
            camera.launch(uri)
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, top = 12.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = GardenText.back) }
                Text(GardenText.s("Growth album", "成长相册"), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            }
        }
    ) { padding ->
        if (plant == null) {
            Text(GardenText.s("Plant not found", "找不到植物"), Modifier.padding(padding).padding(24.dp))
            return@Scaffold
        }

        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            val columns = if (metrics.isTablet) 3 else 2
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize().widthIn(max = metrics.maxContentWidth),
                contentPadding = PaddingValues(
                    start = metrics.screenPadding,
                    top = 20.dp,
                    end = metrics.screenPadding,
                    bottom = 36.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    AlbumHeader(plant = plant, photoCount = photos.size)
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    if (photos.isEmpty()) {
                        EmptyAlbumCard(plant = plant)
                    } else {
                        Text(albumMonthTitle(photos), style = MaterialTheme.typography.titleMedium)
                    }
                }
                items(photos, key = { it.id }) { photo ->
                    AlbumPhotoTile(photo = photo, onDelete = { vm.deletePhoto(photo) })
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { launchCamera() },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(GardenText.s("+ Record today", "+ 记录今天"))
                        }
                        OutlinedButton(
                            onClick = { picker.launch(arrayOf("image/*")) },
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) {
                            Text(GardenText.s("Add from album", "从相册添加"))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumHeader(plant: Plant, photoCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(plant.name, style = MaterialTheme.typography.displaySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(
            albumSummary(plant = plant, photoCount = photoCount),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
        )
    }
}

@Composable
private fun EmptyAlbumCard(plant: Plant) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        contentAlignment = Alignment.Center
    ) {
        PlantPhotoOrIcon(plant = plant, modifier = Modifier.size(96.dp))
    }
}

@Composable
private fun AlbumPhotoTile(photo: PlantPhoto, onDelete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            AsyncImage(
                model = photo.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(34.dp)
                    .clickable(onClick = onDelete),
                shape = RoundedCornerShape(17.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("×", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        Text(
            photoDayTitle(photo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
        )
    }
}

private fun albumSummary(plant: Plant, photoCount: Int): String {
    val day = plant.sowingDate
        ?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).coerceAtLeast(0) }
    return if (GardenText.isZh) {
        buildString {
            append(photoCount).append(" 张照片")
            if (day != null) append(" · 记录第 ").append(day).append(" 天的变化")
        }
    } else {
        buildString {
            append(photoCount).append(if (photoCount == 1) " photo" else " photos")
            if (day != null) append(" · day ").append(day).append(" of changes")
        }
    }
}

private fun albumMonthTitle(photos: List<PlantPhoto>): String {
    val latest = photos.firstOrNull()?.capturedAt?.atZone(ZoneId.systemDefault())?.toLocalDate()
        ?: LocalDate.now()
    return if (GardenText.isZh) {
        "${latest.year}年${latest.monthValue}月"
    } else {
        latest.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    }
}

private fun photoDayTitle(photo: PlantPhoto): String {
    val date = photo.capturedAt.atZone(ZoneId.systemDefault()).toLocalDate()
    return if (GardenText.isZh) {
        "${date.monthValue}月${date.dayOfMonth}日"
    } else {
        date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    }
}

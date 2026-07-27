package com.tony.gardenflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tony.gardenflow.domain.model.Plant

@Composable
fun PlantPhotoOrIcon(
    plant: Plant,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier.size(34.dp)
) {
    val photoUri = plant.photoUri?.takeIf { it.isNotBlank() }
    val shape = MaterialTheme.shapes.extraLarge
    if (photoUri != null) {
        AsyncImage(
            model = photoUri,
            contentDescription = plant.name,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape)
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            GardenLineIcon(icon = plant.gardenIcon(), modifier = iconModifier)
        }
    }
}

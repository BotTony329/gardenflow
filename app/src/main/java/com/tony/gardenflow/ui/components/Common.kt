package com.tony.gardenflow.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tony.gardenflow.domain.model.GardenTask
import com.tony.gardenflow.domain.model.TaskType
import com.tony.gardenflow.util.GardenText

@Composable
fun GardenScreen(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}

@Composable
fun GardenCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        content()
    }
}

@Composable
fun CircleBadge(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun TaskCard(task: GardenTask, onDone: () -> Unit, onSnooze: () -> Unit, onSkip: () -> Unit) {
    GardenCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GardenIconBadge(icon = task.type.gardenIcon(), modifier = Modifier.size(48.dp))
                Column(Modifier.padding(start = 14.dp)) {
                    Text(GardenText.s("${task.type.label()} today", "${task.type.label()} 今天"), style = MaterialTheme.typography.headlineSmall)
                    Text(task.plantName, style = MaterialTheme.typography.titleMedium)
                }
            }
            Text(task.reason ?: GardenText.s("Due today", "今天到期"), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onDone, modifier = Modifier.weight(1f).height(52.dp)) { Text(GardenText.s("Done", "完成")) }
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier.weight(1f).height(52.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) { Text(GardenText.s("Snooze", "稍后提醒")) }
            }
        }
    }
}

@Composable
fun ErrorText(message: String?) {
    if (!message.isNullOrBlank()) {
        Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun ConfirmDeleteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(GardenText.s("Delete plant?", "删除植物？")) },
        text = { Text(GardenText.s("This removes the plant and its generated reminders from this phone.", "这会从手机中删除该植物以及它生成的提醒。")) },
        confirmButton = { Button(onClick = onConfirm) { Text(GardenText.s("Delete", "删除")) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(GardenText.cancel) } }
    )
}

fun TaskType.label() = when (this) {
    TaskType.WATER -> GardenText.water
    TaskType.FERTILISE -> GardenText.fertilise
    TaskType.CHECK_GROWTH -> GardenText.s("Check growth", "检查生长")
    TaskType.HARVEST -> GardenText.s("Harvest", "收获")
    TaskType.CUSTOM -> GardenText.s("Task", "任务")
}

fun TaskType.symbol() = when (this) {
    TaskType.WATER -> "Water"
    TaskType.FERTILISE -> "Fertilise"
    TaskType.CHECK_GROWTH -> "Growth"
    TaskType.HARVEST -> "Harvest"
    TaskType.CUSTOM -> "Task"
}

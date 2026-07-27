package com.tony.gardenflow.ui.plants

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tony.gardenflow.domain.engine.GrowthStageCalculator
import com.tony.gardenflow.domain.model.CareHistory
import com.tony.gardenflow.domain.model.GrowthStage
import com.tony.gardenflow.domain.model.Plant
import com.tony.gardenflow.domain.model.PlantPhoto
import com.tony.gardenflow.domain.repository.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PlantDetailUiState(
    val plant: Plant? = null,
    val currentStage: GrowthStage? = null,
    val history: List<CareHistory> = emptyList(),
    val photos: List<PlantPhoto> = emptyList(),
    val deleted: Boolean = false
)

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GardenRepository,
    private val calculator: GrowthStageCalculator
) : ViewModel() {
    private val plantId: String = checkNotNull(savedStateHandle["plantId"])
    val state: StateFlow<PlantDetailUiState> = combine(
        repository.observePlants().map { plants -> plants.firstOrNull { it.id == plantId } },
        repository.observeHistory(),
        repository.observePlantPhotos(plantId)
    ) { plant, history, photos ->
        val estimated = plant?.confirmedStageKey
            ?.let { key -> plant.growthStages.firstOrNull { it.key == key } }
            ?: plant?.sowingDate?.let { calculator.calculateCurrentStage(it, plant.growthStages, LocalDate.now()) }
        PlantDetailUiState(
            plant = plant,
            currentStage = estimated,
            history = history.filter { it.plantId == plantId },
            photos = photos
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlantDetailUiState())

    fun delete() = viewModelScope.launch { repository.deletePlant(plantId) }

    fun updatePhoto(photoUri: String?) = viewModelScope.launch {
        repository.updatePlantPhoto(plantId, photoUri)
    }

    fun addPhotos(photoUris: List<String>) = viewModelScope.launch {
        repository.addPlantPhotos(plantId, photoUris)
    }

    fun deletePhoto(photo: PlantPhoto) = viewModelScope.launch {
        repository.deletePlantPhoto(plantId, photo.id, photo.uri)
    }

    fun confirmStage(stageKey: String) = viewModelScope.launch {
        repository.confirmStage(plantId, stageKey, LocalDate.now())
    }
}

package com.tony.gardenflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tony.gardenflow.domain.repository.GardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppLanguageViewModel @Inject constructor(
    repository: GardenRepository
) : ViewModel() {
    val languageCode: StateFlow<String?> = repository.observeSettings()
        .map { it.languageCode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

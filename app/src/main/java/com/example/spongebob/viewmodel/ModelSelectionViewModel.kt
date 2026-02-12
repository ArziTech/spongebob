package com.example.spongebob.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spongebob.data.PreferencesManager
import com.example.spongebob.model.ModelConfig
import com.example.spongebob.model.ModelManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for model selection screen
 */
data class ModelSelectionUiState(
    val models: List<ModelConfig> = emptyList(),
    val selectedModelId: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for model selection functionality.
 *
 * Manages loading available models and tracking the currently selected model.
 */
class ModelSelectionViewModel(
    private val application: Application,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val modelManager = ModelManager(application)

    private val _uiState = MutableStateFlow(ModelSelectionUiState())
    val uiState: StateFlow<ModelSelectionUiState> = _uiState.asStateFlow()

    init {
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Load model configurations
                val models = modelManager.loadModelConfigs()

                // Get currently selected model ID
                val selectedId = preferencesManager.selectedModelId.first()

                _uiState.update { it.copy(
                    models = models,
                    selectedModelId = selectedId,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load models: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Select a model as the current model
     */
    fun selectModel(modelId: String) {
        viewModelScope.launch {
            try {
                preferencesManager.setSelectedModelId(modelId)
                _uiState.update { it.copy(selectedModelId = modelId) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Failed to select model: ${e.message}"
                ) }
            }
        }
    }

    /**
     * Get a model configuration by ID
     */
    fun getModelById(id: String): ModelConfig? {
        return _uiState.value.models.find { it.id == id }
    }

    /**
     * Clear any error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Refresh the model list and current selection
     */
    fun refresh() {
        loadModels()
    }
}

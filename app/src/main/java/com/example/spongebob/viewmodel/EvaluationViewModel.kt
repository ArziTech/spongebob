package com.example.spongebob.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spongebob.data.entity.EvaluationEntity
import com.example.spongebob.data.entity.EvaluationGroupEntity
import com.example.spongebob.data.repository.EvaluationRepository
import com.example.spongebob.model.OnnxModelManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

data class EvaluationUiState(
    val isLoading: Boolean = true,
    val availableGroups: List<EvaluationGroupEntity> = emptyList(),
    val groupCounts: Map<Long, Int> = emptyMap(),
    val groupAccuracy: Map<Long, Float> = emptyMap(),
    val selectedImageUri: Uri? = null,
    val expectedClass: String = "",
    val selectedGroupId: Long? = null,
    val selectedGroupName: String? = null,
    val isProcessing: Boolean = false,
    val canRunEvaluation: Boolean = false,
    val lastEvaluationId: Long? = null
)

class EvaluationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EvaluationRepository(application)
    private val onnxModelManager = OnnxModelManager(application)

    private val _uiState = MutableStateFlow(EvaluationUiState())
    val uiState: StateFlow<EvaluationUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
        initializeModel()
    }

    private fun initializeModel() {
        viewModelScope.launch {
            try {
                if (!onnxModelManager.isInitialized) {
                    onnxModelManager.initialize()
                }
            } catch (e: Exception) {
                // Handle model initialization error
            }
        }
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getAllGroups().collect { groups ->
                val groupCounts = mutableMapOf<Long, Int>()
                val groupAccuracy = mutableMapOf<Long, Float>()

                groups.forEach { group ->
                    val count = repository.getEvaluationCountForGroup(group.groupId)
                    groupCounts[group.groupId] = count

                    if (count > 0) {
                        val stats = repository.getGroupAccuracyStats(group.groupId)
                        groupAccuracy[group.groupId] = stats.accuracy
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        availableGroups = groups,
                        groupCounts = groupCounts,
                        groupAccuracy = groupAccuracy
                    )
                }
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        updateCanRunEvaluation(uri, _uiState.value.expectedClass, _uiState.value.selectedGroupId)
    }

    fun onExpectedClassChanged(value: String) {
        _uiState.update { it.copy(expectedClass = value) }
        updateCanRunEvaluation(
            _uiState.value.selectedImageUri,
            value,
            _uiState.value.selectedGroupId
        )
    }

    fun onGroupSelected(groupId: Long, groupName: String) {
        _uiState.update {
            it.copy(
                selectedGroupId = groupId,
                selectedGroupName = groupName
            )
        }
        updateCanRunEvaluation(
            _uiState.value.selectedImageUri,
            _uiState.value.expectedClass,
            groupId
        )
    }

    fun createNewGroup(groupName: String) {
        viewModelScope.launch {
            val groupId = repository.createGroup(groupName)
            onGroupSelected(groupId, groupName)
            loadGroups()
        }
    }

    private fun updateCanRunEvaluation(uri: Uri?, expectedClass: String, groupId: Long?) {
        _uiState.update {
            it.copy(
                selectedImageUri = uri,
                canRunEvaluation = uri != null &&
                        expectedClass.isNotBlank() &&
                        groupId != null &&
                        !it.isProcessing
            )
        }
    }

    fun runEvaluation() {
        val uri = _uiState.value.selectedImageUri ?: return
        val expectedClass = _uiState.value.expectedClass
        val groupId = _uiState.value.selectedGroupId ?: return

        if (!onnxModelManager.isInitialized) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            try {
                val result = onnxModelManager.runInference(uri)
                val inferenceTime = result.inferenceTimeMillis

                val isCorrect = result.className == expectedClass
                val evalId = repository.saveEvaluation(
                    groupId = groupId,
                    imageUri = uri,
                    expectedClass = expectedClass,
                    predictedClass = result.className,
                    confidence = result.confidence,
                    isCorrect = isCorrect,
                    inferenceTimeMillis = inferenceTime,
                    allPredictions = result.allPredictions
                )

                _uiState.update {
                    it.copy(
                        lastEvaluationId = evalId,
                        isProcessing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    fun onNavigationComplete() {
        _uiState.update {
            it.copy(
                lastEvaluationId = null,
                selectedImageUri = null,
                expectedClass = ""
            )
        }
    }

    suspend fun getEvaluationById(id: Long): EvaluationEntity? {
        return repository.getEvaluationById(id)
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            repository.deleteGroup(groupId)
            loadGroups()
        }
    }

    override fun onCleared() {
        super.onCleared()
        onnxModelManager.close()
    }

    fun getClassLabels(): List<String> {
        return OnnxModelManager.CLASS_LABELS
    }
}

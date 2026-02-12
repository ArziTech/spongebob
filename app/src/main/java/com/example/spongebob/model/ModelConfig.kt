package com.example.spongebob.model

import kotlinx.serialization.Serializable

/**
 * Enum representing supported model types
 */
enum class ModelType {
    TFLITE,
    ONNX
}

/**
 * Configuration data for a single ML model
 *
 * @property id Unique identifier for the model (used in preferences)
 * @property file Model filename in assets folder
 * @property name Display name for the model
 * @property description Human-readable description of the model
 * @property type Type of model (TFLITE or ONNX)
 * @property inputSize Input image size (assumes square input: inputSize x inputSize)
 * @property classes List of class labels for model output
 */
@Serializable
data class ModelConfig(
    val id: String,
    val file: String,
    val name: String,
    val description: String,
    val type: ModelType,
    val inputSize: Int,
    val classes: List<String>
)

/**
 * Root configuration containing all available models
 *
 * @property models List of model configurations
 */
@Serializable
data class ModelsConfig(
    val models: List<ModelConfig>
)

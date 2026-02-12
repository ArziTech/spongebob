package com.example.spongebob.model

import kotlinx.serialization.Serializable

/**
 * Enum representing supported model types
 */
enum class ModelType {
    TFLITE,
    ONNX;

    companion object {
        fun fromString(value: String): ModelType {
            return when (value.lowercase()) {
                "tflite" -> TFLITE
                "onnx" -> ONNX
                else -> TFLITE // default
            }
        }
    }
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
) {
    companion object {
        /**
         * Create ModelConfig from a map (for YAML parsing)
         */
        fun fromMap(map: Map<String, Any>): ModelConfig {
            val typeString = map["type"] as? String ?: "tflite"
            return ModelConfig(
                id = map["id"] as? String ?: "",
                file = map["file"] as? String ?: "",
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                type = ModelType.fromString(typeString),
                inputSize = (map["inputSize"] as? Number)?.toInt() ?: 640,
                classes = (map["classes"] as? List<*>)?.map { it as? String ?: "" } ?: emptyList()
            )
        }
    }
}

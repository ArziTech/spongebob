package com.example.spongebob.navigation

import kotlinx.serialization.Serializable

// ==================== MAIN ROUTES ====================

// Main Menu - New Start Destination
@Serializable
object MainMenu

// Input (existing - now navigated from MainMenu)
@Serializable
object Input

// Camera (existing)
@Serializable
object Camera

// Crop (existing)
@Serializable
data class Crop(val imageUri: String)

// Inference (existing)
@Serializable
object Inference

// Result (existing)
@Serializable
data class Result(
    val className: String,
    val confidence: Float
)

// Settings (existing)
@Serializable
object Settings

// NNAPI Prompt (existing)
@Serializable
object NnapiPrompt

// ==================== EVALUATION ROUTES ====================

// Evaluation Home/Dashboard
@Serializable
object EvaluationHome

// Evaluation Input - select image and specify expected class
@Serializable
object EvaluationInput

// Evaluation Result
@Serializable
data class EvaluationResult(
    val evaluationId: Long,
    val groupId: Long
)

// Evaluation History
@Serializable
data class EvaluationHistory(
    val groupId: Long
)

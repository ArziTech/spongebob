package com.example.spongebob.navigation

import kotlinx.serialization.Serializable

// Navigation routes using type-safe navigation
@Serializable
object Input

@Serializable
object Camera

@Serializable
data class Crop(val imageUri: String)

@Serializable
object Inference

@Serializable
data class Result(
    val className: String,
    val confidence: Float
)

@Serializable
object Settings

@Serializable
object NnapiPrompt

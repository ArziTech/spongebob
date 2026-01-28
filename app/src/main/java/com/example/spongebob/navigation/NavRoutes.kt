package com.example.spongebob.navigation

import kotlinx.serialization.Serializable

// Navigation routes using type-safe navigation
@Serializable
object Input

@Serializable
object Camera

@Serializable
object Inference

@Serializable
data class Result(
    val className: String,
    val confidence: Float
)

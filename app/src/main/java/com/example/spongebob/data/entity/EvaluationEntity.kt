package com.example.spongebob.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "evaluations",
    foreignKeys = [
        ForeignKey(
            entity = EvaluationGroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"])]
)
data class EvaluationEntity(
    @PrimaryKey(autoGenerate = true)
    val evaluationId: Long = 0,
    val groupId: Long,
    val imageUri: String,  // Path to stored image file
    val expectedClass: String,  // Ground truth label
    val predictedClass: String,
    val confidence: Float,
    val isCorrect: Boolean,
    val inferenceTimeMillis: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val allPredictionsJson: String? = null  // JSON string of all predictions
)

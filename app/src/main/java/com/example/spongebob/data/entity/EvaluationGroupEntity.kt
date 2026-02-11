package com.example.spongebob.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "evaluation_groups",
    indices = [Index(value = ["groupName"])]
)
data class EvaluationGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val groupId: Long = 0,
    val groupName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val description: String? = null
)

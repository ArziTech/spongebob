package com.example.spongebob.data.repository

import android.content.Context
import android.net.Uri
import com.example.spongebob.data.SpongeBobDatabase
import com.example.spongebob.data.dao.ConfusionMatrixEntry
import com.example.spongebob.data.dao.GroupAccuracyStats
import com.example.spongebob.data.dao.EvaluationDao
import com.example.spongebob.data.dao.EvaluationGroupDao
import com.example.spongebob.data.entity.EvaluationEntity
import com.example.spongebob.data.entity.EvaluationGroupEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

class EvaluationRepository(context: Context) {

    private val database = SpongeBobDatabase.getInstance(context)
    private val groupDao: EvaluationGroupDao = database.evaluationGroupDao()
    private val evaluationDao: EvaluationDao = database.evaluationDao()
    private val appContext = context.applicationContext
    private val json = Json { ignoreUnknownKeys = true }

    // Group operations
    suspend fun createGroup(name: String, description: String? = null): Long {
        val group = EvaluationGroupEntity(
            groupName = name,
            description = description
        )
        return groupDao.insert(group)
    }

    suspend fun getGroupByName(name: String): EvaluationGroupEntity? {
        return groupDao.getGroupByName(name)
    }

    suspend fun getGroupById(groupId: Long): EvaluationGroupEntity? {
        return groupDao.getGroupById(groupId)
    }

    fun getAllGroups(): Flow<List<EvaluationGroupEntity>> {
        return groupDao.getAllGroups()
    }

    suspend fun getEvaluationCountForGroup(groupId: Long): Int {
        return groupDao.getEvaluationCountForGroup(groupId)
    }

    suspend fun deleteGroup(groupId: Long) {
        // Delete associated image files first
        val evaluations = evaluationDao.getEvaluationsByGroup(groupId).first()
        evaluations.forEach { eval ->
            deleteImageFile(eval.imageUri)
        }
        groupDao.deleteGroupById(groupId)
    }

    // Evaluation operations
    suspend fun saveEvaluation(
        groupId: Long,
        imageUri: Uri,
        expectedClass: String,
        predictedClass: String,
        confidence: Float,
        isCorrect: Boolean,
        inferenceTimeMillis: Long,
        allPredictions: List<com.example.spongebob.viewmodel.Prediction>
    ): Long {
        // Copy image to app-specific storage
        val storedImagePath = copyImageToAppStorage(imageUri)

        val evaluation = EvaluationEntity(
            groupId = groupId,
            imageUri = storedImagePath,
            expectedClass = expectedClass,
            predictedClass = predictedClass,
            confidence = confidence,
            isCorrect = isCorrect,
            inferenceTimeMillis = inferenceTimeMillis,
            allPredictionsJson = json.encodeToString(allPredictions)
        )

        return evaluationDao.insert(evaluation)
    }

    fun getEvaluationsByGroup(groupId: Long): Flow<List<EvaluationEntity>> {
        return evaluationDao.getEvaluationsByGroup(groupId)
    }

    fun getRecentEvaluations(limit: Int = 10): Flow<List<EvaluationEntity>> {
        return evaluationDao.getRecentEvaluations(limit)
    }

    suspend fun getEvaluationById(evaluationId: Long): EvaluationEntity? {
        return evaluationDao.getEvaluationById(evaluationId)
    }

    suspend fun getGroupAccuracyStats(groupId: Long): GroupAccuracyStats {
        return evaluationDao.getGroupAccuracyStats(groupId)
    }

    suspend fun getConfusionMatrixData(groupId: Long): List<ConfusionMatrixEntry> {
        return evaluationDao.getConfusionMatrixData(groupId)
    }

    fun getAllClasses(): Flow<List<String>> {
        return evaluationDao.getAllClasses()
    }

    suspend fun deleteEvaluation(evaluationId: Long) {
        val evaluation = evaluationDao.getEvaluationById(evaluationId)
        evaluation?.let {
            deleteImageFile(it.imageUri)
            evaluationDao.delete(it)
        }
    }

    // Image file management
    private fun copyImageToAppStorage(uri: Uri): String {
        val inputStream = appContext.contentResolver.openInputStream(uri)
        val evaluationsDir = File(appContext.filesDir, "evaluations")
        if (!evaluationsDir.exists()) {
            evaluationsDir.mkdirs()
        }

        val fileName = "eval_${System.currentTimeMillis()}.jpg"
        val outputFile = File(evaluationsDir, fileName)

        inputStream?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }

        return outputFile.absolutePath
    }

    private fun deleteImageFile(filePath: String) {
        try {
            File(filePath).delete()
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }

    suspend fun getAllGroupsWithStats(): Flow<List<GroupWithStats>> {
        return getAllGroups().map { groups ->
            groups.map { group ->
                val count = getEvaluationCountForGroup(group.groupId)
                val accuracy = if (count > 0) {
                    getGroupAccuracyStats(group.groupId).accuracy
                } else {
                    0f
                }
                GroupWithStats(group, count, accuracy)
            }
        }
    }

    data class GroupWithStats(
        val group: EvaluationGroupEntity,
        val evaluationCount: Int,
        val accuracy: Float
    )
}

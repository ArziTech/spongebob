package com.example.spongebob.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import com.example.spongebob.data.entity.EvaluationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluationDao {
    @Insert
    suspend fun insert(evaluation: EvaluationEntity): Long

    @Update
    suspend fun update(evaluation: EvaluationEntity): Int

    @Delete
    suspend fun delete(evaluation: EvaluationEntity): Int

    @Query("SELECT * FROM evaluations WHERE evaluationId = :id")
    suspend fun getEvaluationById(id: Long): EvaluationEntity?

    @Query("SELECT * FROM evaluations WHERE groupId = :groupId ORDER BY timestamp DESC")
    fun getEvaluationsByGroup(groupId: Long): Flow<List<EvaluationEntity>>

    @Query("SELECT * FROM evaluations ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvaluations(limit: Int = 10): Flow<List<EvaluationEntity>>

    @Query("""
        SELECT
            COUNT(*) as total,
            SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correct
        FROM evaluations
        WHERE groupId = :groupId
    """)
    suspend fun getGroupAccuracyStats(groupId: Long): GroupAccuracyStats

    @Query("""
        SELECT expectedClass, predictedClass, COUNT(*) as count
        FROM evaluations
        WHERE groupId = :groupId
        GROUP BY expectedClass, predictedClass
        ORDER BY count DESC
    """)
    suspend fun getConfusionMatrixData(groupId: Long): List<ConfusionMatrixEntry>

    @Query("DELETE FROM evaluations WHERE groupId = :groupId")
    suspend fun deleteEvaluationsForGroup(groupId: Long): Int

    @Query("SELECT DISTINCT expectedClass FROM evaluations ORDER BY expectedClass")
    fun getAllClasses(): Flow<List<String>>
}

data class GroupAccuracyStats(
    val total: Int,
    val correct: Int
) {
    val accuracy: Float
        get() = if (total > 0) correct.toFloat() / total else 0f
}

data class ConfusionMatrixEntry(
    val expectedClass: String,
    val predictedClass: String,
    val count: Int
)

package com.example.spongebob.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import com.example.spongebob.data.entity.EvaluationGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluationGroupDao {
    @Insert
    suspend fun insert(group: EvaluationGroupEntity): Long

    @Update
    suspend fun update(group: EvaluationGroupEntity): Int

    @Delete
    suspend fun delete(group: EvaluationGroupEntity): Int

    @Query("SELECT * FROM evaluation_groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<EvaluationGroupEntity>>

    @Query("SELECT * FROM evaluation_groups WHERE groupId = :groupId")
    suspend fun getGroupById(groupId: Long): EvaluationGroupEntity?

    @Query("SELECT * FROM evaluation_groups WHERE groupName = :groupName LIMIT 1")
    suspend fun getGroupByName(groupName: String): EvaluationGroupEntity?

    @Query("SELECT COUNT(*) FROM evaluations WHERE groupId = :groupId")
    suspend fun getEvaluationCountForGroup(groupId: Long): Int

    @Query("DELETE FROM evaluation_groups WHERE groupId = :groupId")
    suspend fun deleteGroupById(groupId: Long): Int
}

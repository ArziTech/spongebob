package com.example.spongebob.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.spongebob.data.converters.Converters
import com.example.spongebob.data.dao.EvaluationDao
import com.example.spongebob.data.dao.EvaluationGroupDao
import com.example.spongebob.data.entity.EvaluationEntity
import com.example.spongebob.data.entity.EvaluationGroupEntity

@Database(
    entities = [EvaluationGroupEntity::class, EvaluationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SpongeBobDatabase : RoomDatabase() {
    abstract fun evaluationGroupDao(): EvaluationGroupDao
    abstract fun evaluationDao(): EvaluationDao

    companion object {
        private const val DATABASE_NAME = "spongebob_db"

        @Volatile
        private var INSTANCE: SpongeBobDatabase? = null

        fun getInstance(context: Context): SpongeBobDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpongeBobDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

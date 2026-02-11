package com.example.spongebob.data.converters

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.example.spongebob.viewmodel.Prediction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@ProvidedTypeConverter
object Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromPredictionList(predictions: List<Prediction>): String {
        return json.encodeToString(predictions)
    }

    @TypeConverter
    fun toPredictionList(jsonString: String): List<Prediction> {
        return try {
            json.decodeFromString<List<Prediction>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

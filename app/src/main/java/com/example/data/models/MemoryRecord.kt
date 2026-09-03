package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.UUID

@Entity(tableName = "memory_records")
@JsonClass(generateAdapter = true)
data class MemoryRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "local_user",
    val text: String,
    val category: String = "ಸಾಮಾನ್ಯ", // ಕೃಷಿ, ಹಸು, ಕುಟುಂಬ, ಕಾರ್ಯಕ್ರಮ, ಖರ್ಚು, ಸಾಮಾನ್ಯ
    val date: String, // YYYY-MM-DD
    val time: String, // HH:mm
    val timestamp: Long = System.currentTimeMillis(),
    val voiceFilePath: String? = null,
    val photoUri: String? = null,
    val relatedAnimal: String? = null,
    val relatedPlot: String? = null,
    val relatedEvent: String? = null,
    val sourceLanguage: String = "kn",
    val syncStatus: String = "SYNCED", // SYNCED, PENDING_SYNC, LOCAL_ONLY
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

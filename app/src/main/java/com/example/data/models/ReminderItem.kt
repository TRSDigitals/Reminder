package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.UUID

@Entity(tableName = "reminders")
@JsonClass(generateAdapter = true)
data class ReminderItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "local_user",
    val title: String,
    val description: String = "",
    val category: String = "ಸಾಮಾನ್ಯ", // ಕೃಷಿ, ಹಸು, ಕುಟುಂಬ, ಕಾರ್ಯಕ್ರಮ, ಸಾಮಾನ್ಯ
    val targetDate: String, // YYYY-MM-DD
    val targetTime: String, // HH:mm (e.g. 07:00, 18:30)
    val timestamp: Long, // Exact epoch millis scheduled for
    val type: String = "ONCE", // ONCE, RECURRING
    val recurrence: String = "NONE", // NONE, DAILY, WEEKLY, SUNDAY, SATURDAY, MONTHLY, YEARLY, DAYS_15
    val notifyAdvance24h: Boolean = true,
    val notifyOnTime: Boolean = true,
    val advanceNotified: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isActive: Boolean = true,
    val relatedAnimal: String? = null,
    val relatedPlot: String? = null,
    val syncStatus: String = "SYNCED",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

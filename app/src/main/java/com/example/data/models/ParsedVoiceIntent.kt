package com.example.data.models

import com.squareup.moshi.JsonClass

enum class IntentType {
    REMINDER,
    HISTORY,
    RECURRING_REMINDER,
    EVENT,
    FARM_RECORD,
    CATTLE_RECORD,
    NOTE
}

@JsonClass(generateAdapter = true)
data class ParsedVoiceIntent(
    val rawText: String,
    val intentType: IntentType,
    val title: String,
    val description: String = "",
    val targetDate: String, // YYYY-MM-DD
    val targetTime: String?, // HH:mm or null if missing
    val isPastHistory: Boolean, // true if already happened
    val category: String, // ಕೃಷಿ, ಹಸು, ಕುಟುಂಬ, ಕಾರ್ಯಕ್ರಮ, ಖರ್ಚು, ಸಾಮಾನ್ಯ
    val recurrence: String = "NONE", // NONE, DAILY, WEEKLY, SUNDAY, SATURDAY, MONTHLY, YEARLY, DAYS_15
    val relatedAnimal: String? = null,
    val relatedPlot: String? = null,
    val confidence: Float = 0.95f,
    val requiresTimeClarification: Boolean = false,
    val audioFilePath: String? = null
)

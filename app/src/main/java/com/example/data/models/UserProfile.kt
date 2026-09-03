package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "user_profile")
@JsonClass(generateAdapter = true)
data class UserProfile(
    @PrimaryKey
    val userId: String = "local_user",
    val phoneNumber: String = "",
    val name: String = "",
    val dob: String = "",
    val language: String = AppLanguage.KANNADA.name,
    val notifyAdvance24h: Boolean = true,
    val notifyOnTime: Boolean = true,
    val enableVoiceBackup: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isAccountActive: Boolean = true
)

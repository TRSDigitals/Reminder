package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.UUID

@Entity(tableName = "cattle_profiles")
@JsonClass(generateAdapter = true)
data class CattleProfile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "local_user",
    val name: String, // e.g. "ಲಕ್ಷ್ಮಿ", "ಗೌರಿ", "ಕಾವೇರಿ"
    val tagNumber: String = "",
    val breed: String = "ಹಳ್ಳಿಕಾರ್ / Malnad Gidda",
    val photoUri: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.util.UUID

@Entity(tableName = "farm_plots")
@JsonClass(generateAdapter = true)
data class FarmPlot(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "local_user",
    val name: String, // e.g. "ಅಡಿಕೆ ತೋಟ", "ಭತ್ತದ ಗದ್ದೆ", "ತೆಂಗಿನ ತೋಟ"
    val area: String = "2 ಎಕರೆ",
    val crop: String = "ಅಡಿಕೆ",
    val photoUri: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.models.IntentType
import com.example.data.models.ParsedVoiceIntent
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Intelligent Voice Intent Parser for DinaSiri.
 * Supports Kannada, Tulu, English, and mixed conversational dialects.
 * Accurately distinguishes History vs Future Reminders, extracts dates & times,
 * and classifies agricultural & domestic categories.
 */
class VoiceParserEngine {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Parses natural user speech into structured intent
     */
    suspend fun parseSpeech(rawSpeech: String, audioPath: String? = null): ParsedVoiceIntent = withContext(Dispatchers.Default) {
        val trimmed = rawSpeech.trim()
        if (trimmed.isBlank()) {
            return@withContext ParsedVoiceIntent(
                rawText = rawSpeech,
                intentType = IntentType.NOTE,
                title = "ನೆನಪು",
                targetDate = LocalDate.now().toString(),
                targetTime = null,
                isPastHistory = false,
                category = "ಸಾಮಾನ್ಯ",
                confidence = 0.0f
            )
        }

        // Try Gemini API online if key exists
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResult = callGeminiParser(trimmed, apiKey)
                if (geminiResult != null) {
                    return@withContext geminiResult.copy(audioFilePath = audioPath)
                }
            } catch (e: Exception) {
                Log.w("VoiceParserEngine", "Gemini API unavailable, falling back to local NLP", e)
            }
        }

        // Local Smart Conversational Parser Fallback
        return@withContext parseLocally(trimmed, audioPath)
    }

    /**
     * Local Offline Conversational Parser with Indian / Kannada / Tulu dialect understanding
     */
    fun parseLocally(text: String, audioPath: String? = null): ParsedVoiceIntent {
        val lower = text.lowercase()
        val today = LocalDate.now()
        val nowTime = LocalTime.now()

        // 1. Detect Past History vs Future Reminder
        // Past action markers: ಕೊಟ್ಟೆ, ಹಾಕಿದೆ, ಮಾಡಿದೆ, ಹೋದೆ, ತಂದೆ, ಮುಗಿಯಿತು, ಆಯಿತು, done, finished, went, bought, gave, watered
        val isPastHistory = containsAny(
            lower,
            "ಕೊಟ್ಟೆ", "ಹಾಕಿದೆ", "ಮಾಡಿದೆ", "ಹೋದೆ", "ತಂದೆ", "ಮುಗಿಯಿತು", "ಆಯಿತು", "ಖರ್ಚಾಯಿತು",
            "ನೀರು ಹಾಕಿದೆ", "ಗೊಬ್ಬರ ಹಾಕಿದೆ", "ಮಾಡ್ದೆ", "ಕೊಟ್ಟಿದ್ದೆ", "ಹೋಗಿದ್ದೆ", "ಖರೀದಿಸಿದೆ",
            "ಕೊರ್ತೆ", "ಪಾಡ್ಯೆ", "ಮಲ್ತೆ", "ಪೋಯೆ", "ತಂದೆ", "ಆಂಡ್", // Tulu past tense
            "done", "completed", "gave", "watered", "bought", "went", "paid", "sprayed", "harvested"
        )

        // Recurring markers
        val isRecurring = containsAny(
            lower,
            "ಪ್ರತಿ", "ವಾರವಾರ", "ದಿನವೂ", "ಪ್ರತಿದಿನ", "ಪ್ರತಿ ಭಾನುವಾರ", "ಪ್ರತಿ ಶನಿವಾರ", "ಪ್ರತಿ ಸೋಮವಾರ",
            "ಪ್ರತಿತಿಂಗಳು", "ಎವ್ರಿ", "every", "daily", "weekly", "monthly", "ಪ್ರತಿ 15 ದಿನ"
        )

        var recurrence = "NONE"
        if (isRecurring) {
            recurrence = when {
                containsAny(lower, "ಭಾನುವಾರ", "sunday", "ಐತಾರ") -> "SUNDAY"
                containsAny(lower, "ಶನಿವಾರ", "saturday", "ಸನಿಯಾರ") -> "SATURDAY"
                containsAny(lower, "ಸೋಮವಾರ", "monday", "ಸೋಮಾರ") -> "WEEKLY"
                containsAny(lower, "ದಿನ", "daily", "ದಿನಾಲೂ") -> "DAILY"
                containsAny(lower, "ತಿಂಗಳು", "monthly") -> "MONTHLY"
                containsAny(lower, "15 ದಿನ", "15 days") -> "DAYS_15"
                else -> "WEEKLY"
            }
        }

        // 2. Extract Date
        var targetDate = today
        when {
            containsAny(lower, "ನಿನ್ನೆ", "yesterday", "ಕೋಡೆ") -> {
                targetDate = today.minusDays(1)
            }
            containsAny(lower, "ನಾಳೆ", "tomorrow", "ಎಲ್ಲೆ") -> {
                targetDate = today.plusDays(1)
            }
            containsAny(lower, "ನಾಡಿದ್ದು", "day after tomorrow", "ಮರ್ಯಾದೆಲ್ಲೆ") -> {
                targetDate = today.plusDays(2)
            }
            containsAny(lower, "ಮುಂದಿನ ಭಾನುವಾರ", "next sunday", "ಬರ್ಪಿನ ಐತಾರ") -> {
                targetDate = today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
            }
            containsAny(lower, "ಮುಂದಿನ ಶನಿವಾರ", "next saturday", "ಬರ್ಪಿನ ಸನಿಯಾರ") -> {
                targetDate = today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
            }
            containsAny(lower, "ಮುಂದಿನ ಸೋಮವಾರ", "next monday") -> {
                targetDate = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            }
            containsAny(lower, "ಮುಂದಿನ ವಾರ", "next week") -> {
                targetDate = today.plusWeeks(1)
            }
            containsAny(lower, "ಒಂದು ತಿಂಗಳ ನಂತರ", "after 1 month", "1 month later") -> {
                targetDate = today.plusMonths(1)
            }
            containsAny(lower, "15 ದಿನಗಳ ನಂತರ", "15 days later") -> {
                targetDate = today.plusDays(15)
            }
            else -> {
                // Check for specific date patterns like "15 ಸೆಪ್ಟೆಂಬರ್", "ಸೆಪ್ಟೆಂಬರ್ 15", "15 September"
                val monthDayMatch = parseSpecificDate(lower, today.year)
                if (monthDayMatch != null) {
                    targetDate = monthDayMatch
                }
            }
        }

        // 3. Extract Time
        val (targetTime, hasTime) = extractTimeFromSpeech(lower)

        // 4. Extract Category & Entities
        val (category, animal, plot) = extractCategoryAndEntities(lower)

        // 5. Intent Type Classification
        val intentType = when {
            isRecurring -> IntentType.RECURRING_REMINDER
            isPastHistory -> {
                when (category) {
                    "ಕೃಷಿ" -> IntentType.FARM_RECORD
                    "ಹಸು" -> IntentType.CATTLE_RECORD
                    else -> IntentType.HISTORY
                }
            }
            containsAny(lower, "ಹುಟ್ಟುಹಬ್ಬ", "ಮದುವೆ", "ಜಾತ್ರೆ", "ಹಬ್ಬ", "ಕಾರ್ಯಕ್ರಮ", "birthday", "anniversary", "festival", "function") -> IntentType.EVENT
            containsAny(lower, "ಕೊಡಬೇಕು", "ಮಾಡಬೇಕು", "ಹೋಗಬೇಕು", "ತರಬೇಕು", "ನೆನಪಿಸು", "ಜ್ಞಾಪಿಸು", "remind", "have to", "need to", "ನೆಂಪು ಮಲ್ಪು") -> IntentType.REMINDER
            else -> if (hasTime || targetDate.isAfter(today)) IntentType.REMINDER else IntentType.NOTE
        }

        val requiresTimeClarification = (intentType == IntentType.REMINDER || intentType == IntentType.RECURRING_REMINDER) && !hasTime

        // Clean Title
        val cleanTitle = generateCleanTitle(text, category, animal, plot)

        return ParsedVoiceIntent(
            rawText = text,
            intentType = intentType,
            title = cleanTitle,
            description = text,
            targetDate = targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            targetTime = targetTime,
            isPastHistory = isPastHistory,
            category = category,
            recurrence = recurrence,
            relatedAnimal = animal,
            relatedPlot = plot,
            confidence = 0.95f,
            requiresTimeClarification = requiresTimeClarification,
            audioFilePath = audioPath
        )
    }

    private fun extractTimeFromSpeech(lower: String): Pair<String?, Boolean> {
        val morning = containsAny(lower, "ಬೆಳಿಗ್ಗೆ", "ಬೆಳಗ್ಗೆ", "ಕಾಂಡೆ", "morning", "am")
        val afternoon = containsAny(lower, "ಮಧ್ಯಾಹ್ನ", "ಮದ್ಯಾಹ್ನ", "afternoon")
        val evening = containsAny(lower, "ಸಂಜೆ", "ಬಯ್ಯ", "evening")
        val night = containsAny(lower, "ರಾತ್ರಿ", "ರಾತ್ರೆ", "night", "pm")

        // Match patterns like "7 ಗಂಟೆಗೆ", "7ಕ್ಕೆ", "7:30", "7 pm", "8 am"
        val timeRegex = Pattern.compile("(\\d{1,2})(:(\\d{2}))?\\s*(ಗಂಟೆಗೆ|ಕ್ಕೆ|pm|am|hours)?")
        val matcher = timeRegex.matcher(lower)

        if (matcher.find()) {
            val hourStr = matcher.group(1) ?: "7"
            val minStr = matcher.group(3) ?: "00"
            var hour = hourStr.toIntOrNull() ?: 7
            val min = minStr.toIntOrNull() ?: 0

            if (afternoon && hour in 1..11) hour += 12
            else if (evening && hour in 1..6) hour += 12
            else if (night && hour in 1..11) hour += 12
            else if (lower.contains("pm") && hour in 1..11) hour += 12

            val formatted = String.format("%02d:%02d", hour.coerceIn(0, 23), min.coerceIn(0, 59))
            return Pair(formatted, true)
        }

        // Broad day-period defaults if explicitly specified without exact hour
        if (morning) return Pair("07:00", true)
        if (afternoon) return Pair("13:00", true)
        if (evening) return Pair("18:00", true)
        if (night) return Pair("20:00", true)

        return Pair(null, false)
    }

    private fun extractCategoryAndEntities(lower: String): Triple<String, String?, String?> {
        var animal: String? = null
        var plot: String? = null

        // Cattle names
        if (containsAny(lower, "ಲಕ್ಷ್ಮಿ", "lakshmi")) animal = "ಲಕ್ಷ್ಮಿ"
        else if (containsAny(lower, "ಗೌರಿ", "gauri", "gowri")) animal = "ಗೌರಿ"
        else if (containsAny(lower, "ಕಾವೇರಿ", "kaveri")) animal = "ಕಾವೇರಿ"
        else if (containsAny(lower, "ತುಂಗಾ", "tunga")) animal = "ತುಂಗಾ"

        // Farm plot names
        if (containsAny(lower, "ಅಡಿಕೆ ತೋಟ", "arecanut", "ಅಡಿಕೆ")) plot = "ಅಡಿಕೆ ತೋಟ"
        else if (containsAny(lower, "ಭತ್ತದ ಗದ್ದೆ", "paddy", "ಗದ್ದೆ")) plot = "ಭತ್ತದ ಗದ್ದೆ"
        else if (containsAny(lower, "ತೆಂಗಿನ ತೋಟ", "coconut", "ತೆಂಗು")) plot = "ತೆಂಗಿನ ತೋಟ"

        val category = when {
            animal != null || containsAny(lower, "ಹಸು", "ಎಮ್ಮೆ", "ಕರು", "ಹಾಲು", "ಲಸಿಕೆ", "ಔಷಧಿ ಕೊಟ್ಟೆ", "ದನ", "ಕೊಟ್ಟಿಗೆ", "cow", "cattle") -> "ಹಸು"
            plot != null || containsAny(lower, "ತೋಟ", "ಗದ್ದೆ", "ನೀರು", "ಗೊಬ್ಬರ", "ಸಿಂಪಡಣೆ", "ನಾಟಿ", "ಕಟಾವು", "ಕೃಷಿ", "farm", "fertilizer", "crops", "ಬೆನ್ನಿ") -> "ಕೃಷಿ"
            containsAny(lower, "ಹುಟ್ಟುಹಬ್ಬ", "ಮದುವೆ", "ಜಾತ್ರೆ", "ಹಬ್ಬ", "ಕಾರ್ಯಕ್ರಮ", "birthday", "event", "temple", "ದೇವಾಲಯ", "ಪೂಜೆ", "ಶಾಲೆ") -> "ಕಾರ್ಯಕ್ರಮ"
            containsAny(lower, "ರೂಪಾಯಿ", "ಖರ್ಚು", "ಹಣ", "ಬಿಲ್", "ವಿದ್ಯುತ್", "electric", "bill", "money", "rupees") -> "ಖರ್ಚು"
            containsAny(lower, "ಅಮ್ಮ", "ಅಪ್ಪ", "ಮನೆ", "ಮಾರುಕಟ್ಟೆ", "ಆಸ್ಪತ್ರೆ", "family", "mother", "father", "market", "school") -> "ಕುಟುಂಬ"
            else -> "ಸಾಮಾನ್ಯ"
        }

        return Triple(category, animal, plot)
    }

    private fun generateCleanTitle(text: String, category: String, animal: String?, plot: String?): String {
        var clean = text.replace(Regex("(?i)(ನಾಳೆ|ಇವತ್ತು|ನಿನ್ನೆ|ಬೆಳಿಗ್ಗೆ|ಸಂಜೆ|ರಾತ್ರಿ|ಗಂಟೆಗೆ|ನೆನಪಿಸು|ಹೇಳು|remind me to|please)"), "")
            .trim()
        if (clean.length > 50) {
            clean = clean.take(47) + "..."
        }
        if (clean.isBlank()) {
            clean = when {
                animal != null -> "$animal ಆರೈಕೆ"
                plot != null -> "$plot ಕೆಲಸ"
                else -> text.take(30)
            }
        }
        return clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun parseSpecificDate(lower: String, currentYear: Int): LocalDate? {
        val monthsKn = mapOf(
            "ಜನವರಿ" to 1, "ಫೆಬ್ರವರಿ" to 2, "ಮಾರ್ಚ್" to 3, "ಏಪ್ರಿಲ್" to 4,
            "ಮೇ" to 5, "ಜೂನ್" to 6, "ಜುಲೈ" to 7, "ಆಗಸ್ಟ್" to 8,
            "ಸೆಪ್ಟೆಂಬರ್" to 9, "ಅಕ್ಟೋಬರ್" to 10, "ನವೆಂಬರ್" to 11, "ಡಿಸೆಂಬರ್" to 12,
            "january" to 1, "february" to 2, "march" to 3, "april" to 4,
            "may" to 5, "june" to 6, "july" to 7, "august" to 8,
            "september" to 9, "october" to 10, "november" to 11, "december" to 12
        )

        for ((mName, mNum) in monthsKn) {
            if (lower.contains(mName)) {
                val dayRegex = Pattern.compile("(\\d{1,2})")
                val matcher = dayRegex.matcher(lower)
                if (matcher.find()) {
                    val day = matcher.group(1)?.toIntOrNull() ?: 1
                    return try {
                        LocalDate.of(currentYear, mNum, day.coerceIn(1, 31))
                    } catch (e: Exception) { null }
                }
            }
        }
        return null
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { text.contains(it) }
    }

    /**
     * Gemini REST API call when connected online
     */
    private suspend fun callGeminiParser(speech: String, apiKey: String): ParsedVoiceIntent? {
        val todayStr = LocalDate.now().toString()
        val prompt = """
        You are DinaSiri, an Indian village life memory assistant.
        Analyze this user speech in Kannada/English/Tulu: "$speech"
        Today's date is: $todayStr.
        
        Return ONLY a JSON object with:
        {
          "intentType": "REMINDER" | "HISTORY" | "RECURRING_REMINDER" | "EVENT" | "FARM_RECORD" | "CATTLE_RECORD" | "NOTE",
          "title": "Short meaningful title in Kannada or user language",
          "targetDate": "YYYY-MM-DD",
          "targetTime": "HH:mm" or null if not mentioned,
          "isPastHistory": true if past action (e.g. gave medicine, watered field) else false,
          "category": "ಕೃಷಿ" | "ಹಸು" | "ಕುಟುಂಬ" | "ಕಾರ್ಯಕ್ರಮ" | "ಖರ್ಚು" | "ಸಾಮಾನ್ಯ",
          "recurrence": "NONE" | "DAILY" | "WEEKLY" | "SUNDAY" | "SATURDAY" | "MONTHLY" | "YEARLY" | "DAYS_15",
          "relatedAnimal": "name of cow if mentioned (e.g. ಲಕ್ಷ್ಮಿ) or null",
          "relatedPlot": "name of farm plot if mentioned or null",
          "requiresTimeClarification": true if it is a reminder without a specific time, else false
        }
        CRITICAL RULE: If user says they DID something in the past (e.g. "ಔಷಧಿ ಕೊಟ್ಟೆ", "ಹಾಕಿದೆ"), isPastHistory MUST be true and intentType MUST be HISTORY/FARM_RECORD/CATTLE_RECORD. NEVER schedule future reminder for past action!
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseText = response.body?.string() ?: return null
        val resJson = JSONObject(responseText)
        val candidateText = resJson.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        val parsedObj = JSONObject(candidateText)
        val intentTypeStr = parsedObj.optString("intentType", "NOTE")
        val intentType = try { IntentType.valueOf(intentTypeStr) } catch (e: Exception) { IntentType.NOTE }

        return ParsedVoiceIntent(
            rawText = speech,
            intentType = intentType,
            title = parsedObj.optString("title", speech.take(30)),
            description = speech,
            targetDate = parsedObj.optString("targetDate", todayStr),
            targetTime = if (parsedObj.isNull("targetTime")) null else parsedObj.optString("targetTime"),
            isPastHistory = parsedObj.optBoolean("isPastHistory", false),
            category = parsedObj.optString("category", "ಸಾಮಾನ್ಯ"),
            recurrence = parsedObj.optString("recurrence", "NONE"),
            relatedAnimal = if (parsedObj.isNull("relatedAnimal")) null else parsedObj.optString("relatedAnimal"),
            relatedPlot = if (parsedObj.isNull("relatedPlot")) null else parsedObj.optString("relatedPlot"),
            confidence = 0.98f,
            requiresTimeClarification = parsedObj.optBoolean("requiresTimeClarification", false)
        )
    }
}

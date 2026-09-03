package com.example.data.repository

import android.content.Context
import com.example.data.ai.VoiceParserEngine
import com.example.data.audio.VoiceRecorderManager
import com.example.data.cloud.CloudSyncManager
import com.example.data.db.AppDatabase
import com.example.data.models.AppLanguage
import com.example.data.models.CattleProfile
import com.example.data.models.FarmPlot
import com.example.data.models.IntentType
import com.example.data.models.MemoryRecord
import com.example.data.models.ParsedVoiceIntent
import com.example.data.models.ReminderItem
import com.example.data.models.UserProfile
import com.example.notifications.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class DinaSiriRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    val cloudSync = CloudSyncManager(context)
    val voiceParser = VoiceParserEngine()
    val audioRecorder = VoiceRecorderManager(context)
    val scheduler = ReminderScheduler(context)

    val currentUserFlow: Flow<UserProfile?> = db.userDao().getCurrentUserFlow()

    // ---------------- AUTH & PROFILE ----------------

    suspend fun getCurrentUser(): UserProfile? = db.userDao().getCurrentUser()

    suspend fun saveProfile(
        phoneNumber: String,
        name: String,
        dob: String,
        language: AppLanguage = AppLanguage.KANNADA
    ): UserProfile = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.replace("+91", "").trim()
        val userId = "user_$cleanPhone"
        val profile = UserProfile(
            userId = userId,
            phoneNumber = phoneNumber,
            name = name.trim(),
            dob = dob.trim(),
            language = language.name,
            updatedAt = System.currentTimeMillis()
        )
        db.userDao().insertUser(profile)

        // Seed initial default farm and cattle profiles if brand new
        val cattleList = db.cattleDao().getAllCattle(userId).firstOrNull()
        if (cattleList.isNullOrEmpty()) {
            db.cattleDao().insertAll(
                listOf(
                    CattleProfile(userId = userId, name = "ಲಕ್ಷ್ಮಿ", breed = "ಹಳ್ಳಿಕಾರ್ / Hallikar", notes = "ಹಾಲು ಕೊಡುವ ಹಸು"),
                    CattleProfile(userId = userId, name = "ಗೌರಿ", breed = "ಮಲೆನಾಡು ಗಿಡ್ಡ / Malnad Gidda", notes = "ಶಾಂತ ಸ್ವಭಾವದ ಹಸು")
                )
            )
        }

        val plotsList = db.farmDao().getAllPlots(userId).firstOrNull()
        if (plotsList.isNullOrEmpty()) {
            db.farmDao().insertAll(
                listOf(
                    FarmPlot(userId = userId, name = "ಅಡಿಕೆ ತೋಟ", area = "2 ಎಕರೆ", crop = "ಅಡಿಕೆ ಹಾಗೂ ಕಾಳುಮೆಣಸು"),
                    FarmPlot(userId = userId, name = "ಭತ್ತದ ಗದ್ದೆ", area = "1.5 ಎಕರೆ", crop = "ಭತ್ತ (ಜ್ಯೋತಿ ತಳಿ)")
                )
            )
        }

        triggerAutoBackup(profile)
        profile
    }

    suspend fun updateLanguage(language: AppLanguage) = withContext(Dispatchers.IO) {
        val user = getCurrentUser() ?: return@withContext
        val updated = user.copy(language = language.name, updatedAt = System.currentTimeMillis())
        db.userDao().updateUser(updated)
        triggerAutoBackup(updated)
    }

    suspend fun updateReminderPreferences(advance24h: Boolean, onTime: Boolean) = withContext(Dispatchers.IO) {
        val user = getCurrentUser() ?: return@withContext
        val updated = user.copy(
            notifyAdvance24h = advance24h,
            notifyOnTime = onTime,
            updatedAt = System.currentTimeMillis()
        )
        db.userDao().updateUser(updated)
        triggerAutoBackup(updated)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        db.userDao().clearUser()
    }

    suspend fun deleteAccount() = withContext(Dispatchers.IO) {
        val user = getCurrentUser()
        if (user != null) {
            cloudSync.deleteCloudAccount(user.phoneNumber)
            db.memoryDao().deleteAllForUser(user.userId)
            db.reminderDao().deleteAllForUser(user.userId)
            db.cattleDao().deleteAllForUser(user.userId)
            db.farmDao().deleteAllForUser(user.userId)
            db.userDao().clearUser()
        }
    }

    // ---------------- MEMORIES & RECORDS ----------------

    fun getAllRecordsFlow(userId: String): Flow<List<MemoryRecord>> = db.memoryDao().getAllRecords(userId)

    fun getRecordsByDateFlow(userId: String, date: String): Flow<List<MemoryRecord>> = db.memoryDao().getRecordsByDate(userId, date)

    fun searchRecordsFlow(userId: String, query: String): Flow<List<MemoryRecord>> = db.memoryDao().searchRecords(userId, query)

    fun getRecordsForAnimalFlow(userId: String, animal: String): Flow<List<MemoryRecord>> = db.memoryDao().getRecordsForAnimal(userId, animal)

    fun getRecordsForPlotFlow(userId: String, plot: String): Flow<List<MemoryRecord>> = db.memoryDao().getRecordsForPlot(userId, plot)

    suspend fun saveMemoryRecord(
        userId: String,
        text: String,
        category: String = "ಸಾಮಾನ್ಯ",
        date: String = LocalDate.now().toString(),
        time: String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
        voicePath: String? = null,
        photoUri: String? = null,
        relatedAnimal: String? = null,
        relatedPlot: String? = null,
        relatedEvent: String? = null
    ): MemoryRecord = withContext(Dispatchers.IO) {
        val record = MemoryRecord(
            userId = userId,
            text = text,
            category = category,
            date = date,
            time = time,
            voiceFilePath = voicePath,
            photoUri = photoUri,
            relatedAnimal = relatedAnimal,
            relatedPlot = relatedPlot,
            relatedEvent = relatedEvent,
            syncStatus = "SYNCED"
        )
        db.memoryDao().insertRecord(record)
        triggerAutoBackup(getCurrentUser())
        record
    }

    suspend fun deleteMemoryRecord(record: MemoryRecord) = withContext(Dispatchers.IO) {
        db.memoryDao().deleteRecordById(record.id, record.userId)
        triggerAutoBackup(getCurrentUser())
    }

    // ---------------- REMINDERS ----------------

    fun getActiveRemindersFlow(userId: String): Flow<List<ReminderItem>> = db.reminderDao().getAllActiveReminders(userId)

    fun getRemindersForDateFlow(userId: String, date: String): Flow<List<ReminderItem>> = db.reminderDao().getRemindersForDate(userId, date)

    fun getPendingRemindersForDateFlow(userId: String, date: String): Flow<List<ReminderItem>> = db.reminderDao().getPendingRemindersForDate(userId, date)

    suspend fun saveReminder(
        userId: String,
        title: String,
        description: String = "",
        category: String = "ಸಾಮಾನ್ಯ",
        targetDate: String,
        targetTime: String = "07:00",
        type: String = "ONCE",
        recurrence: String = "NONE",
        notifyAdvance24h: Boolean = true,
        notifyOnTime: Boolean = true,
        relatedAnimal: String? = null,
        relatedPlot: String? = null
    ): ReminderItem = withContext(Dispatchers.IO) {
        val timeToUse = targetTime.ifBlank { "07:00" }
        val dateObj = try { LocalDate.parse(targetDate) } catch (e: Exception) { LocalDate.now().plusDays(1) }
        val timeObj = try { LocalTime.parse(timeToUse) } catch (e: Exception) { LocalTime.of(7, 0) }
        val epochMillis = dateObj.atTime(timeObj).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val reminder = ReminderItem(
            userId = userId,
            title = title,
            description = description,
            category = category,
            targetDate = targetDate,
            targetTime = timeToUse,
            timestamp = epochMillis,
            type = type,
            recurrence = recurrence,
            notifyAdvance24h = notifyAdvance24h,
            notifyOnTime = notifyOnTime,
            relatedAnimal = relatedAnimal,
            relatedPlot = relatedPlot
        )
        db.reminderDao().insertReminder(reminder)
        scheduler.scheduleReminder(reminder)
        triggerAutoBackup(getCurrentUser())
        reminder
    }

    suspend fun completeReminder(reminder: ReminderItem) = withContext(Dispatchers.IO) {
        db.reminderDao().markCompleted(reminder.id)
        scheduler.cancelReminder(reminder.id)

        // Save into history as completed action memory
        saveMemoryRecord(
            userId = reminder.userId,
            text = "${reminder.title} (ಮುಗಿಸಿದ ಕೆಲಸ)",
            category = reminder.category,
            date = LocalDate.now().toString(),
            time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
            relatedAnimal = reminder.relatedAnimal,
            relatedPlot = reminder.relatedPlot
        )

        // If recurring, schedule next occurrence
        if (reminder.type == "RECURRING" || reminder.recurrence != "NONE") {
            val nextDate = calculateNextOccurrenceDate(reminder.targetDate, reminder.recurrence)
            saveReminder(
                userId = reminder.userId,
                title = reminder.title,
                description = reminder.description,
                category = reminder.category,
                targetDate = nextDate,
                targetTime = reminder.targetTime,
                type = "RECURRING",
                recurrence = reminder.recurrence,
                notifyAdvance24h = reminder.notifyAdvance24h,
                notifyOnTime = reminder.notifyOnTime,
                relatedAnimal = reminder.relatedAnimal,
                relatedPlot = reminder.relatedPlot
            )
        }

        triggerAutoBackup(getCurrentUser())
    }

    suspend fun deleteReminder(reminder: ReminderItem) = withContext(Dispatchers.IO) {
        scheduler.cancelReminder(reminder.id)
        db.reminderDao().deleteReminderById(reminder.id, reminder.userId)
        triggerAutoBackup(getCurrentUser())
    }

    private fun calculateNextOccurrenceDate(currentDateStr: String, recurrence: String): String {
        val curr = try { LocalDate.parse(currentDateStr) } catch (e: Exception) { LocalDate.now() }
        val next = when (recurrence) {
            "DAILY" -> curr.plusDays(1)
            "WEEKLY", "SUNDAY", "SATURDAY" -> curr.plusWeeks(1)
            "DAYS_15" -> curr.plusDays(15)
            "MONTHLY" -> curr.plusMonths(1)
            "YEARLY" -> curr.plusYears(1)
            else -> curr.plusWeeks(1)
        }
        return next.toString()
    }

    // ---------------- CATTLE & FARM ----------------

    fun getAllCattleFlow(userId: String): Flow<List<CattleProfile>> = db.cattleDao().getAllCattle(userId)

    fun getAllPlotsFlow(userId: String): Flow<List<FarmPlot>> = db.farmDao().getAllPlots(userId)

    suspend fun addCattle(userId: String, name: String, breed: String, notes: String, photoUri: String? = null) = withContext(Dispatchers.IO) {
        val cattle = CattleProfile(
            userId = userId,
            name = name.trim(),
            breed = breed.ifBlank { "ಹಳ್ಳಿಕಾರ್" },
            notes = notes,
            photoUri = photoUri
        )
        db.cattleDao().insertCattle(cattle)
        triggerAutoBackup(getCurrentUser())
    }

    suspend fun addPlot(userId: String, name: String, area: String, crop: String, notes: String, photoUri: String? = null) = withContext(Dispatchers.IO) {
        val plot = FarmPlot(
            userId = userId,
            name = name.trim(),
            area = area.ifBlank { "1 ಎಕರೆ" },
            crop = crop.ifBlank { "ಅಡಿಕೆ" },
            notes = notes,
            photoUri = photoUri
        )
        db.farmDao().insertPlot(plot)
        triggerAutoBackup(getCurrentUser())
    }

    // ---------------- PARSED INTENT COMMITTAL ----------------

    suspend fun commitParsedIntent(intent: ParsedVoiceIntent, userId: String): Boolean = withContext(Dispatchers.IO) {
        if (intent.isPastHistory || intent.intentType == IntentType.HISTORY ||
            intent.intentType == IntentType.FARM_RECORD || intent.intentType == IntentType.CATTLE_RECORD ||
            intent.intentType == IntentType.NOTE
        ) {
            // Save as Historical Record
            saveMemoryRecord(
                userId = userId,
                text = intent.title,
                category = intent.category,
                date = intent.targetDate,
                time = intent.targetTime ?: LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                voicePath = intent.audioFilePath,
                relatedAnimal = intent.relatedAnimal,
                relatedPlot = intent.relatedPlot
            )
            true
        } else {
            // Save as Future Reminder / Event
            val isRecurring = intent.intentType == IntentType.RECURRING_REMINDER || intent.recurrence != "NONE"
            saveReminder(
                userId = userId,
                title = intent.title,
                description = intent.description,
                category = intent.category,
                targetDate = intent.targetDate,
                targetTime = intent.targetTime ?: "07:00",
                type = if (isRecurring) "RECURRING" else "ONCE",
                recurrence = intent.recurrence,
                relatedAnimal = intent.relatedAnimal,
                relatedPlot = intent.relatedPlot
            )
            true
        }
    }

    private fun triggerAutoBackup(user: UserProfile?) {
        if (user == null || user.phoneNumber.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val records = db.memoryDao().getAllRecords(user.userId).firstOrNull() ?: emptyList()
                val reminders = db.reminderDao().getAllActiveReminders(user.userId).firstOrNull() ?: emptyList()
                val cattle = db.cattleDao().getAllCattle(user.userId).firstOrNull() ?: emptyList()
                val plots = db.farmDao().getAllPlots(user.userId).firstOrNull() ?: emptyList()
                cloudSync.performFullBackup(user, records, reminders, cattle, plots)
            } catch (e: Exception) {
                // Background quiet backup
            }
        }
    }
}

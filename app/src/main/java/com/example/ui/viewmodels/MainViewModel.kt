package com.example.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DinaSiriApp
import com.example.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface VoiceUiState {
    object Idle : VoiceUiState
    data class Listening(val liveText: String = "", val isAudioRecording: Boolean = false) : VoiceUiState
    object Parsing : VoiceUiState
    data class Confirmation(val parsedIntent: ParsedVoiceIntent) : VoiceUiState
    data class MissingTimePrompt(val parsedIntent: ParsedVoiceIntent) : VoiceUiState
    data class Error(val message: String) : VoiceUiState
}

data class DayTimeline(
    val date: LocalDate,
    val morningItems: List<Any> = emptyList(),   // ReminderItem or MemoryRecord
    val afternoonItems: List<Any> = emptyList(),
    val eveningItems: List<Any> = emptyList(),
    val festival: CalendarFestival? = null
)

data class RecapSummary(
    val farmRecordsCount: Int = 0,
    val cattleRecordsCount: Int = 0,
    val totalRemindersCount: Int = 0,
    val completedTasksCount: Int = 0,
    val totalEventsCount: Int = 0,
    val totalMemoriesCount: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as DinaSiriApp).repository

    val currentUser: StateFlow<UserProfile?> = repository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentLanguage: StateFlow<AppLanguage> = currentUser.map { user ->
        when (user?.language) {
            AppLanguage.ENGLISH.name -> AppLanguage.ENGLISH
            AppLanguage.TULU.name -> AppLanguage.TULU
            else -> AppLanguage.KANNADA
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.KANNADA)

    val today: LocalDate = LocalDate.now()

    // ---------------- LIVE DATA FLOWS ----------------

    val allRecords: StateFlow<List<MemoryRecord>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getAllRecordsFlow(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActiveReminders: StateFlow<List<ReminderItem>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getActiveRemindersFlow(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayReminders: StateFlow<List<ReminderItem>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getRemindersForDateFlow(user.userId, today.toString()) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayRecords: StateFlow<List<MemoryRecord>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getRecordsByDateFlow(user.userId, today.toString()) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tomorrowReminders: StateFlow<List<ReminderItem>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getRemindersForDateFlow(user.userId, today.plusDays(1).toString()) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cattleList: StateFlow<List<CattleProfile>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getAllCattleFlow(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val farmPlots: StateFlow<List<FarmPlot>> = currentUser.flatMapLatest { user ->
        if (user != null) repository.getAllPlotsFlow(user.userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------- VOICE & INTENT STATE ----------------

    private val _voiceState = MutableStateFlow<VoiceUiState>(VoiceUiState.Idle)
    val voiceState: StateFlow<VoiceUiState> = _voiceState.asStateFlow()

    private var currentRecordedAudioPath: String? = null

    // ---------------- CALENDAR STATE ----------------

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // ---------------- SEARCH STATE ----------------

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<MemoryRecord>> = combine(
        currentUser,
        _searchQuery
    ) { user, query ->
        if (user == null || query.isBlank()) emptyList()
        else {
            repository.searchRecordsFlow(user.userId, query).firstOrNull() ?: emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smartSearchAnswer: StateFlow<String?> = combine(
        _searchQuery,
        searchResults
    ) { query, results ->
        if (query.isBlank()) return@combine null
        val lower = query.lowercase()
        if (results.isEmpty()) {
            "ಈ ಮಾಹಿತಿ ದಾಖಲೆಗಳಲ್ಲಿ ಸಿಗಲಿಲ್ಲ."
        } else {
            val top = results.first()
            if (lower.contains("ಯಾವಾಗ") || lower.contains("when") || lower.contains("ಕೊನೆಯ ಬಾರಿ") || lower.contains("last time")) {
                val animalStr = if (top.relatedAnimal != null) "${top.relatedAnimal}ಗೆ " else ""
                "${animalStr}ಕೊನೆಯ ಬಾರಿ ದಾಖಲೆ ${top.date} ರಂದು ಇದೆ: ${top.text}"
            } else {
                "ದಾಖಲೆಯಲ್ಲಿ ಸಿಕ್ಕ ಮಾಹಿತಿ: ${top.text} (${top.date})"
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ---------------- RECAP COMPUTATION ----------------

    val weeklyRecap: StateFlow<RecapSummary> = allRecords.map { records ->
        val weekAgo = today.minusDays(7).toString()
        val inWeek = records.filter { it.date >= weekAgo }
        val completed = allActiveReminders.value.filter { it.isCompleted && (it.targetDate >= weekAgo) }.size
        RecapSummary(
            farmRecordsCount = inWeek.count { it.category == "ಕೃಷಿ" },
            cattleRecordsCount = inWeek.count { it.category == "ಹಸು" },
            totalRemindersCount = allActiveReminders.value.count { it.targetDate >= weekAgo },
            completedTasksCount = completed,
            totalEventsCount = inWeek.count { it.category == "ಕಾರ್ಯಕ್ರಮ" },
            totalMemoriesCount = inWeek.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecapSummary())

    val monthlyRecap: StateFlow<RecapSummary> = allRecords.map { records ->
        val monthPrefix = String.format("%d-%02d", today.year, today.monthValue)
        val inMonth = records.filter { it.date.startsWith(monthPrefix) }
        val completed = allActiveReminders.value.filter { it.isCompleted && it.targetDate.startsWith(monthPrefix) }.size
        RecapSummary(
            farmRecordsCount = inMonth.count { it.category == "ಕೃಷಿ" },
            cattleRecordsCount = inMonth.count { it.category == "ಹಸು" },
            totalRemindersCount = allActiveReminders.value.count { it.targetDate.startsWith(monthPrefix) },
            completedTasksCount = completed,
            totalEventsCount = inMonth.count { it.category == "ಕಾರ್ಯಕ್ರಮ" },
            totalMemoriesCount = inMonth.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecapSummary())

    val yearlyRecap: StateFlow<RecapSummary> = allRecords.map { records ->
        val yearPrefix = "${today.year}"
        val inYear = records.filter { it.date.startsWith(yearPrefix) }
        val completed = allActiveReminders.value.filter { it.isCompleted && it.targetDate.startsWith(yearPrefix) }.size
        RecapSummary(
            farmRecordsCount = inYear.count { it.category == "ಕೃಷಿ" },
            cattleRecordsCount = inYear.count { it.category == "ಹಸು" },
            totalRemindersCount = allActiveReminders.value.count { it.targetDate.startsWith(yearPrefix) },
            completedTasksCount = completed,
            totalEventsCount = inYear.count { it.category == "ಕಾರ್ಯಕ್ರಮ" },
            totalMemoriesCount = inYear.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecapSummary())

    // ---------------- NETWORK & SYNC STATUS ----------------

    fun isOnline(): Boolean {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(net) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ---------------- VOICE ACTIONS ----------------

    fun startListening() {
        val audioPath = repository.audioRecorder.startRecording()
        currentRecordedAudioPath = audioPath
        _voiceState.value = VoiceUiState.Listening(liveText = "", isAudioRecording = audioPath != null)
    }

    fun updateLiveSpeech(text: String) {
        if (_voiceState.value is VoiceUiState.Listening) {
            _voiceState.value = VoiceUiState.Listening(liveText = text, isAudioRecording = currentRecordedAudioPath != null)
        }
    }

    fun finishListeningAndParse(recognizedSpeech: String) {
        val audioPath = repository.audioRecorder.stopRecording() ?: currentRecordedAudioPath
        if (recognizedSpeech.isBlank()) {
            _voiceState.value = VoiceUiState.Error("ಮಾತನ್ನು ಸರಿಯಾಗಿ ಅರ್ಥಮಾಡಿಕೊಳ್ಳಲಾಗಲಿಲ್ಲ. ಮತ್ತೊಮ್ಮೆ ನಿಧಾನವಾಗಿ ಹೇಳಿ.")
            return
        }

        _voiceState.value = VoiceUiState.Parsing
        viewModelScope.launch {
            val parsed = repository.voiceParser.parseSpeech(recognizedSpeech, audioPath)
            if (parsed.requiresTimeClarification) {
                _voiceState.value = VoiceUiState.MissingTimePrompt(parsed)
            } else {
                _voiceState.value = VoiceUiState.Confirmation(parsed)
            }
        }
    }

    fun cancelVoiceInput() {
        repository.audioRecorder.cancelRecording()
        _voiceState.value = VoiceUiState.Idle
    }

    fun selectTimeForMissingPrompt(parsed: ParsedVoiceIntent, chosenTime: String?) {
        val updated = parsed.copy(
            targetTime = chosenTime,
            requiresTimeClarification = false
        )
        _voiceState.value = VoiceUiState.Confirmation(updated)
    }

    fun confirmAndSaveIntent(parsed: ParsedVoiceIntent) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.commitParsedIntent(parsed, user.userId)
            _voiceState.value = VoiceUiState.Idle
        }
    }

    // ---------------- REMINDER & RECORD ACTIONS ----------------

    fun completeReminder(reminder: ReminderItem) {
        viewModelScope.launch {
            repository.completeReminder(reminder)
        }
    }

    fun deleteReminder(reminder: ReminderItem) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun deleteRecord(record: MemoryRecord) {
        viewModelScope.launch {
            repository.deleteMemoryRecord(record)
        }
    }

    fun playVoiceMemo(filePath: String) {
        repository.audioRecorder.playAudio(filePath)
    }

    fun stopVoicePlayback() {
        repository.audioRecorder.stopPlayback()
    }

    val isPlayingAudio = repository.audioRecorder.isPlaying
    val currentlyPlayingPath = repository.audioRecorder.currentlyPlayingPath

    // ---------------- CALENDAR NAVIGATION ----------------

    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _selectedMonth.value = YearMonth.from(date)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun prevMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun goToToday() {
        _selectedDate.value = LocalDate.now()
        _selectedMonth.value = YearMonth.now()
    }

    // ---------------- CATTLE & FARM ACTIONS ----------------

    fun addCattle(name: String, breed: String, notes: String, photoUri: String? = null) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addCattle(user.userId, name, breed, notes, photoUri)
        }
    }

    fun addPlot(name: String, area: String, crop: String, notes: String, photoUri: String? = null) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addPlot(user.userId, name, area, crop, notes, photoUri)
        }
    }

    // ---------------- SETTINGS ----------------

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            repository.updateLanguage(language)
        }
    }

    fun updateReminderPreferences(advance24h: Boolean, onTime: Boolean) {
        viewModelScope.launch {
            repository.updateReminderPreferences(advance24h, onTime)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onLoggedOut()
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAccount()
            onDeleted()
        }
    }
}

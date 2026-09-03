package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DinaSiriApp
import com.example.data.models.AppLanguage
import com.example.data.models.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

sealed interface AuthUiState {
    object Welcome : AuthUiState
    object EnterPhone : AuthUiState
    data class RestoringAccount(val message: String) : AuthUiState
    data class EnterProfile(val phoneNumber: String) : AuthUiState
    data class Authenticated(val user: UserProfile) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as DinaSiriApp).repository

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Welcome)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val existing = repository.getCurrentUser()
            if (existing != null && existing.isAccountActive && existing.name.isNotBlank()) {
                _uiState.value = AuthUiState.Authenticated(existing)
            }
        }
    }

    fun onContinueFromWelcome() {
        _uiState.value = AuthUiState.EnterPhone
    }

    fun loginWithPhone(phoneNumber: String) {
        val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
        if (cleanPhone.length < 10) {
            _errorMessage.value = "ದಯವಿಟ್ಟು ಸರಿಯಾದ 10 ಅಂಕಿಯ ಮೊಬೈಲ್ ಸಂಖ್ಯೆ ನಮೂದಿಸಿ."
            return
        }

        val formattedPhone = "+91 $cleanPhone"
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Check if account data exists in cloud backup
                val hasCloud = repository.cloudSync.hasCloudAccount(formattedPhone)
                if (hasCloud) {
                    _uiState.value = AuthUiState.RestoringAccount("ನಿಮ್ಮ ಖಾತೆಯ ಮಾಹಿತಿಯನ್ನು ಪಡೆಯಲಾಗುತ್ತಿದೆ…")
                    val restored = repository.cloudSync.restoreAccountData(
                        phoneNumber = formattedPhone,
                        database = com.example.data.db.AppDatabase.getInstance(getApplication()),
                        onProgress = { msg ->
                            _uiState.value = AuthUiState.RestoringAccount(msg)
                        }
                    )
                    delay(600)
                    if (restored) {
                        val user = repository.getCurrentUser()
                        if (user != null && user.name.isNotBlank()) {
                            _isLoading.value = false
                            _uiState.value = AuthUiState.Authenticated(user)
                            return@launch
                        }
                    }
                }

                // Check if user profile already exists locally
                val localUser = repository.getCurrentUser()
                if (localUser != null && localUser.phoneNumber == formattedPhone && localUser.name.isNotBlank()) {
                    _isLoading.value = false
                    _uiState.value = AuthUiState.Authenticated(localUser)
                    return@launch
                }

                // New user - navigate directly to profile details setup
                _isLoading.value = false
                _uiState.value = AuthUiState.EnterProfile(formattedPhone)
            } catch (e: Exception) {
                _isLoading.value = false
                _uiState.value = AuthUiState.EnterProfile(formattedPhone)
            }
        }
    }

    fun submitProfile(
        phoneNumber: String,
        name: String,
        dobDay: String,
        dobMonth: String,
        dobYear: String,
        language: AppLanguage = AppLanguage.KANNADA
    ) {
        _errorMessage.value = null

        if (name.trim().isBlank()) {
            _errorMessage.value = "ದಯವಿಟ್ಟು ನಿಮ್ಮ ಹೆಸರನ್ನು ನಮೂದಿಸಿ."
            return
        }

        // Validate DOB strictly per requirements:
        // Future date invalid, impossible date invalid, age below 6 invalid, age above 120 invalid
        // For every invalid case show only: "ಹುಟ್ಟಿದ ದಿನಾಂಕ ಸರಿಯಾಗಿಲ್ಲ."
        val isValidDob = validateDob(dobDay, dobMonth, dobYear)
        if (!isValidDob) {
            _errorMessage.value = "ಹುಟ್ಟಿದ ದಿನಾಂಕ ಸರಿಯಾಗಿಲ್ಲ."
            return
        }

        val dobFormatted = String.format("%02d/%02d/%s", dobDay.toInt(), dobMonth.toInt(), dobYear.trim())

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val profile = repository.saveProfile(
                    phoneNumber = phoneNumber,
                    name = name,
                    dob = dobFormatted,
                    language = language
                )
                _isLoading.value = false
                _uiState.value = AuthUiState.Authenticated(profile)
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "ಮಾಹಿತಿ ಉಳಿಸಲು ಸಾಧ್ಯವಾಗಲಿಲ್ಲ. ದಯವಿಟ್ಟು ಪುನಃ ಪ್ರಯತ್ನಿಸಿ."
            }
        }
    }

    private fun validateDob(dayStr: String, monthStr: String, yearStr: String): Boolean {
        val d = dayStr.toIntOrNull() ?: return false
        val m = monthStr.toIntOrNull() ?: return false
        val y = yearStr.toIntOrNull() ?: return false

        val today = LocalDate.now()
        val parsedDate = try {
            LocalDate.of(y, m, d)
        } catch (e: Exception) {
            return false
        }

        if (parsedDate.isAfter(today)) return false

        val age = Period.between(parsedDate, today).years
        if (age < 6 || age > 120) return false

        return true
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

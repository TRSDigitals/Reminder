package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppLanguage
import com.example.data.models.StringsHelper
import com.example.ui.theme.DinaSiriTerracotta
import com.example.ui.theme.DinaSiriWarmCream
import com.example.ui.viewmodels.AuthViewModel
import java.time.LocalDate

@Composable
fun ProfileSetupScreen(
    authViewModel: AuthViewModel,
    phoneNumber: String,
    initialLanguage: AppLanguage
) {
    var name by remember { mutableStateOf("") }
    var dobDay by remember { mutableStateOf("") }
    var dobMonth by remember { mutableStateOf("") }
    var dobYear by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf(initialLanguage) }

    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val currentYear = remember {
        try {
            LocalDate.now().year
        } catch (e: Exception) {
            2026
        }
    }

    val dayItems = remember {
        (1..31).map { d ->
            val s = String.format("%02d", d)
            Pair(s, s)
        }
    }

    val monthItems = remember {
        listOf(
            Pair("01", "01 - ಜನವರಿ (Jan)"),
            Pair("02", "02 - ಫೆಬ್ರವರಿ (Feb)"),
            Pair("03", "03 - ಮಾರ್ಚ್ (Mar)"),
            Pair("04", "04 - ಏಪ್ರಿಲ್ (Apr)"),
            Pair("05", "05 - ಮೇ (May)"),
            Pair("06", "06 - ಜೂನ್ (Jun)"),
            Pair("07", "07 - ಜುಲೈ (Jul)"),
            Pair("08", "08 - ಆಗಸ್ಟ್ (Aug)"),
            Pair("09", "09 - ಸೆಪ್ಟೆಂಬರ್ (Sep)"),
            Pair("10", "10 - ಅಕ್ಟೋಬರ್ (Oct)"),
            Pair("11", "11 - ನವೆಂಬರ್ (Nov)"),
            Pair("12", "12 - ಡಿಸೆಂಬರ್ (Dec)")
        )
    }

    val yearItems = remember(currentYear) {
        (currentYear - 6 downTo currentYear - 110).map { y ->
            Pair(y.toString(), y.toString())
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = StringsHelper.get("profile_title", selectedLanguage),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ದಿನಸಿರಿ ನಿಮ್ಮನ್ನು ನೆನಪಿಟ್ಟುಕೊಳ್ಳಲು ಈ ವಿವರಗಳು ಸಾಕು.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Name Field
                    Text(
                        text = "ಹೆಸರು",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            authViewModel.clearError()
                        },
                        placeholder = { Text("ಉದಾ: ಕಮಲಮ್ಮ / ಶಂಕರ") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Date of Birth Dropdown Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ಹುಟ್ಟಿದ ದಿನಾಂಕ",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ಆಯ್ಕೆ ಮಾಡಿ (Select)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date (Day) Dropdown
                        DobDropdownField(
                            label = "ದಿನ",
                            selectedValue = dobDay,
                            displayValue = if (dobDay.isNotBlank()) dobDay else "ದಿನ",
                            items = dayItems,
                            onSelect = {
                                dobDay = it
                                authViewModel.clearError()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dob_day_input"),
                            dropdownTestTag = "dob_day_dropdown"
                        )

                        // Month Dropdown
                        val selectedMonthText = monthItems.find { it.first == dobMonth }?.second
                        val monthShortDisplay = if (selectedMonthText != null) {
                            val knMonth = selectedMonthText.substringAfter("- ").substringBefore(" (")
                            "$dobMonth $knMonth".trim()
                        } else "ತಿಂಗಳು"

                        DobDropdownField(
                            label = "ತಿಂಗಳು",
                            selectedValue = dobMonth,
                            displayValue = monthShortDisplay,
                            items = monthItems,
                            onSelect = {
                                dobMonth = it
                                authViewModel.clearError()
                            },
                            modifier = Modifier
                                .weight(1.35f)
                                .testTag("dob_month_input"),
                            dropdownTestTag = "dob_month_dropdown"
                        )

                        // Year Dropdown
                        DobDropdownField(
                            label = "ವರ್ಷ",
                            selectedValue = dobYear,
                            displayValue = if (dobYear.isNotBlank()) dobYear else "ವರ್ಷ",
                            items = yearItems,
                            onSelect = {
                                dobYear = it
                                authViewModel.clearError()
                            },
                            modifier = Modifier
                                .weight(1.15f)
                                .testTag("dob_year_input"),
                            dropdownTestTag = "dob_year_dropdown"
                        )
                    }

                    // Display selected DOB confirmation badge if selected
                    if (dobDay.isNotBlank() && dobMonth.isNotBlank() && dobYear.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DinaSiriWarmCream,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎂 ದಿನಾಂಕ: $dobDay/$dobMonth/$dobYear",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DinaSiriTerracotta
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Preferred Language Selection
                    Text(
                        text = "ಆದ್ಯತೆಯ ಭಾಷೆ",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val isSelected = selectedLanguage == lang
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedLanguage = lang },
                                label = {
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lang_chip_${lang.code}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error message (Strictly: "ಹುಟ್ಟಿದ ದಿನಾಂಕ ಸರಿಯಾಗಿಲ್ಲ.")
                    AnimatedVisibility(visible = errorMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            authViewModel.submitProfile(
                                phoneNumber = phoneNumber,
                                name = name,
                                dobDay = dobDay,
                                dobMonth = dobMonth,
                                dobYear = dobYear,
                                language = selectedLanguage
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = name.isNotBlank() && dobDay.isNotBlank() && dobMonth.isNotBlank() && dobYear.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = StringsHelper.get("save_profile", selectedLanguage),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DobDropdownField(
    label: String,
    selectedValue: String,
    displayValue: String = selectedValue,
    items: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    dropdownTestTag: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = if (expanded) 2.dp else 1.dp,
                color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 54.dp)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (displayValue.isNotBlank()) displayValue else label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (selectedValue.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedValue.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "ಆಯ್ಕೆ $label",
                    tint = if (selectedValue.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 280.dp)
                .widthIn(min = 140.dp)
                .testTag(dropdownTestTag)
        ) {
            items.forEach { (value, text) ->
                val isSelected = selectedValue == value
                DropdownMenuItem(
                    text = {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                    modifier = Modifier.testTag("${dropdownTestTag}_$value")
                )
            }
        }
    }
}

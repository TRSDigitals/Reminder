package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppLanguage
import com.example.data.models.FestivalData
import com.example.data.models.MemoryRecord
import com.example.data.models.ReminderItem
import com.example.data.models.StringsHelper
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allReminders by viewModel.allActiveReminders.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()

    val monthReminders = allReminders.filter { it.targetDate.startsWith(selectedMonth.toString()) }
    val monthRecords = allRecords.filter { it.date.startsWith(selectedMonth.toString()) }

    val selectedDateReminders = allReminders.filter { it.targetDate == selectedDate.toString() }
    val selectedDateRecords = allRecords.filter { it.date == selectedDate.toString() }
    val festivalOnSelectedDate = FestivalData.getFestivalForDate(selectedDate.toString())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "📅 ${StringsHelper.get("calendar_title", language)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.goToToday() }) {
                        Text(
                            text = "ಇಂದು (Today)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Selector Bar
            item {
                MonthHeader(
                    currentMonth = selectedMonth,
                    onPrev = { viewModel.prevMonth() },
                    onNext = { viewModel.nextMonth() }
                )
            }

            // Month Grid
            item {
                MonthCalendarGrid(
                    month = selectedMonth,
                    selectedDate = selectedDate,
                    today = viewModel.today,
                    reminders = monthReminders,
                    records = monthRecords,
                    onDateSelect = { date -> viewModel.selectDate(date) }
                )
            }

            // Festival Banner if any
            if (festivalOnSelectedDate != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DinaSiriWarmCream),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = festivalOnSelectedDate.icon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = festivalOnSelectedDate.nameKn,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DinaSiriTerracotta
                                    )
                                )
                                Text(
                                    text = festivalOnSelectedDate.descriptionKn,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Selected Day Header
            item {
                Text(
                    text = "🗓️ ${selectedDate.dayOfMonth} ${selectedDate.month.name} — ವಿವರ",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Timeline breakdown for selected day
            item {
                DayTimelineView(
                    date = selectedDate,
                    reminders = selectedDateReminders,
                    records = selectedDateRecords,
                    onCompleteReminder = { viewModel.completeReminder(it) },
                    onDeleteReminder = { viewModel.deleteReminder(it) }
                )
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
fun MonthHeader(
    currentMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val monthNamesKn = mapOf(
        1 to "ಜನವರಿ", 2 to "ಫೆಬ್ರವರಿ", 3 to "ಮಾರ್ಚ್", 4 to "ಏಪ್ರಿಲ್",
        5 to "ಮೇ", 6 to "ಜೂನ್", 7 to "ಜುಲೈ", 8 to "ಆಗಸ್ಟ್",
        9 to "ಸೆಪ್ಟೆಂಬರ್", 10 to "ಅಕ್ಟೋಬರ್", 11 to "ನವೆಂಬರ್", 12 to "ಡಿಸೆಂಬರ್"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }

            Text(
                text = "${monthNamesKn[currentMonth.monthValue]} ${currentMonth.year} (${currentMonth.month.name})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }
    }
}

@Composable
fun MonthCalendarGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    reminders: List<ReminderItem>,
    records: List<MemoryRecord>,
    onDateSelect: (LocalDate) -> Unit
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value % 7 // 0 for Sunday
    val weekdaysKn = listOf("ರವಿ", "ಸೋಮ", "ಮಂಗಳ", "ಬುಧ", "ಗುರು", "ಶುಕ್ರ", "ಶನಿ")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Weekday Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekdaysKn.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (dayName == "ರವಿ") DinaSiriTerracotta else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (c in 0..6) {
                        val cellIndex = r * 7 + c
                        val dayNumber = cellIndex - firstDayOfWeek + 1

                        if (dayNumber in 1..daysInMonth) {
                            val cellDate = month.atDay(dayNumber)
                            val isSelected = cellDate == selectedDate
                            val isToday = cellDate == today

                            val hasReminder = reminders.any { it.targetDate == cellDate.toString() && !it.isCompleted }
                            val hasRecord = records.any { it.date == cellDate.toString() }
                            val hasFestival = FestivalData.getFestivalForDate(cellDate.toString()) != null

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDateSelect(cellDate) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> Color.White
                                                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                                c == 0 -> DinaSiriTerracotta
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    )

                                    // Indicator dot row
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.padding(top = 1.dp)
                                    ) {
                                        if (hasFestival) {
                                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(DinaSiriAmber))
                                        }
                                        if (hasReminder) {
                                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else DinaSiriTerracotta))
                                        }
                                        if (hasRecord) {
                                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else DinaSiriFieldGreen))
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.size(42.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun DayTimelineView(
    date: LocalDate,
    reminders: List<ReminderItem>,
    records: List<MemoryRecord>,
    onCompleteReminder: (ReminderItem) -> Unit,
    onDeleteReminder: (ReminderItem) -> Unit
) {
    val morningReminders = reminders.filter { it.targetTime.isBlank() || it.targetTime < "12:00" }
    val afternoonReminders = reminders.filter { it.targetTime >= "12:00" && it.targetTime < "17:00" }
    val eveningReminders = reminders.filter { it.targetTime >= "17:00" }

    if (reminders.isEmpty() && records.isEmpty()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ಈ ದಿನಕ್ಕೆ ಯಾವುದೇ ಕೆಲಸಗಳು ನಿಗದಿಯಾಗಿಲ್ಲ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (morningReminders.isNotEmpty()) {
                TimelinePeriodSection("🌅 ಬೆಳಿಗ್ಗೆ (Morning)", morningReminders, onCompleteReminder, onDeleteReminder)
            }
            if (afternoonReminders.isNotEmpty()) {
                TimelinePeriodSection("🌞 ಮಧ್ಯಾಹ್ನ (Afternoon)", afternoonReminders, onCompleteReminder, onDeleteReminder)
            }
            if (eveningReminders.isNotEmpty()) {
                TimelinePeriodSection("🌙 ಸಂಜೆ & ರಾತ್ರಿ (Evening)", eveningReminders, onCompleteReminder, onDeleteReminder)
            }

            if (records.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📝 ಈ ದಿನದ ದಾಖಲೆಗಳು (Logged Memories)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        records.forEach { record ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = record.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelinePeriodSection(
    title: String,
    items: List<ReminderItem>,
    onCompleteReminder: (ReminderItem) -> Unit,
    onDeleteReminder: (ReminderItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DinaSiriTerracotta
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { reminder ->
                ReminderCardItem(
                    reminder = reminder,
                    onCompleteToggle = { onCompleteReminder(reminder) },
                    onDelete = { onDeleteReminder(reminder) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

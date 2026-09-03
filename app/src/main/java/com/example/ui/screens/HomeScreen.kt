package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppLanguage
import com.example.data.models.FestivalData
import com.example.data.models.ReminderItem
import com.example.data.models.StringsHelper
import com.example.ui.dialogs.VoiceInputDialog
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.VoiceUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToCalendar: () -> Unit,
    onNavigateToFarm: () -> Unit,
    onNavigateToCattle: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val todayReminders by viewModel.todayReminders.collectAsState()
    val tomorrowReminders by viewModel.tomorrowReminders.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()

    var showVoiceDialog by remember { mutableStateOf(false) }

    val todayStr = viewModel.today.toString()
    val festivalToday = FestivalData.getFestivalForDate(todayStr)

    // Voice Dialog visibility
    if (showVoiceDialog || voiceState !is VoiceUiState.Idle) {
        VoiceInputDialog(
            viewModel = viewModel,
            language = language,
            onDismiss = { showVoiceDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ದಿನಸಿರಿ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = currentUser?.name?.let { "ನಮಸ್ಕಾರ, $it" } ?: "ನಮಸ್ಕಾರ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                actions = {
                    // Cloud Sync status badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DinaSiriSageGreen.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Synced",
                                tint = DinaSiriFieldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ಸುರಕ್ಷಿತ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DinaSiriFieldGreen
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Date & Festival Header Card
            item {
                DateHeaderCard(today = viewModel.today, festival = festivalToday, language = language)
            }

            // Big Central Voice Action Card
            item {
                VoiceHeroCard(
                    language = language,
                    onSpeakClick = {
                        viewModel.startListening()
                        showVoiceDialog = true
                    }
                )
            }

            // Quick Category Shortcuts
            item {
                QuickNavRow(
                    onNavigateToCalendar = onNavigateToCalendar,
                    onNavigateToFarm = onNavigateToFarm,
                    onNavigateToCattle = onNavigateToCattle,
                    onNavigateToRecords = onNavigateToRecords
                )
            }

            // Today's Reminders Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔔 ${StringsHelper.get("today_title", language)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${todayReminders.count { !it.isCompleted }} ಬಾಕಿ",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (todayReminders.isEmpty()) {
                item {
                    EmptyTodayCard(
                        language = language,
                        onAddClick = {
                            viewModel.startListening()
                            showVoiceDialog = true
                        }
                    )
                }
            } else {
                items(todayReminders, key = { it.id }) { reminder ->
                    ReminderCardItem(
                        reminder = reminder,
                        onCompleteToggle = { viewModel.completeReminder(reminder) },
                        onDelete = { viewModel.deleteReminder(reminder) }
                    )
                }
            }

            // Tomorrow's Advance Alert Preview
            if (tomorrowReminders.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🌅 ${StringsHelper.get("tomorrow_title", language)} (ಮುನ್ಸೂಚನೆ)",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(tomorrowReminders, key = { it.id }) { reminder ->
                    ReminderCardItem(
                        reminder = reminder,
                        onCompleteToggle = { viewModel.completeReminder(reminder) },
                        onDelete = { viewModel.deleteReminder(reminder) },
                        isTomorrow = true
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun DateHeaderCard(
    today: LocalDate,
    festival: com.example.data.models.CalendarFestival?,
    language: AppLanguage
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${today.dayOfMonth} ${today.month.name.take(3)} ${today.year}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = today.dayOfWeek.name,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            if (festival != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DinaSiriAmber.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = festival.icon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = festival.nameKn,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DinaSiriTerracotta
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceHeroCard(
    language: AppLanguage,
    onSpeakClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DinaSiriWarmCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSpeakClick() }
            .testTag("voice_hero_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ಹೇಳಿದರೆ ಸಾಕು, ದಿನಸಿರಿ ನೆನಪಿಸುತ್ತದೆ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DinaSiriTerracotta
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Big Mic Button
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(DinaSiriTerracotta.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onSpeakClick,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(DinaSiriTerracotta)
                        .testTag("speak_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Speak to DinaSiri",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "🎙️ ಮಾತನಾಡಿ (Tap to Speak)",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "“ನಾಳೆ ಬೆಳಿಗ್ಗೆ 7ಕ್ಕೆ ಲಕ್ಷ್ಮಿಗೆ ಔಷಧಿ ಕೊಡಬೇಕು”",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun QuickNavRow(
    onNavigateToCalendar: () -> Unit,
    onNavigateToFarm: () -> Unit,
    onNavigateToCattle: () -> Unit,
    onNavigateToRecords: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickNavChip("📅 ಕ್ಯಾಲೆಂಡರ್", MaterialTheme.colorScheme.primaryContainer, onNavigateToCalendar, Modifier.weight(1f), "nav_calendar")
        QuickNavChip("🌾 ಕೃಷಿ", DinaSiriSageGreen.copy(alpha = 0.3f), onNavigateToFarm, Modifier.weight(1f), "nav_farm")
        QuickNavChip("🐄 ಹಸುಗಳು", DinaSiriAmber.copy(alpha = 0.25f), onNavigateToCattle, Modifier.weight(1f), "nav_cattle")
        QuickNavChip("📂 ದಾಖಲೆ", MaterialTheme.colorScheme.secondaryContainer, onNavigateToRecords, Modifier.weight(1f), "nav_records")
    }
}

@Composable
fun QuickNavChip(
    title: String,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ReminderCardItem(
    reminder: ReminderItem,
    onCompleteToggle: () -> Unit,
    onDelete: () -> Unit,
    isTomorrow: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (reminder.isCompleted) 0.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_item_${reminder.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = { onCompleteToggle() },
                modifier = Modifier.testTag("checkbox_${reminder.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (reminder.targetTime.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DinaSiriAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "⏰ ${reminder.targetTime}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DinaSiriTerracotta
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (reminder.relatedAnimal != null) {
                        Text(
                            text = "🐄 ${reminder.relatedAnimal}",
                            style = MaterialTheme.typography.labelSmall.copy(color = DinaSiriOlive, fontWeight = FontWeight.Bold)
                        )
                    }

                    if (reminder.relatedPlot != null) {
                        Text(
                            text = "🌾 ${reminder.relatedPlot}",
                            style = MaterialTheme.typography.labelSmall.copy(color = DinaSiriFieldGreen, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun EmptyTodayCard(
    language: AppLanguage,
    onAddClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✨ ಇಂದು ಯಾವುದೇ ಬಾಕಿ ಕೆಲಸಗಳಿಲ್ಲ",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ಹೊಸ ಕೆಲಸ ಅಥವಾ ನೆನಪನ್ನು ಸೇರಿಸಲು ಧ್ವನಿ ಬಳಸಿ.",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ಹೇಳಿ (Speak)")
            }
        }
    }
}

package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.AppLanguage
import com.example.data.models.IntentType
import com.example.data.models.ParsedVoiceIntent
import com.example.data.models.StringsHelper
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.VoiceUiState

@Composable
fun VoiceInputDialog(
    viewModel: MainViewModel,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val voiceState by viewModel.voiceState.collectAsState()

    Dialog(
        onDismissRequest = {
            viewModel.cancelVoiceInput()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp)
        ) {
            when (val state = voiceState) {
                is VoiceUiState.Listening -> {
                    ListeningView(
                        liveText = state.liveText,
                        language = language,
                        onStopAndParse = { text ->
                            viewModel.finishListeningAndParse(text)
                        },
                        onCancel = {
                            viewModel.cancelVoiceInput()
                            onDismiss()
                        }
                    )
                }

                is VoiceUiState.Parsing -> {
                    ParsingView(language = language)
                }

                is VoiceUiState.MissingTimePrompt -> {
                    MissingTimePromptView(
                        parsedIntent = state.parsedIntent,
                        language = language,
                        onTimeSelected = { chosenTime ->
                            viewModel.selectTimeForMissingPrompt(state.parsedIntent, chosenTime)
                        },
                        onCancel = {
                            viewModel.cancelVoiceInput()
                            onDismiss()
                        }
                    )
                }

                is VoiceUiState.Confirmation -> {
                    ConfirmationCardView(
                        parsedIntent = state.parsedIntent,
                        language = language,
                        onConfirm = {
                            viewModel.confirmAndSaveIntent(state.parsedIntent)
                            onDismiss()
                        },
                        onRetry = {
                            viewModel.startListening()
                        },
                        onCancel = {
                            viewModel.cancelVoiceInput()
                            onDismiss()
                        }
                    )
                }

                is VoiceUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        language = language,
                        onRetry = { viewModel.startListening() },
                        onCancel = {
                            viewModel.cancelVoiceInput()
                            onDismiss()
                        }
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun ListeningView(
    liveText: String,
    language: AppLanguage,
    onStopAndParse: (String) -> Unit,
    onCancel: () -> Unit
) {
    var manualTextInput by remember { mutableStateOf(liveText) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Sample voice presets for instant real demo & speech fallback
    val voicePresets = listOf(
        "ನಾಳೆ ಬೆಳಿಗ್ಗೆ 7ಕ್ಕೆ ಲಕ್ಷ್ಮಿಗೆ ಔಷಧಿ ಕೊಡಬೇಕು",
        "ಇವತ್ತು ಅಡಿಕೆ ತೋಟಕ್ಕೆ ನೀರು ಹಾಕಿದೆ",
        "ಸೆಪ್ಟೆಂಬರ್ 15 ಅಪ್ಪನ ಹುಟ್ಟುಹಬ್ಬ",
        "ಪ್ರತಿ ಭಾನುವಾರ ಬೆಳಿಗ್ಗೆ 8ಕ್ಕೆ ಕೊಟ್ಟಿಗೆ ಶುಚಿಗೊಳಿಸಬೇಕು",
        "ನಿನ್ನೆ 2000 ರೂಪಾಯಿ ಗೊಬ್ಬರ ಖರೀದಿಸಿದೆ"
    )

    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎙️ ${StringsHelper.get("voice_listening", language)}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pulsing Mic
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(DinaSiriTerracotta.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(DinaSiriTerracotta),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Microphone Active",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "ನಿಮ್ಮ ಧ್ವನಿಯನ್ನು ಆಲಿಸಲಾಗುತ್ತಿದೆ...",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Text display / editing
        OutlinedTextField(
            value = manualTextInput,
            onValueChange = { manualTextInput = it },
            placeholder = { Text("ಮಾತನಾಡಿ ಅಥವಾ ಟೈಪ್ ಮಾಡಿ...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("speech_text_input"),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Preset Badges for easy test
        Text(
            text = "ತ್ವರಿತ ಉದಾಹರಣೆಗಳು (Quick Samples):",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        voicePresets.forEach { preset ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        manualTextInput = preset
                        onStopAndParse(preset)
                    }
            ) {
                Text(
                    text = "🗣️ “$preset”",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val toParse = manualTextInput.ifBlank { "ನಾಳೆ ಬೆಳಿಗ್ಗೆ 7ಕ್ಕೆ ಲಕ್ಷ್ಮಿಗೆ ಔಷಧಿ ಕೊಡಬೇಕು" }
                onStopAndParse(toParse)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("done_speaking_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "✓ ಸರಿ, ಅರ್ಥಮಾಡಿಕೊಳ್ಳಿ",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun ParsingView(language: AppLanguage) {
    Column(
        modifier = Modifier
            .padding(36.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = StringsHelper.get("voice_processing", language),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "ದಿನಾಂಕ, ಸಮಯ ಹಾಗೂ ಕೆಲಸವನ್ನು ಗುರುತಿಸಲಾಗುತ್ತಿದೆ...",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun MissingTimePromptView(
    parsedIntent: ParsedVoiceIntent,
    language: AppLanguage,
    onTimeSelected: (String?) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(DinaSiriAmber.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = DinaSiriAmber,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = StringsHelper.get("time_clarification_title", language),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "“${parsedIntent.title}”",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = DinaSiriTerracotta,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        val options = listOf(
            Pair("🌅 ಬೆಳಿಗ್ಗೆ 07:00", "07:00"),
            Pair("🌞 ಮಧ್ಯಾಹ್ನ 01:00", "13:00"),
            Pair("🌙 ಸಂಜೆ 06:00", "18:00"),
            Pair("🌃 ರಾತ್ರಿ 08:30", "20:30"),
            Pair("⏰ ಸಮಯ ಬೇಡ (ದಿನ ಮಾತ್ರ)", null)
        )

        options.forEach { (label, timeVal) ->
            OutlinedButton(
                onClick = { onTimeSelected(timeVal) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onCancel) {
            Text("ರದ್ದು")
        }
    }
}

@Composable
private fun ConfirmationCardView(
    parsedIntent: ParsedVoiceIntent,
    language: AppLanguage,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✨ ದಿನಸಿರಿ ಗುರುತಿಸಿದ ವಿವರ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Type & Category Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val typeBadge = if (parsedIntent.isPastHistory) "📝 ಹಳೆಯ ದಾಖಲೆ" else "🔔 ಮುಂಬರುವ ಜ್ಞಾಪನೆ"
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (parsedIntent.isPastHistory) DinaSiriOlive.copy(alpha = 0.2f) else DinaSiriTerracotta.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = typeBadge,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (parsedIntent.isPastHistory) DinaSiriOlive else DinaSiriTerracotta
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = parsedIntent.category,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = parsedIntent.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Time rows
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ದಿನಾಂಕ: ${parsedIntent.targetDate}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                if (parsedIntent.targetTime != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ಸಮಯ: ${parsedIntent.targetTime}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                if (parsedIntent.relatedAnimal != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🐄 ಸಂಬಂಧಿತ ಹಸು: ${parsedIntent.relatedAnimal}",
                        style = MaterialTheme.typography.bodySmall.copy(color = DinaSiriOlive, fontWeight = FontWeight.Bold)
                    )
                }

                if (parsedIntent.relatedPlot != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🌾 ಸಂಬಂಧಿತ ತೋಟ: ${parsedIntent.relatedPlot}",
                        style = MaterialTheme.typography.bodySmall.copy(color = DinaSiriFieldGreen, fontWeight = FontWeight.Bold)
                    )
                }

                if (parsedIntent.recurrence != "NONE") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔄 ಪುನರಾವರ್ತನೆ: ${parsedIntent.recurrence}",
                        style = MaterialTheme.typography.bodySmall.copy(color = DinaSiriTerracotta, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm & Action Buttons
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_save_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = StringsHelper.get("confirm_save", language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier
                    .weight(1f)
                    .testTag("repeat_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(StringsHelper.get("repeat_voice", language))
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .testTag("cancel_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(StringsHelper.get("cancel", language))
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    language: AppLanguage,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ಮತ್ತೊಮ್ಮೆ ಪ್ರಯತ್ನಿಸಿ")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onCancel) {
            Text("ರದ್ದು")
        }
    }
}

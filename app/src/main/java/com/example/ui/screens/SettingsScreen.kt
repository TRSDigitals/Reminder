package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppLanguage
import com.example.data.models.StringsHelper
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showHelpExamplesDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("ಖಾತೆಯನ್ನು ಅಳಿಸುವುದೇ?", fontWeight = FontWeight.Bold) },
            text = {
                Text("ಖಾತೆಯನ್ನು ಅಳಿಸಿದರೆ ನಿಮ್ಮ ಎಲ್ಲಾ ನೆನಪುಗಳು, ಜ್ಞಾಪನೆಗಳು ಮತ್ತು ಹಸುಗಳ ವಿವರಗಳು ಶಾಶ್ವತವಾಗಿ ಅಳಿಸಲ್ಪಡುತ್ತವೆ. ಇದನ್ನು ಮರಳಿ ಪಡೆಯಲು ಸಾಧ್ಯವಿಲ್ಲ.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteAccount(onLoggedOut)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ಹೌದು, ಅಳಿಸಿ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("ರದ್ದು")
                }
            }
        )
    }

    if (showHelpExamplesDialog) {
        HelpExamplesDialog(onDismiss = { showHelpExamplesDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "⚙️ ${StringsHelper.get("settings_title", language)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = currentUser?.name ?: "ಬಳಕೆದಾರರು",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "📱 ${currentUser?.phoneNumber}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            if (currentUser?.dob?.isNotBlank() == true) {
                                Text(
                                    text = "🎂 ಹುಟ್ಟಿದ ದಿನಾಂಕ: ${currentUser?.dob}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            }

            // Cloud Backup Status Pill
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
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = DinaSiriFieldGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "☁️ ಕ್ಲೌಡ್ ಬ್ಯಾಕಪ್ ಸಕ್ರಿಯವಾಗಿದೆ",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DinaSiriFieldGreen
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ಆ್ಯಪ್ ಮರು-ಸ್ಥಾಪಿಸಿದರೂ ಇದೇ ಮೊಬೈಲ್ ಸಂಖ್ಯೆ ಮೂಲಕ ಎಲ್ಲಾ ಮಾಹಿತಿ ಮರಳಿ ಪಡೆಯಬಹುದು.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }

            // Language Selector Section
            item {
                Text(
                    text = "🌐 ${StringsHelper.get("language_title", language)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AppLanguage.values().forEach { lang ->
                            val isSelected = language == lang
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setLanguage(lang) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${lang.nativeName} (${lang.displayName})",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setLanguage(lang) }
                                )
                            }
                        }
                    }
                }
            }

            // Reminder Preferences Section
            item {
                Text(
                    text = "🔔 ಜ್ಞಾಪನೆ ಆದ್ಯತೆಗಳು (Notification Settings)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "24 ಗಂಟೆ ಮುಂಚಿತವಾಗಿ ಎಚ್ಚರಿಕೆ (Advance 24h Alert)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "ನಾಳೆಯ ಕೆಲಸವನ್ನು ಹಿಂದಿನ ದಿನವೇ ನೆನಪಿಸುತ್ತದೆ.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Switch(
                                checked = currentUser?.notifyAdvance24h ?: true,
                                onCheckedChange = {
                                    viewModel.updateReminderPreferences(
                                        advance24h = it,
                                        onTime = currentUser?.notifyOnTime ?: true
                                    )
                                }
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ಸರಿಯಾದ ಸಮಯಕ್ಕೆ ಎಚ್ಚರಿಕೆ (On-Time Alert)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "ನಿಗದಿತ ಸಮಯದಲ್ಲಿ ಧ್ವನಿ ಹಾಗೂ ಅಧಿಸೂಚನೆ ನೀಡುತ್ತದೆ.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Switch(
                                checked = currentUser?.notifyOnTime ?: true,
                                onCheckedChange = {
                                    viewModel.updateReminderPreferences(
                                        advance24h = currentUser?.notifyAdvance24h ?: true,
                                        onTime = it
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Help & Village Examples
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHelpExamplesDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "💡 ದಿನಸಿರಿ ಮಾತನಾಡುವ ಉದಾಹರಣೆಗಳು",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "ಅಮ್ಮನ ಕೆಲಸ, ಕೃಷಿ, ಹಸು, ಬಿಲ್ ಪಾವತಿ ಉದಾಹರಣೆಗಳು",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // App & Creator Info
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ದಿನಸಿರಿ (DinaSiri)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ರೂಪಕರ್ತರು: ತೇಜಸ್ ಆರ್ ಶೆಟ್ಟಿ (Created by Thejas R Shetty)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ಆವೃತ್ತಿ 1.0.0 • Voice-First Memory Assistant for Rural India",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Actions: Logout & Delete Account
            item {
                OutlinedButton(
                    onClick = { viewModel.logout(onLoggedOut) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logout_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ಹೊರಹೋಗಿ (Logout)")
                }
            }

            item {
                TextButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delete_account_button")
                ) {
                    Text(
                        text = "ನನ್ನ ಖಾತೆಯನ್ನು ಶಾಶ್ವತವಾಗಿ ಅಳಿಸಿ (Delete Account)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
fun HelpExamplesDialog(onDismiss: () -> Unit) {
    val examples = listOf(
        Pair("👩 ಅಮ್ಮನ ನೆನಪುಗಳು", "“ನಾಳೆ ಬೆಳಿಗ್ಗೆ 7ಕ್ಕೆ ಗುಳಿಗೆ ತೆಗೆದುಕೊಳ್ಳಬೇಕು.”"),
        Pair("🌾 ಕೃಷಿ ಕೆಲಸಗಳು", "“ಇವತ್ತು ಅಡಿಕೆ ತೋಟಕ್ಕೆ ನೀರು ಹಾಕಿದೆ.” / “ನಾಳೆ ಗೊಬ್ಬರ ತರಬೇಕು.”"),
        Pair("🐄 ಹಸು ಆರೈಕೆ", "“ಇವತ್ತು ಲಕ್ಷ್ಮಿಗೆ ಜಂತುಹುಳು ಔಷಧಿ ಕೊಟ್ಟೆ.” / “ನಾಳೆ ಗೌರಿಗೆ ಲಸಿಕೆ ಹಾಕಿಸಬೇಕು.”"),
        Pair("💡 ಬಿಲ್ ಹಾಗೂ ಶುಲ್ಕ", "“ಸೆಪ್ಟೆಂಬರ್ 10 ರೊಳಗೆ ಕರೆಂಟ್ ಬಿಲ್ ಕಟ್ಟಬೇಕು.”"),
        Pair("🎉 ಹಬ್ಬ ಹಾಗೂ ಕಾರ್ಯಕ್ರಮ", "“ಸೆಪ್ಟೆಂಬರ್ 15 ಅಪ್ಪನ ಹುಟ್ಟುಹಬ್ಬ.”"),
        Pair("🔄 ಪುನರಾವರ್ತನೆ", "“ಪ್ರತಿ ಭಾನುವಾರ ಬೆಳಿಗ್ಗೆ 8ಕ್ಕೆ ಕೊಟ್ಟಿಗೆ ತೊಳೆಯಬೇಕು.”")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("💡 ಮಾತನಾಡುವ ಉದಾಹರಣೆಗಳು", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                examples.forEach { (cat, text) ->
                    Column {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DinaSiriTerracotta
                            )
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("ಸರಿ") }
        }
    )
}

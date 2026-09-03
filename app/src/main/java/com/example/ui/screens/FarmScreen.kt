package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppLanguage
import com.example.data.models.FarmPlot
import com.example.data.models.MemoryRecord
import com.example.data.models.StringsHelper
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val plots by viewModel.farmPlots.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val farmRecords = allRecords.filter { it.category == "ಕೃಷಿ" || it.relatedPlot != null }

    var showAddPlotDialog by remember { mutableStateOf(false) }
    var selectedPlotFilter by remember { mutableStateOf<String?>(null) }

    val displayedRecords = if (selectedPlotFilter == null) {
        farmRecords
    } else {
        farmRecords.filter { it.relatedPlot == selectedPlotFilter }
    }

    if (showAddPlotDialog) {
        AddPlotDialog(
            onDismiss = { showAddPlotDialog = false },
            onAdd = { name, area, crop, notes ->
                viewModel.addPlot(name, area, crop, notes)
                showAddPlotDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🌾 ${StringsHelper.get("farm_title", language)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddPlotDialog = true },
                        modifier = Modifier.testTag("add_plot_button")
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Plot")
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

            // Plots Overview
            item {
                Text(
                    text = "🌱 ನಿಮ್ಮ ತೋಟ ಮತ್ತು ಗದ್ದೆಗಳು",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (selectedPlotFilter == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedPlotFilter = null }
                                .testTag("plot_filter_all")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ಎಲ್ಲಾ ತೋಟಗಳು",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedPlotFilter == null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    items(plots, key = { it.id }) { plot ->
                        val isSelected = selectedPlotFilter == plot.name
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DinaSiriFieldGreen else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .clickable { selectedPlotFilter = plot.name }
                                .testTag("plot_card_${plot.name}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🌾 ${plot.name}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${plot.area} • ${plot.crop}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Quick Farm Log Actions
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DinaSiriWarmCream),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚡ ತ್ವರಿತ ಕೃಷಿ ಕೆಲಸ ದಾಖಲಿಸಿ (Quick Farm Log)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DinaSiriTerracotta
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickFarmBadge("💧 ನೀರು", "ಇವತ್ತು ತೋಟಕ್ಕೆ ನೀರು ಹಾಯಿಸಿದೆ", viewModel, Modifier.weight(1f))
                            QuickFarmBadge("🧪 ಗೊಬ್ಬರ", "ಇವತ್ತು ಗೊಬ್ಬರ ಹಾಕಿದೆ", viewModel, Modifier.weight(1f))
                            QuickFarmBadge("🌿 ಸಿಂಪಡಣೆ", "ಇವತ್ತು ಕೀಟನಾಶಕ ಸಿಂಪಡಿಸಿದೆ", viewModel, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Farm History Timeline
            item {
                Text(
                    text = "📜 ಕೃಷಿ ದಾಖಲೆಗಳ ಇತಿಹಾಸ (${displayedRecords.size})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (displayedRecords.isEmpty()) {
                item {
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
                                text = "ಯಾವುದೇ ಕೃಷಿ ದಾಖಲೆಗಳಿಲ್ಲ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "“ಇವತ್ತು ಅಡಿಕೆ ತೋಟಕ್ಕೆ ನೀರು ಹಾಕಿದೆ” ಎಂದು ಧ್ವನಿಯಲ್ಲಿ ಹೇಳಿ.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            } else {
                items(displayedRecords, key = { it.id }) { record ->
                    FarmRecordCard(
                        record = record,
                        viewModel = viewModel,
                        onDelete = { viewModel.deleteRecord(record) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
fun QuickFarmBadge(
    label: String,
    speechText: String,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .height(40.dp)
            .clickable {
                viewModel.finishListeningAndParse(speechText)
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun FarmRecordCard(
    record: MemoryRecord,
    viewModel: MainViewModel,
    onDelete: () -> Unit
) {
    val isPlaying by viewModel.isPlayingAudio.collectAsState()
    val currentPath by viewModel.currentlyPlayingPath.collectAsState()
    val isThisPlaying = isPlaying && currentPath == record.voiceFilePath

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DinaSiriFieldGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = record.relatedPlot ?: "ಕೃಷಿ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DinaSiriFieldGreen
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${record.date} • ${record.time}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = record.text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (record.voiceFilePath != null) {
                    Button(
                        onClick = {
                            if (isThisPlaying) viewModel.stopVoicePlayback()
                            else viewModel.playVoiceMemo(record.voiceFilePath)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isThisPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isThisPlaying) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isThisPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Voice Memo",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isThisPlaying) "ನಿಲ್ಲಿಸಿ" else "ಧ್ವನಿ ಕೇಳಿ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun AddPlotDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, area: String, crop: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var crop by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🌾 ಹೊಸ ತೋಟ / ಗದ್ದೆ ಸೇರಿಸಿ", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ತೋಟದ ಹೆಸರು (ಉದಾ: ಅಡಿಕೆ ತೋಟ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("ವಿಸ್ತೀರ್ಣ (ಉದಾ: 2 ಎಕರೆ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = crop,
                    onValueChange = { crop = it },
                    label = { Text("ಬೆಳೆ (ಉದಾ: ಅಡಿಕೆ, ಕಾಳುಮೆಣಸು, ಭತ್ತ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAdd(name, area, crop, notes) },
                enabled = name.isNotBlank()
            ) {
                Text("ಸೇರಿಸಿ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ರದ್ದು") }
        }
    )
}

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
import com.example.data.models.CattleProfile
import com.example.data.models.MemoryRecord
import com.example.data.models.StringsHelper
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CattleScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val cattleList by viewModel.cattleList.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val cattleRecords = allRecords.filter { it.category == "ಹಸು" || it.relatedAnimal != null }

    var showAddCattleDialog by remember { mutableStateOf(false) }
    var selectedAnimalFilter by remember { mutableStateOf<String?>(null) }

    val displayedRecords = if (selectedAnimalFilter == null) {
        cattleRecords
    } else {
        cattleRecords.filter { it.relatedAnimal == selectedAnimalFilter }
    }

    if (showAddCattleDialog) {
        AddCattleDialog(
            onDismiss = { showAddCattleDialog = false },
            onAdd = { name, breed, notes ->
                viewModel.addCattle(name, breed, notes)
                showAddCattleDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🐄 ${StringsHelper.get("cattle_title", language)}",
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
                        onClick = { showAddCattleDialog = true },
                        modifier = Modifier.testTag("add_cattle_button")
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Cow")
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

            // Cattle List Overview
            item {
                Text(
                    text = "🐄 ನಿಮ್ಮ ಹಸುಗಳು ಮತ್ತು ಕರುಗಳು",
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
                            color = if (selectedAnimalFilter == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedAnimalFilter = null }
                                .testTag("animal_filter_all")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ಎಲ್ಲಾ ಹಸುಗಳು",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedAnimalFilter == null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    items(cattleList, key = { it.id }) { cattle ->
                        val isSelected = selectedAnimalFilter == cattle.name
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DinaSiriAmber else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .clickable { selectedAnimalFilter = cattle.name }
                                .testTag("cattle_card_${cattle.name}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🐄 ${cattle.name}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DinaSiriEarthyDark else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cattle.breed,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) DinaSiriEarthyDark.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Quick Cattle Actions
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DinaSiriWarmCream),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚡ ತ್ವರಿತ ಹಸು ಆರೈಕೆ ದಾಖಲಿಸಿ (Quick Cattle Log)",
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
                            val activeCow = selectedAnimalFilter ?: "ಲಕ್ಷ್ಮಿ"
                            QuickFarmBadge("💊 ಔಷಧಿ", "ಇವತ್ತು $activeCow ಗೆ ಔಷಧಿ ಕೊಟ್ಟೆ", viewModel, Modifier.weight(1f))
                            QuickFarmBadge("💉 ಲಸಿಕೆ", "ಇವತ್ತು $activeCow ಗೆ ಲಸಿಕೆ ಹಾಕಿಸಿದೆ", viewModel, Modifier.weight(1f))
                            QuickFarmBadge("🥛 ಹಾಲು", "ಇವತ್ತು $activeCow 6 ಲೀಟರ್ ಹಾಲು ಕೊಟ್ಟಿದೆ", viewModel, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Cattle Records Timeline
            item {
                Text(
                    text = "📜 ಆರೈಕೆ ಮತ್ತು ಚಿಕಿತ್ಸೆ ಇತಿಹಾಸ (${displayedRecords.size})",
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
                                text = "ಯಾವುದೇ ಹಸುವಿನ ದಾಖಲೆಗಳಿಲ್ಲ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "“ಇವತ್ತು ಲಕ್ಷ್ಮಿಗೆ ಜಂತುಹುಳು ಔಷಧಿ ಕೊಟ್ಟೆ” ಎಂದು ಧ್ವನಿಯಲ್ಲಿ ಹೇಳಿ.",
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
fun AddCattleDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, breed: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🐄 ಹೊಸ ಹಸು ಸೇರಿಸಿ", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ಹಸುವಿನ ಹೆಸರು (ಉದಾ: ಕಾವೇರಿ, ತುಂಗಾ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("ತಳಿ (ಉದಾ: ಹಳ್ಳಿಕಾರ್, ಮಲೆನಾಡು ಗಿಡ್ಡ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ಟಿಪ್ಪಣಿ (ಉದಾ: 4ನೇ ಈತು, ಹಾಲು ಕೊಡುವ ಹಸು)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onAdd(name, breed, notes) },
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

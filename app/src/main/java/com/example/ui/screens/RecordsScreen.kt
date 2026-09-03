package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppLanguage
import com.example.data.models.MemoryRecord
import com.example.data.models.StringsHelper
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val smartAnswer by viewModel.smartSearchAnswer.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val categories = listOf("ಎಲ್ಲಾ", "ಕೃಷಿ", "ಹಸು", "ಕುಟುಂಬ", "ಕಾರ್ಯಕ್ರಮ", "ಖರ್ಚು", "ಸಾಮಾನ್ಯ")

    val filteredRecords = allRecords.filter { record ->
        val matchesCategory = selectedCategoryFilter == null || selectedCategoryFilter == "ಎಲ್ಲಾ" || record.category == selectedCategoryFilter
        val matchesQuery = searchQuery.isBlank() ||
                record.text.contains(searchQuery, ignoreCase = true) ||
                (record.relatedAnimal?.contains(searchQuery, ignoreCase = true) == true) ||
                (record.relatedPlot?.contains(searchQuery, ignoreCase = true) == true)
        matchesCategory && matchesQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "📂 ${StringsHelper.get("records_title", language)}",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("ಹುಡುಕಿ... ಉದಾ: ಲಕ್ಷ್ಮಿ ಔಷಧಿ, ನೀರು, ಖರ್ಚು") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("records_search_input")
                )
            }

            // Smart Instant Answer Banner
            if (searchQuery.isNotBlank() && smartAnswer != null) {
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
                            Text(text = "💡", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = smartAnswer ?: "",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = DinaSiriTerracotta
                                )
                            )
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = (selectedCategoryFilter == null && cat == "ಎಲ್ಲಾ") || selectedCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = if (cat == "ಎಲ್ಲಾ") null else cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Records Count
            item {
                Text(
                    text = "ದಾಖಲೆಗಳು (${filteredRecords.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (filteredRecords.isEmpty()) {
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
                                text = "ಯಾವುದೇ ದಾಖಲೆಗಳು ಸಿಗಲಿಲ್ಲ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            } else {
                items(filteredRecords, key = { it.id }) { record ->
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

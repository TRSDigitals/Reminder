package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppLanguage
import com.example.data.models.StringsHelper
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.RecapSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecapScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val weeklyRecap by viewModel.weeklyRecap.collectAsState()
    val monthlyRecap by viewModel.monthlyRecap.collectAsState()
    val yearlyRecap by viewModel.yearlyRecap.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Week, 1: Month, 2: Year

    val currentRecap: RecapSummary = when (selectedTab) {
        1 -> monthlyRecap
        2 -> yearlyRecap
        else -> weeklyRecap
    }

    val tabTitle = when (selectedTab) {
        1 -> "ಈ ತಿಂಗಳ ಸಾರಾಂಶ (This Month)"
        2 -> "ಈ ವರ್ಷದ ಸಾರಾಂಶ (This Year)"
        else -> "ಈ ವಾರದ ಸಾರಾಂಶ (This Week)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "📊 ${StringsHelper.get("recap_title", language)}",
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

            // Tab Selector
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("🌿 ಈ ವಾರ", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("📅 ಈ ತಿಂಗಳು", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("🌾 ಈ ವರ್ಷ", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            // Summary Header Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DinaSiriWarmCream),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = tabTitle,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DinaSiriTerracotta
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ನಿಮ್ಮ ಕುಟುಂಬ, ಕೃಷಿ ಹಾಗೂ ಹಸುಗಳ ಆರೈಕೆಯ ಒಟ್ಟು ಲೆಕ್ಕಾಚಾರ.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // Stat Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("🌾 ಕೃಷಿ ಕೆಲಸಗಳು", "${currentRecap.farmRecordsCount}", DinaSiriFieldGreen, Modifier.weight(1f))
                    StatCard("🐄 ಹಸು ಆರೈಕೆ", "${currentRecap.cattleRecordsCount}", DinaSiriOlive, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("✓ ಪೂರ್ಣಗೊಂಡ ಕೆಲಸ", "${currentRecap.completedTasksCount}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    StatCard("🔔 ಒಟ್ಟು ಜ್ಞಾಪನೆಗಳು", "${currentRecap.totalRemindersCount}", DinaSiriTerracotta, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("🎉 ಕಾರ್ಯಕ್ರಮಗಳು", "${currentRecap.totalEventsCount}", DinaSiriAmber, Modifier.weight(1f))
                    StatCard("📝 ಒಟ್ಟು ನೆನಪುಗಳು", "${currentRecap.totalMemoriesCount}", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
        }
    }
}

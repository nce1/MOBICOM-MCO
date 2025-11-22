package com.mobdeve.s18.group5.bayanihanspots.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SecondarySage
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SurfaceOffWhite
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.TextCharcoal

// Simple placeholder data model for UI wiring
data class SpotUiModel(
    val id: String,
    val name: String,
    val category: String,
    val distance: String,
    val crowdLevel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var selectedCategory by remember { mutableStateOf("All") }

    val sampleSpots = remember {
        listOf(
            SpotUiModel("1", "Rooftop Garden Study Nook", "Study", "120 m", "Calm"),
            SpotUiModel("2", "Barangay Covered Court", "Play", "250 m", "Busy"),
            SpotUiModel("3", "Pocket Plaza - Wifi Corner", "Rest", "400 m", "Moderate")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover Spots", color = TextCharcoal) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryTeal,
                    titleContentColor = TextCharcoal
                )
            )
        },
        containerColor = SurfaceOffWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filters row (category chips)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf("All", "Study", "Rest", "Play", "Market")
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondarySage,
                            selectedLabelColor = TextCharcoal
                        )
                    )
                }
            }

            // Placeholder map area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = SecondarySage
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Map preview placeholder", color = TextCharcoal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spot list
            Text(
                text = "Nearby Micro-Spots",
                style = MaterialTheme.typography.titleMedium,
                color = TextCharcoal,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sampleSpots.filter { selectedCategory == "All" || it.category == selectedCategory }) { spot ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceOffWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(spot.name, style = MaterialTheme.typography.titleMedium, color = TextCharcoal)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${spot.category} • ${spot.distance}", style = MaterialTheme.typography.bodyMedium, color = TextCharcoal)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Crowd: ${spot.crowdLevel}", style = MaterialTheme.typography.bodySmall, color = TextCharcoal)
                        }
                    }
                }
            }
        }
    }
}
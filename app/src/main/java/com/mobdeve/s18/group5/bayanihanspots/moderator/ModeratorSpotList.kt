package com.mobdeve.s18.group5.bayanihanspots.moderator

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.mobdeve.s18.group5.bayanihanspots.data.spots.toSpot
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.AccentCoral
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal

enum class SpotFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorSpotList(
    onSpotClick: (String) -> Unit,
    onAddSpotClick: () -> Unit = {}
) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var allSpots by remember { mutableStateOf<List<Spot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf(SpotFilter.PENDING) }
    var showDeleteDialog by remember { mutableStateOf<Spot?>(null) }

    LaunchedEffect(Unit) {
        firestore.collection("spots")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    allSpots = snapshot.documents.mapNotNull { it.toSpot() }
                }
                isLoading = false
            }
    }

    val filteredSpots = when (selectedFilter) {
        SpotFilter.ALL -> allSpots
        SpotFilter.PENDING -> allSpots.filter { it.approvalStatus == "PENDING" }
        SpotFilter.APPROVED -> allSpots.filter { it.approvalStatus == "APPROVED" }
        SpotFilter.REJECTED -> allSpots.filter { it.approvalStatus == "REJECTED" }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Manage Spots",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredSpots.size} spots • ${allSpots.count { it.approvalStatus == "PENDING" }} pending",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    FloatingActionButton(
                        onClick = onAddSpotClick,
                        containerColor = PrimaryTeal,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Spot")
                    }
                }
            }
        }

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpotFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            when (filter) {
                                SpotFilter.ALL -> "${filter.label} (${allSpots.size})"
                                SpotFilter.PENDING -> "${filter.label} (${allSpots.count { it.approvalStatus == "PENDING" }})"
                                SpotFilter.APPROVED -> "${filter.label} (${allSpots.count { it.approvalStatus == "APPROVED" }})"
                                SpotFilter.REJECTED -> "${filter.label} (${allSpots.count { it.approvalStatus == "REJECTED" }})"
                            }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryTeal,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredSpots.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No ${selectedFilter.label.lowercase()} spots",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSpots) { spot ->
                    SpotRequestCard(
                        spot = spot,
                        onClick = { onSpotClick(spot.id) },
                        onDeleteClick = { showDeleteDialog = spot }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Spot") },
            text = { Text("Are you sure you want to permanently delete \"${showDeleteDialog!!.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val spotToDelete = showDeleteDialog!!
                        firestore.collection("spots").document(spotToDelete.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Spot deleted", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = AccentCoral)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SpotRequestCard(
    spot: Spot,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val isNewSubmission = spot.modificationType == "NEW"
    val isPending = spot.approvalStatus == "PENDING"

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                spot.approvalStatus == "REJECTED" -> Color(0xFFFFEBEE)
                spot.approvalStatus == "APPROVED" -> Color(0xFFE8F5E9)
                isNewSubmission -> MaterialTheme.colorScheme.surface
                else -> Color(0xFFFFF8E1)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Status badge
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = when (spot.approvalStatus) {
                                "APPROVED" -> Color(0xFFC8E6C9)
                                "REJECTED" -> Color(0xFFFFCDD2)
                                else -> if (isNewSubmission) Color(0xFFE3F2FD) else Color(0xFFFFE0B2)
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = spot.approvalStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (spot.approvalStatus) {
                                    "APPROVED" -> Color(0xFF2E7D32)
                                    "REJECTED" -> Color(0xFFC62828)
                                    else -> if (isNewSubmission) Color(0xFF1565C0) else Color(0xFFE65100)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        if (isPending) {
                            Surface(
                                color = if (isNewSubmission) Color(0xFFE3F2FD) else Color(0xFFFFE0B2),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = if (isNewSubmission) "NEW" else "EDITED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNewSubmission) Color(0xFF1565C0) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = spot.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AccentCoral
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Type info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = spot.type,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Creator info
            Text(
                text = "Submitted by: ${spot.userID.take(20)}${if (spot.userID.length > 20) "..." else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }
    }
}
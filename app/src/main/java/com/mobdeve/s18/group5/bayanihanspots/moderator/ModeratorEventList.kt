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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.AccentCoral
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal

enum class EventFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorEventList(
    onEventClick: (String) -> Unit,
    onAddEventClick: () -> Unit = {}
) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var allEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf(EventFilter.PENDING) }
    var showDeleteDialog by remember { mutableStateOf<Event?>(null) }

    LaunchedEffect(Unit) {
        firestore.collection("events")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    allEvents = snapshot.documents.mapNotNull { doc ->
                        try {
                            Event(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                schedule = doc.getString("schedule") ?: "",
                                locationLabel = doc.getString("locationLabel") ?: "",
                                description = doc.getString("description") ?: "",
                                host = doc.getString("host"),
                                maxVolunteers = doc.getLong("maxVolunteers")?.toInt(),
                                scheduleUtcMillis = doc.getLong("scheduleUtcMillis"),
                                creatorId = doc.getString("creatorId") ?: "",
                                approvalStatus = doc.getString("approvalStatus") ?: "PENDING",
                                modificationType = doc.getString("modificationType") ?: "NEW",
                                currentVolunteers = doc.getLong("currentVolunteers")?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                isLoading = false
            }
    }

    val filteredEvents = when (selectedFilter) {
        EventFilter.ALL -> allEvents
        EventFilter.PENDING -> allEvents.filter { it.approvalStatus == "PENDING" }
        EventFilter.APPROVED -> allEvents.filter { it.approvalStatus == "APPROVED" }
        EventFilter.REJECTED -> allEvents.filter { it.approvalStatus == "REJECTED" }
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
                            text = "Manage Events",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredEvents.size} events • ${allEvents.count { it.approvalStatus == "PENDING" }} pending",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    FloatingActionButton(
                        onClick = onAddEventClick,
                        containerColor = PrimaryTeal,
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Event")
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
            EventFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            when (filter) {
                                EventFilter.ALL -> "${filter.label} (${allEvents.size})"
                                EventFilter.PENDING -> "${filter.label} (${allEvents.count { it.approvalStatus == "PENDING" }})"
                                EventFilter.APPROVED -> "${filter.label} (${allEvents.count { it.approvalStatus == "APPROVED" }})"
                                EventFilter.REJECTED -> "${filter.label} (${allEvents.count { it.approvalStatus == "REJECTED" }})"
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
        } else if (filteredEvents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No ${selectedFilter.label.lowercase()} events",
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
                items(filteredEvents) { event ->
                    EventRequestCard(
                        event = event,
                        onClick = { onEventClick(event.id) },
                        onDeleteClick = { showDeleteDialog = event }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Event") },
            text = { Text("Are you sure you want to permanently delete \"${showDeleteDialog!!.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val eventToDelete = showDeleteDialog!!
                        firestore.collection("events").document(eventToDelete.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
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
fun EventRequestCard(
    event: Event,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val isNewSubmission = event.modificationType == "NEW"
    val isPending = event.approvalStatus == "PENDING"

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                event.approvalStatus == "REJECTED" -> Color(0xFFFFEBEE)
                event.approvalStatus == "APPROVED" -> Color(0xFFE8F5E9)
                isNewSubmission -> Color.White
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
                            color = when (event.approvalStatus) {
                                "APPROVED" -> Color(0xFFC8E6C9)
                                "REJECTED" -> Color(0xFFFFCDD2)
                                else -> if (isNewSubmission) Color(0xFFE3F2FD) else Color(0xFFFFE0B2)
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = event.approvalStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (event.approvalStatus) {
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
                        text = event.title,
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

            // Schedule info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.schedule,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Host info
            if (!event.host.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Host: ${event.host}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Creator info
            Text(
                text = "Submitted by: ${event.creatorId.take(20)}...",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }
    }
}


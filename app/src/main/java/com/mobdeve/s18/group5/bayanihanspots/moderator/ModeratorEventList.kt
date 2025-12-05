package com.mobdeve.s18.group5.bayanihanspots.moderator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event

@Composable
fun ModeratorEventList(onEventClick: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    var pendingEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        firestore.collection("events")
            .whereEqualTo("approvalStatus", "PENDING")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    pendingEvents = snapshot.documents.mapNotNull { doc ->
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

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Event Requests",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${pendingEvents.size} pending approval",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (pendingEvents.isEmpty()) {
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
                        text = "No pending event requests",
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
                items(pendingEvents) { event ->
                    EventRequestCard(
                        event = event,
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventRequestCard(event: Event, onClick: () -> Unit) {
    val isNewSubmission = event.modificationType == "NEW"

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNewSubmission) Color.White else Color(0xFFFFF8E1)
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
                    // Modification type badge
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
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


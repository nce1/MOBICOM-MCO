package com.mobdeve.s18.group5.bayanihanspots.moderator

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorEventScreen(
    eventId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    LaunchedEffect(eventId) {
        firestore.collection("events").document(eventId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    event = Event(
                        id = snapshot.id,
                        title = snapshot.getString("title") ?: "",
                        schedule = snapshot.getString("schedule") ?: "",
                        locationLabel = snapshot.getString("locationLabel") ?: "",
                        description = snapshot.getString("description") ?: "",
                        host = snapshot.getString("host"),
                        maxVolunteers = snapshot.getLong("maxVolunteers")?.toInt(),
                        scheduleUtcMillis = snapshot.getLong("scheduleUtcMillis"),
                        creatorId = snapshot.getString("creatorId") ?: "",
                        approvalStatus = snapshot.getString("approvalStatus") ?: "PENDING",
                        modificationType = snapshot.getString("modificationType") ?: "NEW",
                        currentVolunteers = snapshot.getLong("currentVolunteers")?.toInt() ?: 0
                    )
                }
                isLoading = false
            }
    }

    fun approveEvent() {
        isProcessing = true
        firestore.collection("events").document(eventId)
            .update("approvalStatus", "APPROVED")
            .addOnSuccessListener {
                Toast.makeText(context, "Event approved successfully!", Toast.LENGTH_SHORT).show()
                onBack()
            }
            .addOnFailureListener { e ->
                isProcessing = false
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun rejectEvent(reason: String) {
        isProcessing = true
        firestore.collection("events").document(eventId)
            .update(
                mapOf(
                    "approvalStatus" to "REJECTED",
                    "rejectionReason" to reason
                )
            )
            .addOnSuccessListener {
                Toast.makeText(context, "Event rejected", Toast.LENGTH_SHORT).show()
                onBack()
            }
            .addOnFailureListener { e ->
                isProcessing = false
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Event") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (event == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Event not found", color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Status Banner
                val isNewSubmission = event!!.modificationType == "NEW"
                Surface(
                    color = if (isNewSubmission) Color(0xFFE3F2FD) else Color(0xFFFFE0B2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isNewSubmission) Icons.Default.FiberNew else Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (isNewSubmission) Color(0xFF1565C0) else Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNewSubmission) "New Event Submission" else "Edited Event - Requires Re-approval",
                            fontWeight = FontWeight.Medium,
                            color = if (isNewSubmission) Color(0xFF1565C0) else Color(0xFFE65100)
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Event Title
                    Text(
                        text = event!!.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRow(
                                icon = Icons.Default.Schedule,
                                label = "Schedule",
                                value = event!!.schedule
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            DetailRow(
                                icon = Icons.Default.LocationOn,
                                label = "Location",
                                value = event!!.locationLabel
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            DetailRow(
                                icon = Icons.Default.Person,
                                label = "Host",
                                value = event!!.host ?: "Not specified"
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            DetailRow(
                                icon = Icons.Default.People,
                                label = "Max Volunteers",
                                value = event!!.maxVolunteers?.toString() ?: "Unlimited"
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            DetailRow(
                                icon = Icons.Default.AccountCircle,
                                label = "Creator ID",
                                value = event!!.creatorId
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = event!!.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRejectDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            enabled = !isProcessing
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reject")
                        }

                        Button(
                            onClick = { approveEvent() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            enabled = !isProcessing
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Approve")
                            }
                        }
                    }
                }
            }
        }
    }

    // Reject Dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Event") },
            text = {
                Column {
                    Text("Please provide a reason for rejection (optional):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Reason...") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRejectDialog = false
                        rejectEvent(rejectReason)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


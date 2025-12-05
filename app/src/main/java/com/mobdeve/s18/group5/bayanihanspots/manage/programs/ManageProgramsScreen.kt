package com.mobdeve.s18.group5.bayanihanspots.manage.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProgramsScreen(
    programs: List<Event>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onAddProgram: () -> Unit,
    onEditProgram: (String) -> Unit,
    onDeleteProgram: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<Event?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Programs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddProgram) {
                        Icon(Icons.Default.Add, contentDescription = "Add Program")
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
        } else if (programs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No programs yet", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onAddProgram) {
                        Text("Create Your First Program")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(programs) { program ->
                    ProgramCard(
                        program = program,
                        onEdit = { onEditProgram(program.id) },
                        onDelete = { showDeleteDialog = program }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { program ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Program") },
            text = { Text("Are you sure you want to delete \"${program.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProgram(program.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
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
fun ProgramCard(
    program: Event,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = program.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = program.schedule,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Status badge
                val statusColor = when (program.approvalStatus.uppercase()) {
                    "APPROVED" -> Color(0xFF4CAF50)
                    "PENDING" -> Color(0xFFFFA000)
                    "REJECTED" -> Color(0xFFF44336)
                    else -> Color.Gray
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = program.approvalStatus.uppercase(),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = program.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${program.currentVolunteers}/${program.maxVolunteers ?: "∞"} volunteers",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF1976D2))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}


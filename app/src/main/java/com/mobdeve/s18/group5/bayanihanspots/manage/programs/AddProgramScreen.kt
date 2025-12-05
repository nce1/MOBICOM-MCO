package com.mobdeve.s18.group5.bayanihanspots.manage.programs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProgramScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var maxVolunteers by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Program") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Program Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Host/Organization *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Location *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = maxVolunteers,
                onValueChange = { if (it.all { char -> char.isDigit() }) maxVolunteers = it },
                label = { Text("Max Volunteers (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Date Picker
            OutlinedTextField(
                value = selectedDate?.let { dateFormatter.format(it) } ?: "",
                onValueChange = {},
                label = { Text("Date *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                selectedDate = calendar.time
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )

            // Time Picker
            OutlinedTextField(
                value = selectedTime?.let {
                    calendar.set(Calendar.HOUR_OF_DAY, it.first)
                    calendar.set(Calendar.MINUTE, it.second)
                    timeFormatter.format(calendar.time)
                } ?: "",
                onValueChange = {},
                label = { Text("Time *") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                selectedTime = Pair(hourOfDay, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        ).show()
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Time")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        // Validation
                        if (title.isBlank() || description.isBlank() || host.isBlank() ||
                            locationName.isBlank() || selectedDate == null || selectedTime == null) {
                            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val userId = auth.currentUser?.uid ?: return@Button
                        val userEmail = auth.currentUser?.email ?: ""

                        isSaving = true

                        // Build schedule string
                        val scheduleCalendar = Calendar.getInstance().apply {
                            time = selectedDate!!
                            set(Calendar.HOUR_OF_DAY, selectedTime!!.first)
                            set(Calendar.MINUTE, selectedTime!!.second)
                        }
                        val scheduleFormat = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                        val scheduleString = scheduleFormat.format(scheduleCalendar.time)

                        val programData = hashMapOf(
                            "title" to title,
                            "description" to description,
                            "host" to host,
                            "locationLabel" to locationName,
                            "schedule" to scheduleString,
                            "scheduleUtcMillis" to scheduleCalendar.timeInMillis,
                            "maxVolunteers" to maxVolunteers.toIntOrNull(),
                            "currentVolunteers" to 0,
                            "creatorId" to userId,
                            "approvalStatus" to "PENDING",
                            "modificationType" to "NEW"
                        )

                        firestore.collection("events")
                            .add(programData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Program created successfully!", Toast.LENGTH_SHORT).show()
                                onSaveSuccess()
                            }
                            .addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create Program")
                    }
                }
            }
        }
    }
}


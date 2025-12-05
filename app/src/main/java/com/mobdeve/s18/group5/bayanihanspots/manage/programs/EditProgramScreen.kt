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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProgramScreen(
    program: Event,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    var title by remember { mutableStateOf(program.title) }
    var description by remember { mutableStateOf(program.description) }
    var host by remember { mutableStateOf(program.host ?: "") }
    var locationName by remember { mutableStateOf(program.locationLabel) }
    var maxVolunteers by remember { mutableStateOf(program.maxVolunteers?.toString() ?: "") }
    var selectedDate by remember { mutableStateOf<Date?>(program.scheduleUtcMillis?.let { Date(it) }) }
    var selectedTime by remember {
        mutableStateOf<Pair<Int, Int>?>(
            program.scheduleUtcMillis?.let {
                val cal = Calendar.getInstance().apply { timeInMillis = it }
                Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            }
        )
    }
    var isSaving by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Program") },
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
            // Status banner
            if (program.approvalStatus.uppercase() == "REJECTED") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "This program was rejected. You can edit and resubmit.",
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else if (program.approvalStatus.uppercase() == "APPROVED") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "This program is live. Changes will need re-approval.",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

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
                        val initCal = Calendar.getInstance().apply {
                            selectedDate?.let { time = it }
                        }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                selectedDate = calendar.time
                            },
                            initCal.get(Calendar.YEAR),
                            initCal.get(Calendar.MONTH),
                            initCal.get(Calendar.DAY_OF_MONTH)
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
                            selectedTime?.first ?: calendar.get(Calendar.HOUR_OF_DAY),
                            selectedTime?.second ?: calendar.get(Calendar.MINUTE),
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

                        isSaving = true

                        // Build schedule string
                        val scheduleCalendar = Calendar.getInstance().apply {
                            time = selectedDate!!
                            set(Calendar.HOUR_OF_DAY, selectedTime!!.first)
                            set(Calendar.MINUTE, selectedTime!!.second)
                        }
                        val scheduleFormat = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                        val scheduleString = scheduleFormat.format(scheduleCalendar.time)

                        val updates = hashMapOf<String, Any?>(
                            "title" to title,
                            "description" to description,
                            "host" to host,
                            "locationLabel" to locationName,
                            "schedule" to scheduleString,
                            "scheduleUtcMillis" to scheduleCalendar.timeInMillis,
                            "maxVolunteers" to maxVolunteers.toIntOrNull(),
                            "approvalStatus" to "PENDING", // Resubmit for approval
                            "modificationType" to "EDITED"
                        )

                        firestore.collection("events")
                            .document(program.id)
                            .update(updates)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Program updated successfully!", Toast.LENGTH_SHORT).show()
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
                        Text("Update Program")
                    }
                }
            }
        }
    }
}


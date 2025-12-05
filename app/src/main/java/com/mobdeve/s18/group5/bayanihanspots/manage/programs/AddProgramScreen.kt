package com.mobdeve.s18.group5.bayanihanspots.manage.programs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal
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
    var locationLabel by remember { mutableStateOf("") }
    var maxVolunteers by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }

    // Date and time
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Location
    var selectedLocation by remember { mutableStateOf(LatLng(14.5995, 120.9842)) }
    var showMapPicker by remember { mutableStateOf(false) }
    var useCoordinates by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Date picker
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Time picker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime = Pair(hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Program") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding(), start = 16.dp, end = 16.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Program Title *") },
                placeholder = { Text("e.g., Community Cleanup Drive") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                placeholder = { Text("Describe the program...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Host
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Organizer/Host") },
                placeholder = { Text("e.g., Barangay Hall, Your Organization") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Date and Time Section
            Text("Schedule", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date picker
                OutlinedTextField(
                    value = selectedDate?.let { dateFormatter.format(it) } ?: "",
                    onValueChange = {},
                    label = { Text("Date *") },
                    placeholder = { Text("Select date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick date")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { datePickerDialog.show() }
                )

                // Time picker
                OutlinedTextField(
                    value = selectedTime?.let {
                        String.format("%02d:%02d %s",
                            if (it.first > 12) it.first - 12 else if (it.first == 0) 12 else it.first,
                            it.second,
                            if (it.first >= 12) "PM" else "AM"
                        )
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Time *") },
                    placeholder = { Text("Select time") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { timePickerDialog.show() }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Pick time")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { timePickerDialog.show() }
                )
            }

            // Max Volunteers
            OutlinedTextField(
                value = maxVolunteers,
                onValueChange = { if (it.all { char -> char.isDigit() }) maxVolunteers = it },
                label = { Text("Max Volunteers (optional)") },
                placeholder = { Text("e.g., 50") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Location Section
            Text("Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            // Location label
            OutlinedTextField(
                value = locationLabel,
                onValueChange = { locationLabel = it },
                label = { Text("Location Name/Address *") },
                placeholder = { Text("e.g., Barangay Hall, Main Street") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Option to add coordinates
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = useCoordinates,
                    onCheckedChange = { useCoordinates = it }
                )
                Text("Add map coordinates (optional)")
            }

            if (useCoordinates) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .clickable { showMapPicker = true }
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = CameraPositionState(
                            CameraPosition.fromLatLngZoom(selectedLocation, 15f)
                        ),
                        uiSettings = MapUiSettings(zoomControlsEnabled = false),
                        googleMapOptionsFactory = {
                            com.google.android.gms.maps.GoogleMapOptions().liteMode(true)
                        }
                    ) {
                        Marker(state = MarkerState(position = selectedLocation))
                    }
                    Button(
                        onClick = { showMapPicker = true },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(Icons.Default.EditLocation, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set Location")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(50.dp),
                    enabled = !isSaving
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        // Validation
                        if (title.isBlank()) {
                            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (description.isBlank()) {
                            Toast.makeText(context, "Please enter a description", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedDate == null || selectedTime == null) {
                            Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (locationLabel.isBlank()) {
                            Toast.makeText(context, "Please enter a location", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSaving = true

                        // Build schedule string
                        val scheduleCalendar = Calendar.getInstance().apply {
                            time = selectedDate!!
                            set(Calendar.HOUR_OF_DAY, selectedTime!!.first)
                            set(Calendar.MINUTE, selectedTime!!.second)
                        }
                        val scheduleString = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                            .format(scheduleCalendar.time)

                        val programData = hashMapOf<String, Any?>(
                            "title" to title,
                            "description" to description,
                            "schedule" to scheduleString,
                            "scheduleUtcMillis" to scheduleCalendar.timeInMillis,
                            "locationLabel" to locationLabel,
                            "host" to host.ifBlank { null },
                            "maxVolunteers" to maxVolunteers.toIntOrNull(),
                            "currentVolunteers" to 0,
                            "creatorId" to (auth.currentUser?.uid ?: ""),
                            "approvalStatus" to "PENDING",
                            "modificationType" to "NEW"
                        )

                        if (useCoordinates) {
                            programData["coordinates"] = GeoPoint(
                                selectedLocation.latitude,
                                selectedLocation.longitude
                            )
                        }

                        firestore.collection("events").add(programData)
                            .addOnSuccessListener {
                                isSaving = false
                                Toast.makeText(context, "Program submitted for approval!", Toast.LENGTH_LONG).show()
                                onSaveSuccess()
                            }
                            .addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    if (isSaving) Text("Saving...") else Text("Submit Program")
                }
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }

    // Map Picker Dialog
    if (showMapPicker) {
        Dialog(
            onDismissRequest = { showMapPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Select Location") },
                        navigationIcon = {
                            IconButton(onClick = { showMapPicker = false }) {
                                Icon(Icons.Default.Close, null)
                            }
                        },
                        actions = {
                            TextButton(onClick = { showMapPicker = false }) {
                                Text("Done", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(selectedLocation, 17f)
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(zoomControlsEnabled = true)
                    )

                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                            .offset(y = (-24).dp)
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
                    ) {
                        Text(
                            "Move map to center the pin",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    LaunchedEffect(cameraPositionState.isMoving) {
                        if (!cameraPositionState.isMoving) {
                            selectedLocation = cameraPositionState.position.target
                        }
                    }

                    Button(
                        onClick = { showMapPicker = false },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
                    ) {
                        Text("Confirm Location")
                    }
                }
            }
        }
    }
}


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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal
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
    var locationLabel by remember { mutableStateOf(program.locationLabel) }
    var maxVolunteers by remember { mutableStateOf(program.maxVolunteers?.toString() ?: "") }
    var host by remember { mutableStateOf(program.host ?: "") }

    // Parse existing schedule
    var selectedDate by remember {
        mutableStateOf<Date?>(
            program.scheduleUtcMillis?.let { Date(it) }
        )
    }
    var selectedTime by remember {
        mutableStateOf<Pair<Int, Int>?>(
            program.scheduleUtcMillis?.let {
                val cal = Calendar.getInstance().apply { timeInMillis = it }
                Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            }
        )
    }

    // Location
    var selectedLocation by remember {
        mutableStateOf(
            program.coordinates?.let { LatLng(it.latitude, it.longitude) }
                ?: LatLng(14.5995, 120.9842)
        )
    }
    var showMapPicker by remember { mutableStateOf(false) }
    var useCoordinates by remember { mutableStateOf(program.coordinates != null) }

    var isSaving by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Date picker
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
        },
        selectedDate?.let {
            Calendar.getInstance().apply { time = it }.get(Calendar.YEAR)
        } ?: calendar.get(Calendar.YEAR),
        selectedDate?.let {
            Calendar.getInstance().apply { time = it }.get(Calendar.MONTH)
        } ?: calendar.get(Calendar.MONTH),
        selectedDate?.let {
            Calendar.getInstance().apply { time = it }.get(Calendar.DAY_OF_MONTH)
        } ?: calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Time picker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedTime = Pair(hourOfDay, minute)
        },
        selectedTime?.first ?: calendar.get(Calendar.HOUR_OF_DAY),
        selectedTime?.second ?: calendar.get(Calendar.MINUTE),
        false
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Program") },
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
            // Status banner
            if (program.approvalStatus == "APPROVED") {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ This program is live. Editing will require re-approval.",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else if (program.approvalStatus == "REJECTED") {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✗ This program was rejected. Edit and resubmit for approval.",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFFC62828),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Program Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Host
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Organizer/Host") },
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
                label = { Text("Max Volunteers") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Current signups info
            if (program.currentVolunteers > 0) {
                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ℹ️ ${program.currentVolunteers} volunteer(s) have already signed up",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFFF57C00),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Location Section
            Text("Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = locationLabel,
                onValueChange = { locationLabel = it },
                label = { Text("Location Name/Address *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = useCoordinates,
                    onCheckedChange = { useCoordinates = it }
                )
                Text("Include map coordinates")
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
                        Text("Update Location")
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

                        val updates = hashMapOf<String, Any?>(
                            "title" to title,
                            "description" to description,
                            "schedule" to scheduleString,
                            "scheduleUtcMillis" to scheduleCalendar.timeInMillis,
                            "locationLabel" to locationLabel,
                            "host" to host.ifBlank { null },
                            "maxVolunteers" to maxVolunteers.toIntOrNull(),
                            "approvalStatus" to "PENDING",
                            "modificationType" to "EDIT"
                        )

                        if (useCoordinates) {
                            updates["coordinates"] = GeoPoint(
                                selectedLocation.latitude,
                                selectedLocation.longitude
                            )
                        } else {
                            updates["coordinates"] = null
                        }

                        firestore.collection("events").document(program.id)
                            .update(updates.filterValues { it != null } as Map<String, Any>)
                            .addOnSuccessListener {
                                isSaving = false
                                Toast.makeText(context, "Changes submitted for approval!", Toast.LENGTH_LONG).show()
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
                    if (isSaving) Text("Saving...") else Text("Save Changes")
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
                        title = { Text("Update Location") },
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


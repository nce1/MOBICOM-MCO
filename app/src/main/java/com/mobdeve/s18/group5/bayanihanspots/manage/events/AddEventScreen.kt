package com.mobdeve.s18.group5.bayanihanspots.manage.events

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import com.mobdeve.s18.group5.bayanihanspots.manage.spots.searchAddress
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SecondarySage
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SurfaceOffWhite
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.TextCharcoal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    isModeratorMode: Boolean = false
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var isSaving by remember { mutableStateOf(false) }

    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var locationLabel by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var maxVolunteers by remember { mutableStateOf("") }

    // Date and Time
    var selectedDate by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Location - Default to Manila
    var selectedLocation by remember { mutableStateOf(LatLng(14.5995, 120.9842)) }
    var showMapPicker by remember { mutableStateOf(false) }
    var addressSearchText by remember { mutableStateOf("") }
    var isSearchingAddress by remember { mutableStateOf(false) }

    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    // Time picker state
    val timePickerState = rememberTimePickerState(
        initialHour = 9,
        initialMinute = 0,
        is24Hour = false
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add New Program",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryTeal
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = SurfaceOffWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SecondarySage.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Create a Volunteer Program",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextCharcoal
                        )
                        Text(
                            "Fill in the details to list your program",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextCharcoal.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Title
            Text(
                "Program Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextCharcoal
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Program Title *") },
                placeholder = { Text("e.g., Community Clean-up Drive") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    focusedLabelColor = PrimaryTeal
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                placeholder = { Text("Describe what volunteers will do...") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    focusedLabelColor = PrimaryTeal
                ),
                minLines = 3
            )

            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Host/Organization (optional)") },
                placeholder = { Text("e.g., Barangay San Antonio") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    focusedLabelColor = PrimaryTeal
                ),
                singleLine = true
            )

            // Volunteer Limit
            OutlinedTextField(
                value = maxVolunteers,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        maxVolunteers = newValue
                    }
                },
                label = { Text("Max Volunteers (optional)") },
                placeholder = { Text("Leave empty for unlimited") },
                leadingIcon = {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = PrimaryTeal)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    focusedLabelColor = PrimaryTeal
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            HorizontalDivider(color = SecondarySage)

            // Schedule Section
            Text(
                "Schedule *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextCharcoal
            )

            // Date & Time Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Date & Time",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextCharcoal.copy(alpha = 0.6f)
                        )
                        Text(
                            if (selectedDate.isNotEmpty()) selectedDate else "Tap to select date and time",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedDate.isEmpty()) TextCharcoal.copy(alpha = 0.5f) else TextCharcoal
                        )
                    }
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextCharcoal.copy(alpha = 0.4f)
                    )
                }
            }

            HorizontalDivider(color = SecondarySage)

            // Location Section
            Text(
                "Location *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextCharcoal
            )

            OutlinedTextField(
                value = locationLabel,
                onValueChange = { locationLabel = it },
                label = { Text("Location Name") },
                placeholder = { Text("e.g., Barangay Hall, San Antonio") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryTeal)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    focusedLabelColor = PrimaryTeal
                ),
                singleLine = true
            )

            // Map Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, SecondarySage, RoundedCornerShape(12.dp))
                    .clickable { showMapPicker = true }
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = CameraPositionState(
                        CameraPosition.fromLatLngZoom(selectedLocation, 15f)
                    ),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    googleMapOptionsFactory = { com.google.android.gms.maps.GoogleMapOptions().liteMode(true) },
                ) {
                    Marker(state = MarkerState(position = selectedLocation))
                }
                Button(
                    onClick = { showMapPicker = true },
                    modifier = Modifier.align(Alignment.Center),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Icon(Icons.Default.EditLocation, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set Location")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    // Validation
                    when {
                        title.isBlank() -> {
                            Toast.makeText(context, "Please enter a program title", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        description.isBlank() -> {
                            Toast.makeText(context, "Please enter a description", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        selectedDate.isBlank() -> {
                            Toast.makeText(context, "Please select a date and time", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        locationLabel.isBlank() -> {
                            Toast.makeText(context, "Please enter a location name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                    }

                    val currentUser = auth.currentUser
                    if (currentUser == null && !isModeratorMode) {
                        Toast.makeText(context, "Please login to create a program", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true

                    val eventData = hashMapOf(
                        "title" to title,
                        "description" to description,
                        "locationLabel" to locationLabel,
                        "host" to host.ifBlank { null },
                        "maxVolunteers" to maxVolunteers.toIntOrNull(),
                        "schedule" to selectedDate,
                        "scheduleUtcMillis" to selectedDateMillis,
                        "coordinates" to GeoPoint(selectedLocation.latitude, selectedLocation.longitude),
                        "creatorId" to (currentUser?.uid ?: "moderator"),
                        "approvalStatus" to if (isModeratorMode) "APPROVED" else "PENDING",
                        "modificationType" to "NEW",
                        "currentVolunteers" to 0,
                        "reminderOffsetsDays" to listOf(3, 1)
                    )

                    firestore.collection("events")
                        .add(eventData.filterValues { it != null })
                        .addOnSuccessListener {
                            isSaving = false
                            val message = if (isModeratorMode) "Event added!" else "Program submitted for review!"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            onSaveSuccess()
                        }
                        .addOnFailureListener { e ->
                            isSaving = false
                            Toast.makeText(context, "Failed to create: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isModeratorMode) "Add Event" else "Create Program", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDateMillis = millis
                            showDatePicker = false
                            showTimePicker = true
                        }
                    }
                ) {
                    Text("Next: Select Time", color = PrimaryTeal)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = PrimaryTeal,
                    todayDateBorderColor = PrimaryTeal
                )
            )
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        selectorColor = PrimaryTeal,
                        timeSelectorSelectedContainerColor = SecondarySage
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)

                        selectedDateMillis = calendar.timeInMillis
                        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                        selectedDate = formatter.format(calendar.time)
                        showTimePicker = false
                    }
                ) {
                    Text("Confirm", color = PrimaryTeal)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
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
                        title = { Text("Select Location", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { showMapPicker = false }) {
                                Icon(Icons.Default.Close, null, tint = Color.White)
                            }
                        },
                        actions = {
                            TextButton(onClick = { showMapPicker = false }) {
                                Text("Done", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryTeal)
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(selectedLocation, 17f)
                    }

                    LaunchedEffect(selectedLocation) {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(selectedLocation, 17f),
                            durationMs = 500
                        )
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(zoomControlsEnabled = true),
                        onMapClick = { latLng ->
                            selectedLocation = latLng
                        }
                    )

                    // Center marker
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = Color.Red,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center)
                            .offset(y = (-24).dp)
                    )

                    // Search bar
                    OutlinedTextField(
                        value = addressSearchText,
                        onValueChange = { addressSearchText = it },
                        placeholder = { Text("Search address...") },
                        leadingIcon = {
                            if (isSearchingAddress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = PrimaryTeal
                                )
                            } else {
                                Icon(Icons.Default.Search, null)
                            }
                        },
                        trailingIcon = {
                            if (addressSearchText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        isSearchingAddress = true
                                        searchAddress(context, addressSearchText) { result ->
                                            isSearchingAddress = false
                                            if (result != null) {
                                                selectedLocation = result
                                            } else {
                                                Toast.makeText(context, "Address not found", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.LocationOn, null, tint = PrimaryTeal)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Update location on camera move
                    LaunchedEffect(cameraPositionState.isMoving) {
                        if (!cameraPositionState.isMoving) {
                            selectedLocation = cameraPositionState.position.target
                        }
                    }
                }
            }
        }
    }
}


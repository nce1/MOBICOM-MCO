package com.mobdeve.s18.group5.bayanihanspots.manage.spots

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.maps.android.compose.*
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.mobdeve.s18.group5.bayanihanspots.data.spots.toSpot


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSpotScreen(spot: Spot, onBack: () -> Unit, onSaveSuccess: () -> Unit){
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var isSaving by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(spot.name) }
    var description by remember { mutableStateOf(spot.description) }
    var type by remember { mutableStateOf(spot.type) }
    var crowdLevel by remember { mutableStateOf(spot.crowdLevel) }

    var existingImages by remember { mutableStateOf(spot.imageList) }
    var newImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var selectedLocation by remember{
        mutableStateOf(
            if (spot.coordinates != null) LatLng(spot.coordinates.latitude, spot.coordinates.longitude)
            else LatLng(14.5995, 120.9842)
        )
    }
    var showMapPicker by remember { mutableStateOf(false) }
    var addressText by remember { mutableStateOf("") }
    var isSearchingAddress by remember { mutableStateOf(false) }

    val typeOptions = listOf("Study", "Rest", "Play", "Market", "Dining")
    val crowdOptions = listOf("Quiet", "Moderate", "Busy", "Packed")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> newImages = newImages + uris }
    )

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            newImages = newImages + tempCameraUri!!
        }
    }

    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to launch camera with permission check
    fun launchCamera() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                val uri = createImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Spot") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding(), start = 16.dp, end = 16.dp, bottom = 0.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
            Text("Photos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(100.dp).fillMaxWidth()) {
                item{
                    Box(modifier = Modifier.size(100.dp).border(1.dp, Color.Gray, RoundedCornerShape(8.dp)).clickable {
                        showImageSourceDialog = true
                    }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray)
                            Text("Add", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
                items(existingImages){ url ->
                    Box {
                        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)))
                        IconButton(onClick = { existingImages = existingImages - url }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.background(Color.Black.copy(0.5f), CircleShape))
                        }
                    }
                }
                items(newImages){ uri ->
                    Box {
                        AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)))
                        IconButton(onClick = { newImages = newImages - uri }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.background(Color.Black.copy(0.5f), CircleShape))
                        }
                    }
                }
            }
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Spot Name") }, modifier = Modifier.fillMaxWidth())
            CustomDropdown(label = "Type", options = typeOptions, selectedOption = type, onOptionSelected = { type = it })
            CustomDropdown(label = "Crowd Level", options = crowdOptions, selectedOption = crowdLevel, onOptionSelected = { crowdLevel = it })
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Text("Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            // Address search bar
            OutlinedTextField(
                value = addressText,
                onValueChange = { addressText = it },
                label = { Text("Search address") },
                placeholder = { Text("Enter address or place name") },
                leadingIcon = {
                    if (isSearchingAddress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                trailingIcon = {
                    if (addressText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                if (addressText.isNotBlank() && !isSearchingAddress) {
                                    isSearchingAddress = true
                                    searchAddress(context, addressText) { result ->
                                        isSearchingAddress = false
                                        if (result != null) {
                                            selectedLocation = result
                                            Toast.makeText(context, "Location found!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Could not find address", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            enabled = !isSearchingAddress
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Search location")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSearchingAddress
            )

            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)).clickable { showMapPicker = true }
            ){
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = CameraPositionState(CameraPosition.fromLatLngZoom(selectedLocation, 15f)),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    googleMapOptionsFactory = { com.google.android.gms.maps.GoogleMapOptions().liteMode(true) },
                ){
                    Marker(state = MarkerState(position = selectedLocation))
                }
                Button(onClick = { showMapPicker = true }, modifier = Modifier.align(Alignment.Center)) {
                    Icon(Icons.Default.EditLocation, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Location")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (name.isBlank() || description.isBlank()) return@Button
                    isSaving = true
                    uploadImagesToFirebase(storage, newImages) { uploadedUrls ->
                        val finalImageList = existingImages + uploadedUrls
                        val updates = hashMapOf<String, Any>(
                            "name" to name,
                            "description" to description,
                            "type" to type,
                            "crowdLevel" to crowdLevel,
                            "coordinates" to GeoPoint(selectedLocation.latitude, selectedLocation.longitude),
                            "imageList" to finalImageList,
                            "approvalStatus" to "PENDING",
                            "modificationType" to "EDIT"
                        )
                        firestore.collection("spots").document(spot.id) // Use passed ID
                            .update(updates)
                            .addOnSuccessListener{
                                isSaving = false
                                Toast.makeText(context, "Changes submitted!", Toast.LENGTH_SHORT).show()
                                onSaveSuccess()
                            }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ){
                if (isSaving) Text("Saving...") else Text("Save Changes")
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
    if (showMapPicker){
        var mapSearchQuery by remember { mutableStateOf("") }
        var isMapSearching by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showMapPicker = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Scaffold(topBar = { TopAppBar(title = { Text("Update Location") }, navigationIcon = { IconButton(onClick = { showMapPicker = false }) { Icon(Icons.Default.Close, null) } }, actions = { TextButton(onClick = { showMapPicker = false }) { Text("Done", fontWeight = FontWeight.Bold) } }) }) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(selectedLocation, 17f) }

                    // Update camera when selectedLocation changes from search
                    LaunchedEffect(selectedLocation) {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(selectedLocation, 17f),
                            durationMs = 500
                        )
                    }

                    GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, uiSettings = MapUiSettings(zoomControlsEnabled = true))
                    Icon(Icons.Default.LocationOn, null, tint = Color.Red, modifier = Modifier.size(48.dp).align(Alignment.Center).offset(y = (-24).dp))

                    // Search bar at the top
                    OutlinedTextField(
                        value = mapSearchQuery,
                        onValueChange = { mapSearchQuery = it },
                        placeholder = { Text("Search address...") },
                        leadingIcon = {
                            if (isMapSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        trailingIcon = {
                            if (mapSearchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        if (!isMapSearching) {
                                            isMapSearching = true
                                            searchAddress(context, mapSearchQuery) { result ->
                                                isMapSearching = false
                                                if (result != null) {
                                                    selectedLocation = result
                                                } else {
                                                    Toast.makeText(context, "Could not find address", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isMapSearching
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = "Go to location")
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        enabled = !isMapSearching
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 70.dp)
                    ){
                        Text(
                            "Move map to center the pin",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    LaunchedEffect(cameraPositionState.isMoving) { if (!cameraPositionState.isMoving) selectedLocation = cameraPositionState.position.target }
                    Button(onClick = { showMapPicker = false }, modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)) { Text("Confirm Location") }
                }
            }
        }
    }

    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Add Photo") },
            text = {
                Column {
                    Text("Choose how you want to add a photo")
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    showImageSourceDialog = false
                                    launchCamera()
                                }
                                .padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Camera", style = MaterialTheme.typography.labelMedium)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    showImageSourceDialog = false
                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                                .padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = "Gallery",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Gallery", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
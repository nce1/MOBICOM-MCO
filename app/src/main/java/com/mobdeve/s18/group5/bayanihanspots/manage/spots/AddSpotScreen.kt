package com.mobdeve.s18.group5.bayanihanspots.manage.spots

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.maps.android.compose.*
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpotScreen(onBack: () -> Unit, onSaveSuccess: () -> Unit){
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Study") }
    var crowdLevel by remember { mutableStateOf("Moderate") }

    // Default Location for Open Map
    var selectedLocation by remember { mutableStateOf(LatLng(14.5995, 120.9842)) }
    var addressText by remember { mutableStateOf("") }
    var showMapPicker by remember { mutableStateOf(false) }

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val typeOptions = listOf("Study", "Rest", "Play", "Market", "Dining")
    val crowdOptions = listOf("Quiet", "Moderate", "Busy", "Packed")

    val photoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia(), onResult = { uris -> selectedImages = selectedImages + uris })

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImages = selectedImages + tempCameraUri!!
        }
    }

    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Create image file and launch camera
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
                title = { Text("Add New Spot") },
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
                    Box(
                        modifier = Modifier.size(100.dp).border(1.dp, Color.Gray, RoundedCornerShape(8.dp)).clickable {
                            showImageSourceDialog = true
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally){
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray)
                            Text("Add", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
                items(selectedImages) { uri ->
                    Box{
                        AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)))
                        IconButton(onClick = { selectedImages = selectedImages - uri }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)){
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
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (addressText.isNotBlank()) {
                                try {
                                    val geocoder = Geocoder(context)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        geocoder.getFromLocationName(addressText, 1) { addresses ->
                                            if (addresses.isNotEmpty()) {
                                                val address = addresses[0]
                                                selectedLocation = LatLng(address.latitude, address.longitude)
                                            }
                                        }
                                    } else {
                                        @Suppress("DEPRECATION")
                                        val addresses = geocoder.getFromLocationName(addressText, 1)
                                        if (!addresses.isNullOrEmpty()) {
                                            val address = addresses[0]
                                            selectedLocation = LatLng(address.latitude, address.longitude)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not find address", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    .clickable { showMapPicker = true } // <--- Opens Dialog
            ){
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = CameraPositionState(CameraPosition.fromLatLngZoom(selectedLocation, 15f)),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    googleMapOptionsFactory = { com.google.android.gms.maps.GoogleMapOptions().liteMode(true) }
                ){
                    Marker(state = MarkerState(position = selectedLocation))
                }
                Button(
                    onClick = { showMapPicker = true },
                    modifier = Modifier.align(Alignment.Center)
                ){
                    Icon(Icons.Default.EditLocation, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set Location")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (name.isBlank() || description.isBlank()) {
                        Toast.makeText(context, "Please fill in name and description", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isUploading = true
                    uploadImagesToFirebase(storage, selectedImages) { imageUrls ->
                        val newSpot = Spot(
                            id = "",
                            name = name,
                            type = type,
                            crowdLevel = crowdLevel,
                            description = description,
                            status = "OPEN",
                            userID = auth.currentUser?.uid ?: "Anonymous",
                            coordinates = GeoPoint(selectedLocation.latitude, selectedLocation.longitude),
                            imageList = imageUrls
                        )
                        firestore.collection("spots").add(newSpot)
                            .addOnSuccessListener{
                                isUploading = false
                                Toast.makeText(context, "Spot submitted!", Toast.LENGTH_LONG).show()
                                onSaveSuccess()
                            }.addOnFailureListener{
                                isUploading = false
                                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ){
                if (isUploading) Text("Uploading...") else Text("Submit Spot")
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
    if (showMapPicker){
        var mapSearchQuery by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showMapPicker = false }, properties = DialogProperties(usePlatformDefaultWidth = false)){
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Pin Location") },
                        navigationIcon = { IconButton(onClick = { showMapPicker = false }) { Icon(Icons.Default.Close, null) } },
                        actions = { TextButton(onClick = { showMapPicker = false }) { Text("Done", fontWeight = FontWeight.Bold) } }
                    )
                }
            ){ padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()){
                    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(selectedLocation, 17f) }
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(zoomControlsEnabled = true)
                    )
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp).align(Alignment.Center).offset(y = (-24).dp)
                    )

                    // Search bar at the top
                    OutlinedTextField(
                        value = mapSearchQuery,
                        onValueChange = { mapSearchQuery = it },
                        placeholder = { Text("Search address...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (mapSearchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        try {
                                            val geocoder = Geocoder(context)
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                geocoder.getFromLocationName(mapSearchQuery, 1) { addresses ->
                                                    if (addresses.isNotEmpty()) {
                                                        val address = addresses[0]
                                                        selectedLocation = LatLng(address.latitude, address.longitude)
                                                    }
                                                }
                                            } else {
                                                @Suppress("DEPRECATION")
                                                val addresses = geocoder.getFromLocationName(mapSearchQuery, 1)
                                                if (!addresses.isNullOrEmpty()) {
                                                    val address = addresses[0]
                                                    selectedLocation = LatLng(address.latitude, address.longitude)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not find address", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
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
                        )
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
                    LaunchedEffect(cameraPositionState.isMoving){
                        if (!cameraPositionState.isMoving){
                            selectedLocation = cameraPositionState.position.target
                        }
                    }
                    Button(
                        onClick = { showMapPicker = false },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
                    ){ Text("Confirm Location") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdown(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit){
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }){
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onOptionSelected(option); expanded = false })
            }
        }
    }
}

fun uploadImagesToFirebase(storage: FirebaseStorage, uris: List<Uri>, onComplete: (List<String>) -> Unit){
    if (uris.isEmpty()) { onComplete(emptyList()); return }
    val uploadedUrls = mutableListOf<String>()
    var count = 0
    uris.forEach { uri ->
        val ref = storage.reference.child("spot_images/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).continueWithTask { it.result?.storage?.downloadUrl }
            .addOnSuccessListener { uploadedUrls.add(it.toString()); count++; if (count == uris.size) onComplete(uploadedUrls) }
            .addOnFailureListener { count++; if (count == uris.size) onComplete(uploadedUrls) }
    }
}

/**
 * Creates a temporary image URI for camera capture using FileProvider
 */
fun createImageUri(context: android.content.Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "SPOT_${timeStamp}.jpg"

    // Create the camera_images directory if it doesn't exist
    val cacheDir = File(context.cacheDir, "camera_images")
    if (!cacheDir.exists()) {
        cacheDir.mkdirs()
    }

    val imageFile = File(cacheDir, imageFileName)

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}


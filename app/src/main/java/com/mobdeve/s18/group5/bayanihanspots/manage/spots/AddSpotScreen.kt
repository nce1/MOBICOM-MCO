package com.mobdeve.s18.group5.bayanihanspots.manage.spots

import android.net.Uri
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.LocationOn
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
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.maps.android.compose.*
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
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
    var showMapPicker by remember { mutableStateOf(false) }

    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }

    val typeOptions = listOf("Study", "Rest", "Play", "Market", "Dining")
    val crowdOptions = listOf("Quiet", "Moderate", "Busy", "Packed")

    val photoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia(), onResult = { uris -> selectedImages = uris })

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
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
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
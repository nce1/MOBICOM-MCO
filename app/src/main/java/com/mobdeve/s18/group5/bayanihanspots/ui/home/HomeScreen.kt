package com.mobdeve.s18.group5.bayanihanspots.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.mobdeve.s18.group5.bayanihanspots.R
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.mobdeve.s18.group5.bayanihanspots.spots.SpotDetailsDialog
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SecondarySage
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SurfaceOffWhite
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.TextCharcoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    onNavigateToDetails: (Spot) -> Unit,
    isLoggedIn: Boolean
){
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedSpot by remember { mutableStateOf<Spot?>(null) }
    var showFullscreenMap by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            try {
                @SuppressLint("MissingPermission")
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.updateUserLocation(location)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    LaunchedEffect(Unit) {
        val hasFinePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFinePermission || hasCoarsePermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateUserLocation(location)
                }
            }
        } else{
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    Scaffold(
        containerColor = SurfaceOffWhite
    ) { innerPadding ->
        when (val state = uiState) {
            is SpotsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center){
                    CircularProgressIndicator(color = PrimaryTeal)
                }
            }
            is SpotsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center){
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is SpotsUiState.Success -> {
                val allSpots = state.events

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        //.padding(innerPadding)
                ) {
                    // Custom Header with Logo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        PrimaryTeal,
                                        PrimaryTeal.copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Logo
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo),
                                        contentDescription = "Bayanihan Spots Logo",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Bayanihan Spots",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Discover community spaces",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            // Explore Icon
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Explore,
                                    contentDescription = "Explore",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search spots...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = PrimaryTeal,
                            unfocusedBorderColor = SecondarySage
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("All", "Study", "Rest", "Play", "Market")
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondarySage,
                                    selectedLabelColor = TextCharcoal
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter spots by search and category
                    val filteredSpots = allSpots.filter { spot ->
                        val matchesCategory = selectedCategory == "All" || spot.type == selectedCategory
                        val matchesSearch = searchQuery.isEmpty() ||
                            spot.name.contains(searchQuery, ignoreCase = true) ||
                            spot.description.contains(searchQuery, ignoreCase = true) ||
                            spot.type.contains(searchQuery, ignoreCase = true)
                        matchesCategory && matchesSearch
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ){
                        HomeMap(
                            spots = filteredSpots,
                            userLocation = viewModel.userLocation,
                            onMarkerClick = { clickedSpot ->
                                selectedSpot = clickedSpot
                            }
                        )

                        // Fullscreen button
                        IconButton(
                            onClick = { showFullscreenMap = true },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = TextCharcoal
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Search Results (${filteredSpots.size})" else "Nearby Micro-Spots",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextCharcoal,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        items(filteredSpots){ spot ->
                            SpotCard(spot = spot,
                                onClick = {
                                    selectedSpot = spot
                                })
                        }

                        if (filteredSpots.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (searchQuery.isNotEmpty()) "No spots found for \"$searchQuery\"" else "No spots available",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                    if (selectedSpot != null) {
                        SpotDetailsDialog(
                            spot = selectedSpot!!,
                            onDismiss = { selectedSpot = null },
                            onExpand = {
                                val spotToPass = selectedSpot!!
                                selectedSpot = null
                                onNavigateToDetails(spotToPass)
                            }
                        )
                    }
                }

                // Fullscreen Map Dialog
                if (showFullscreenMap) {
                    val filteredSpots = allSpots.filter { spot ->
                        val matchesCategory = selectedCategory == "All" || spot.type == selectedCategory
                        val matchesSearch = searchQuery.isEmpty() ||
                            spot.name.contains(searchQuery, ignoreCase = true) ||
                            spot.description.contains(searchQuery, ignoreCase = true)
                        matchesCategory && matchesSearch
                    }

                    Dialog(
                        onDismissRequest = { showFullscreenMap = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            HomeMap(
                                spots = filteredSpots,
                                userLocation = viewModel.userLocation,
                                onMarkerClick = { clickedSpot ->
                                    selectedSpot = clickedSpot
                                    showFullscreenMap = false
                                }
                            )

                            // Close button
                            IconButton(
                                onClick = { showFullscreenMap = false },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextCharcoal
                                )
                            }

                            // Search bar and filters in fullscreen
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                                    .fillMaxWidth(0.85f)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search spots...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Category filter chips
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val categories = listOf("All", "Study", "Rest", "Play", "Market")
                                    categories.forEach { category ->
                                        FilterChip(
                                            selected = selectedCategory == category,
                                            onClick = { selectedCategory = category },
                                            label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color.White.copy(alpha = 0.9f),
                                                selectedContainerColor = SecondarySage,
                                                selectedLabelColor = TextCharcoal
                                            ),
                                            modifier = Modifier.height(32.dp)
                                        )
                                    }
                                }
                            }

                            // Spots count indicator
                            Surface(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 24.dp)
                            ) {
                                Text(
                                    text = "${filteredSpots.size} spots found",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextCharcoal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotCard(spot: Spot, onClick: () -> Unit){
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)){
            Text(spot.name, style = MaterialTheme.typography.titleMedium, color = TextCharcoal)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = spot.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextCharcoal,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${spot.type} ${spot.distanceString}",
                style = MaterialTheme.typography.labelMedium,
                color = TextCharcoal.copy(alpha = 0.8f),
                maxLines = 1
            )
            Text(
                text = "Crowd: ${spot.crowdLevel}",
                style = MaterialTheme.typography.labelMedium,
                color = if (spot.crowdLevel == "Busy") Color.Red.copy(alpha = 0.8f) else TextCharcoal.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

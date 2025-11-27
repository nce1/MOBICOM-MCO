package com.mobdeve.s18.group5.bayanihanspots.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.mobdeve.s18.group5.bayanihanspots.spots.SpotDetailsDialog
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SecondarySage
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SurfaceOffWhite
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.TextCharcoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory()), onNavigateToDetails: (String) -> Unit, isLoggedIn: Boolean){
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedSpot by remember { mutableStateOf<Spot?>(null) }

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
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover Spots", color = TextCharcoal) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryTeal,
                    titleContentColor = TextCharcoal
                )
            )
        },
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
                val displayedSpots = if (selectedCategory == "All"){
                    allSpots
                } else{
                    allSpots.filter { it.type == selectedCategory }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ){
                        HomeMap(
                            spots = displayedSpots,
                            userLocation = viewModel.userLocation,
                            onMarkerClick = { clickedSpot ->
                                selectedSpot = clickedSpot
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nearby Micro-Spots",
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
                        items(displayedSpots){ spot ->
                            SpotCard(spot = spot,
                                onClick = {
                                    selectedSpot = spot
                                })
                        }
                    }
                    if (selectedSpot != null) {
                        SpotDetailsDialog(
                            spot = selectedSpot!!,
                            onDismiss = { selectedSpot = null },
                            onExpand = {
                                selectedSpot?.let { spot ->
                                    selectedSpot = null
                                    onNavigateToDetails(spot.id)
                                }
                            }
                        )
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

package com.mobdeve.s18.group5.bayanihanspots.ui.home

import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.rememberUpdatedState
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot

@Composable
fun HomeMap(
    spots: List<Spot>,
    userLocation: Location?,
    focusedSpot: Spot? = null,
    onMarkerClick: (Spot) -> Unit,
    onFocusComplete: () -> Unit = {}
){
    val lat = userLocation?.latitude ?: 14.564840351545678
    val long = userLocation?.longitude ?: 120.99288364166271

    val defaultLocation = LatLng(lat, long)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    // Use rememberUpdatedState to capture the latest callback
    val currentOnFocusComplete = rememberUpdatedState(onFocusComplete)

    // Focus on a specific spot when requested - this takes priority
    LaunchedEffect(focusedSpot?.id) {  // Use spot.id as key instead of the entire object
        if (focusedSpot != null && focusedSpot.coordinates != null) {
            Log.d("MAP", "Focusing on spot: ${focusedSpot.name} at ${focusedSpot.coordinates.latitude}, ${focusedSpot.coordinates.longitude}")
            val spotLocation = LatLng(
                focusedSpot.coordinates.latitude,
                focusedSpot.coordinates.longitude
            )
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(spotLocation, 17f),
                durationMs = 1000
            )
            // Wait for animation to complete before clearing
            kotlinx.coroutines.delay(1100)
            currentOnFocusComplete.value()
        }
    }

    // Only animate to user location on initial load when no spot is focused
    LaunchedEffect(userLocation) {
        // Don't override if we're focusing on a specific spot
        if (userLocation != null && focusedSpot == null) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(userLocation.latitude, userLocation.longitude),
                    15f
                ),
                durationMs = 1000
            )
        }
        Log.d("MAP", "User location: $userLocation")
    }

    GoogleMap(modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp)),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = userLocation != null
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = true
        )
    ) {
        spots.forEach { spot ->
            val spotLat = spot.coordinates?.latitude ?: 0.0
            val spotLng = spot.coordinates?.longitude ?: 0.0

            Marker(
                state = MarkerState(position = LatLng(spotLat, spotLng)),
                title = spot.name,
                snippet = spot.type,
                onClick = {
                    onMarkerClick(spot)
                    false
                }
            )
        }
    } 
}
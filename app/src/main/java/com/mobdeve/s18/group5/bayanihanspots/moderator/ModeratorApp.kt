package com.mobdeve.s18.group5.bayanihanspots.moderator

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.manage.events.AddEventScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.spots.AddSpotScreen
import com.mobdeve.s18.group5.bayanihanspots.ui.moderator.ModeratorSpotScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorApp(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val firestore = FirebaseFirestore.getInstance()

    var pendingSpotsCount by remember { mutableIntStateOf(0) }
    var pendingEventsCount by remember { mutableIntStateOf(0) }

    // Listen for pending spots
    LaunchedEffect(Unit) {
        firestore.collection("spots")
            .whereEqualTo("approvalStatus", "PENDING")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    pendingSpotsCount = snapshot.size()
                }
            }
    }

    // Listen for pending events
    LaunchedEffect(Unit) {
        firestore.collection("events")
            .whereEqualTo("approvalStatus", "PENDING")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    pendingEventsCount = snapshot.size()
                }
            }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingSpotsCount > 0) {
                                    Badge { Text("$pendingSpotsCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Place, contentDescription = "Spots")
                        }
                    },
                    label = { Text("Spots") },
                    selected = currentRoute == "spots" || currentRoute?.startsWith("spot_detail") == true,
                    onClick = {
                        if (currentRoute != "spots") {
                            navController.navigate("spots") {
                                popUpTo("spots") { inclusive = true }
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingEventsCount > 0) {
                                    Badge { Text("$pendingEventsCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Event, contentDescription = "Events")
                        }
                    },
                    label = { Text("Events") },
                    selected = currentRoute == "events" || currentRoute?.startsWith("event_detail") == true,
                    onClick = {
                        if (currentRoute != "events") {
                            navController.navigate("events") {
                                popUpTo("events") { inclusive = true }
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = currentRoute == "logout_screen",
                    onClick = { navController.navigate("logout_screen") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "spots",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Spots moderation
            composable("spots") {
                ModeratorSpotList(
                    onSpotClick = { spotId ->
                        navController.navigate("spot_detail/$spotId")
                    },
                    onAddSpotClick = {
                        navController.navigate("add_spot")
                    }
                )
            }

            composable(
                route = "spot_detail/{spotId}",
                arguments = listOf(navArgument("spotId") { type = NavType.StringType })
            ) { backStackEntry ->
                val spotId = backStackEntry.arguments?.getString("spotId") ?: ""
                ModeratorSpotScreen(
                    spotId = spotId,
                    onBack = { navController.popBackStack() }
                )
            }

            // Add Spot (Moderator - auto-approved)
            composable("add_spot") {
                val context = LocalContext.current
                ModeratorAddSpotScreen(
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = {
                        Toast.makeText(context, "Spot added successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            }

            // Events moderation
            composable("events") {
                ModeratorEventList(
                    onEventClick = { eventId ->
                        navController.navigate("event_detail/$eventId")
                    },
                    onAddEventClick = {
                        navController.navigate("add_event")
                    }
                )
            }

            composable(
                route = "event_detail/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                ModeratorEventScreen(
                    eventId = eventId,
                    onBack = { navController.popBackStack() }
                )
            }

            // Add Event (Moderator - auto-approved)
            composable("add_event") {
                val context = LocalContext.current
                ModeratorAddEventScreen(
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = {
                        Toast.makeText(context, "Event added successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            }

            // Logout screen
            composable("logout_screen") {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bayanihan Spots",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Moderator Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(onClick = onLogout) {
                        Text("Confirm Logout")
                    }
                }
            }
        }
    }
}

/**
 * Wrapper for AddSpotScreen that auto-approves spots created by moderator
 */
@Composable
fun ModeratorAddSpotScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    // Use the regular AddSpotScreen with moderator mode enabled
    AddSpotScreen(
        onBack = onBack,
        onSaveSuccess = onSaveSuccess,
        isModeratorMode = true
    )
}

/**
 * Wrapper for AddEventScreen that auto-approves events created by moderator
 */
@Composable
fun ModeratorAddEventScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    AddEventScreen(
        onBack = onBack,
        onSaveSuccess = onSaveSuccess,
        isModeratorMode = true
    )
}


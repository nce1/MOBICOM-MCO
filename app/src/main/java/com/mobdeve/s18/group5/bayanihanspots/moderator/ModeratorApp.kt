package com.mobdeve.s18.group5.bayanihanspots.moderator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.ui.moderator.ModeratorSpotScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorApp(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val firestore = FirebaseFirestore.getInstance()

    var pendingEventsCount by remember { mutableStateOf(0) }
    var pendingProgramsCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        firestore.collection("spots")
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
                                if (pendingEventsCount > 0) {
                                    Badge { Text("$pendingEventsCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Event, contentDescription = "Events")
                        }
                    },
                    label = { Text("Events") },
                    selected = currentRoute == "events",
                    onClick = { navController.navigate("events") }
                )
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingProgramsCount > 0) {
                                    Badge { Text("$pendingProgramsCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.List, contentDescription = "Programs")
                        }
                    },
                    label = { Text("Programs") },
                    selected = currentRoute == "programs",
                    onClick = { navController.navigate("programs") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = currentRoute == "logout_screen",
                    onClick = { navController.navigate("logout_screen") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "events",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("events") {
                ModeratorSpotList(
                    onSpotClick = { spotId ->
                        navController.navigate("detail/$spotId")
                    }
                )
            }
            composable("programs") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Programs Feature Coming Soon")
                }
            }
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

            composable(
                route = "detail/{spotId}",
                arguments = listOf(navArgument("spotId") { type = NavType.StringType })
            ) { backStackEntry ->
                val spotId = backStackEntry.arguments?.getString("spotId") ?: ""
                ModeratorSpotScreen(
                    spotId = spotId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
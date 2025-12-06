package com.mobdeve.s18.group5.bayanihanspots

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobdeve.s18.group5.bayanihanspots.spots.SpotScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.mobdeve.s18.group5.bayanihanspots.auth.ui.login.LoginScreen
import com.mobdeve.s18.group5.bayanihanspots.auth.ui.login.LoginViewModel
import com.mobdeve.s18.group5.bayanihanspots.auth.ui.login.LoginViewModelFactory
import com.mobdeve.s18.group5.bayanihanspots.auth.ui.signup.SignupScreen
import com.mobdeve.s18.group5.bayanihanspots.auth.ui.signup.SignupViewModel
import com.mobdeve.s18.group5.bayanihanspots.auth.ui.signup.SignupViewModelFactory
import com.mobdeve.s18.group5.bayanihanspots.data.sync.PendingUploadManager
import com.mobdeve.s18.group5.bayanihanspots.ui.dashboard.EventsScreen
import com.mobdeve.s18.group5.bayanihanspots.ui.dashboard.EventsViewModel
import com.mobdeve.s18.group5.bayanihanspots.ui.dashboard.EventsViewModelFactory
import com.mobdeve.s18.group5.bayanihanspots.ui.home.HomeScreen
import com.mobdeve.s18.group5.bayanihanspots.ui.profile.ProfileScreen
import com.mobdeve.s18.group5.bayanihanspots.ui.profile.ProfileViewModel
import com.mobdeve.s18.group5.bayanihanspots.ui.profile.ProfileViewModelFactory
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.BayanihanSpotsTheme
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.mobdeve.s18.group5.bayanihanspots.auth.ui.login.ForgotPasswordScreen
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.manage.events.AddEventScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.events.EditEventScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.events.ManageEventsScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.events.ManageEventsViewModel
import com.mobdeve.s18.group5.bayanihanspots.manage.signups.ManageSignupsScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.spots.AddSpotScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.spots.EditSpotScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.spots.ManageSpotsScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.spots.ManageSpotsViewModel
import com.mobdeve.s18.group5.bayanihanspots.moderator.ModeratorApp
import com.mobdeve.s18.group5.bayanihanspots.notifications.EventReminderManager
import com.mobdeve.s18.group5.bayanihanspots.ui.dashboard.EventsUiState
import com.mobdeve.s18.group5.bayanihanspots.ui.favorites.FavoritesScreen
import com.mobdeve.s18.group5.bayanihanspots.ui.home.HomeViewModel
import com.mobdeve.s18.group5.bayanihanspots.ui.home.HomeViewModelFactory
import com.mobdeve.s18.group5.bayanihanspots.ui.notifications.NotificationScreen

class MainActivity : ComponentActivity(), OnMapsSdkInitializedCallback {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        // Initialize WorkManager for pending uploads (offline-first sync)
        initializeOfflineSync()

        // Initialize notification channel for event reminders
        EventReminderManager.createNotificationChannel(applicationContext)

        try {
            MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LEGACY, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setContent {
            BayanihanSpotsTheme {
                var currentUser by remember { mutableStateOf(auth.currentUser) }
                DisposableEffect(auth) {
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        currentUser = firebaseAuth.currentUser
                    }
                    auth.addAuthStateListener(listener)
                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }
                if (currentUser != null && currentUser?.email == "moderator.bayanihanspots@gmail.com") {
                    ModeratorApp(onLogout = {
                        auth.signOut()
                    })
                } else {
                    MainApp(auth, application)
                }
            }
        }
    }

    /**
     * Initialize offline-first sync with WorkManager.
     * This schedules periodic uploads for data created while offline.
     */
    private fun initializeOfflineSync() {
        try {
            val pendingUploadManager = PendingUploadManager.getInstance(applicationContext)
            pendingUploadManager.initializeWorker()
            Log.d("MainActivity", "Offline sync initialized successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize offline sync: ${e.message}")
        }
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d("MapsSetup", "Latest Renderer loaded")
            MapsInitializer.Renderer.LEGACY -> Log.d("MapsSetup", "Legacy Renderer loaded (Emulator Safe)")
        }
    }
    private fun isUserAdmin(): Boolean {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.email == "moderator.bayanihanspots@gmail.com"
    }
}

@Composable
fun MainApp(auth: FirebaseAuth, application: Application){
    val sharedViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(application))
    val navController = rememberNavController()
    val isLoggedIn by rememberAuthState(auth)
    val manageSpotsViewModel: ManageSpotsViewModel = viewModel()
    val manageEventsViewModel: ManageEventsViewModel = viewModel()
    Scaffold(bottomBar = { BottomNavBar(navController, isLoggedIn) })
    { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ){
            composable("home"){
                HomeScreen(
                    isLoggedIn = isLoggedIn,
                    onNavigateToDetails = { spot ->
                        navController.navigate("details/${spot.id}")
                    }
                )
            }
            composable("details/{spotId}", arguments = listOf(navArgument("spotId") { type = NavType.StringType })){ backStackEntry ->
                val spotId = backStackEntry.arguments?.getString("spotId") ?: ""
                val spotFromMemory = sharedViewModel.getSpotById(spotId)
                if (spotFromMemory != null){
                    SpotScreen(
                        spot = spotFromMemory,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable("events") {
                val viewModel: EventsViewModel = viewModel(factory = EventsViewModelFactory(application))
                val state by viewModel.uiState.collectAsState()
                val context = LocalContext.current
                val fusedLocationClient = remember { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context) }

                // Track event for check-in permission callback
                var pendingCheckInEvent by remember { mutableStateOf<Event?>(null) }

                // Function to perform the actual check-in
                fun performCheckIn(event: Event) {
                    event.coordinates?.let { eventCoords ->
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    // Calculate distance between user and event
                                    val results = FloatArray(1)
                                    android.location.Location.distanceBetween(
                                        location.latitude, location.longitude,
                                        eventCoords.latitude, eventCoords.longitude,
                                        results
                                    )
                                    val distanceInMeters = results[0]

                                    if (distanceInMeters <= 100f) {
                                        // User is within 100 meters - successful check-in!
                                        viewModel.checkInEvent(event, distanceInMeters)
                                        Toast.makeText(
                                            context,
                                            "✓ Checked in to '${event.title}'! You're ${distanceInMeters.toInt()}m away.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        // User is too far
                                        Toast.makeText(
                                            context,
                                            "You're ${distanceInMeters.toInt()}m away. Get within 100m of the event to check in.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Unable to get your location. Please try again.", Toast.LENGTH_SHORT).show()
                                }
                            }.addOnFailureListener {
                                Toast.makeText(context, "Failed to get location: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: SecurityException) {
                            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

                    if (fineLocationGranted) {
                        pendingCheckInEvent?.let { event ->
                            performCheckIn(event)
                        }
                    } else {
                        Toast.makeText(context, "Location permission required for check-in", Toast.LENGTH_SHORT).show()
                    }
                    pendingCheckInEvent = null
                }

                EventsScreen(
                    state = state,
                    onRefresh = { viewModel.refresh() },
                    isLoggedIn = isLoggedIn,
                    onLoginClick = { navController.navigate("login") },
                    onJoinEvent = { eventId ->
                        val event = (state as? EventsUiState.Success)?.events?.find { it.id == eventId }
                        if (event != null){
                            viewModel.joinEvent(event)
                        }
                    },
                    onLeaveEvent = { eventId ->
                        val event = (state as? EventsUiState.Success)?.events?.find { it.id == eventId }
                        if (event != null) {
                            viewModel.leaveEvent(event)
                        }
                    },
                    onCheckIn = { event ->
                        if (event.coordinates != null) {
                            // Check if we have location permission
                            val hasFineLocation = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasFineLocation) {
                                // Already have permission, check location immediately
                                performCheckIn(event)
                            } else {
                                // Request permission
                                pendingCheckInEvent = event
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        } else {
                            Toast.makeText(context, "This event doesn't have a location set", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            composable("favorites") {
                FavoritesScreen(
                    onSpotClick = { spot ->
                        navController.navigate("details/${spot.id}")
                    },
                    onLoginClick = { navController.navigate("login") }
                )
            }
            composable("profile"){
                val context = LocalContext.current
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context.applicationContext))
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogout = { navController.navigate("home"){
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onManageSpotsClick = {navController.navigate("manage_spots")},
                    onManageEventsClick = {navController.navigate("manage_events")},
                    onManageSignUpsClick = {navController.navigate("manage_signups")},
                    onNotificationsClick = {navController.navigate("notifications")}
                )
            }
            // Auth Area
            composable("signup") {
                val context = LocalContext.current
                val viewModel: SignupViewModel = viewModel(factory = SignupViewModelFactory(context.applicationContext))
                SignupScreen(viewModel = viewModel,
                    onSignUpSuccess = {
                        navController.navigate("profile") {
                            popUpTo("signup") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onLoginClick = { navController.navigate("login") }
                )
            }
            composable("login"){
                val context = LocalContext.current
                val viewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(context.applicationContext))

                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        if (auth.currentUser?.email == "moderator.bayanihanspots@gmail.com"){
                            // Do Nothing
                        } else {
                            navController.navigate("profile") {
                                popUpTo("signup") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onSignupClick = { navController.navigate("signup") },
                    onForgotPasswordClick = { navController.navigate("forgot_password") }
                )
            }
            composable("forgot_password") {
                ForgotPasswordScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("notifications"){
                NotificationScreen(
                    onBack = {
                        navController.navigate("home"){
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        }
                    }
                )
            }
            // Manage Spots Area
            composable("manage_spots"){
                val context = LocalContext.current
                LaunchedEffect(Unit){ manageSpotsViewModel.fetchUserSpots() }
                ManageSpotsScreen(
                    spots = manageSpotsViewModel.mySpots,
                    isLoading = manageSpotsViewModel.isLoading,
                    onBack = { navController.popBackStack() },
                    onAddSpot = { navController.navigate("add_spot") },
                    onEditSpot = { spotId -> navController.navigate("edit_spot/$spotId") },
                    onDeleteSpot = { spotId ->
                        manageSpotsViewModel.deleteSpot(
                            spotId = spotId,
                            onSuccess = {
                                android.widget.Toast.makeText(context, "Spot deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { e ->
                                android.widget.Toast.makeText(context, "Failed to delete: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }
            composable("add_spot"){
                AddSpotScreen(onBack = { navController.popBackStack() }, onSaveSuccess = { navController.popBackStack() })
            }
            composable(route = "edit_spot/{spotId}", arguments = listOf(navArgument("spotId"){ type = NavType.StringType })){ backStackEntry ->
                val spotId = backStackEntry.arguments?.getString("spotId") ?: ""
                val spot = manageSpotsViewModel.getSpotById(spotId)
                if (spot != null){
                    EditSpotScreen(
                        spot = spot,
                        onBack = { navController.popBackStack() },
                        onSaveSuccess = { manageSpotsViewModel.fetchUserSpots()
                            navController.popBackStack() }
                    )
                } else{
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }
            }
            // Manage Sign ups
            composable("manage_signups"){
                ManageSignupsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            // Manage Events (Programs)
            composable("manage_events"){
                val context = LocalContext.current
                LaunchedEffect(Unit){ manageEventsViewModel.fetchUserEvents() }
                ManageEventsScreen(
                    events = manageEventsViewModel.myEvents,
                    isLoading = manageEventsViewModel.isLoading,
                    onBack = { navController.popBackStack() },
                    onAddEvent = { navController.navigate("add_event") },
                    onEditEvent = { eventId -> navController.navigate("edit_event/$eventId") },
                    onDeleteEvent = { eventId ->
                        manageEventsViewModel.deleteEvent(
                            eventId = eventId,
                            onSuccess = {
                                android.widget.Toast.makeText(context, "Program deleted successfully", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { e ->
                                android.widget.Toast.makeText(context, "Failed to delete: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }
            // Add Event
            composable("add_event") {
                AddEventScreen(
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = {
                        manageEventsViewModel.fetchUserEvents()
                        navController.popBackStack()
                    }
                )
            }
            // Edit Event
            composable(
                route = "edit_event/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                val event = manageEventsViewModel.getEventById(eventId)
                if (event != null) {
                    EditEventScreen(
                        event = event,
                        onBack = { navController.popBackStack() },
                        onSaveSuccess = {
                            manageEventsViewModel.fetchUserEvents()
                            navController.popBackStack()
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        LaunchedEffect(Unit) { navController.popBackStack() }
                    }
                }
            }
        }
    }
}
@Composable
fun BottomNavBar(navController: NavHostController, isLoggedIn: Boolean) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute(navController) == "home",
            onClick = { navController.navigate("home") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home_black_24dp),
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute(navController) == "events",
            onClick = { navController.navigate("events") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dashboard_black_24dp),
                    contentDescription = "Events"
                )
            },
            label = { Text("Events") }
        )
        NavigationBarItem(
            selected = currentRoute(navController) == "favorites",
            onClick = { navController.navigate("favorites") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite_24dp),
                    contentDescription = "Favorites"
                )
            },
            label = { Text("Favorites") }
        )
        NavigationBarItem(
            selected = currentRoute(navController) == "notifications",
            onClick = {
                if (isLoggedIn) {
                    navController.navigate("notifications")
                } else {
                    navController.navigate("login")
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notifications_black_24dp),
                    contentDescription = "Notifications"
                )
            },
            label = { Text("Updates") },
        )
        NavigationBarItem(
            selected = currentRoute(navController) == "profile",
            onClick = {
                if (!isLoggedIn) {
                    navController.navigate("login")
                } else {
                    navController.navigate("profile")
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_profile_black_24dp),
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") }
        )
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val backStackEntry by navController.currentBackStackEntryAsState()
    return backStackEntry?.destination?.route
}

@Composable
private fun rememberAuthState(auth: FirebaseAuth): State<Boolean> {
    val loggedIn = remember { mutableStateOf(auth.currentUser != null) }
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            loggedIn.value = firebaseAuth.currentUser != null
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }
    return loggedIn
}
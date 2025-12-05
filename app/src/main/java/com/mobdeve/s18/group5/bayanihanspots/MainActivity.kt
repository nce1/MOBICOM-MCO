package com.mobdeve.s18.group5.bayanihanspots

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.manage.signups.ManageSignupsScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.spots.AddSpotScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.spots.EditSpotScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.spots.ManageSpotsScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.spots.ManageSpotsViewModel
import com.mobdeve.s18.group5.bayanihanspots.manage.programs.AddProgramScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.programs.EditProgramScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.programs.ManageProgramsScreen
import com.mobdeve.s18.group5.bayanihanspots.manage.programs.ManageProgramsViewModel
import com.mobdeve.s18.group5.bayanihanspots.moderator.ModeratorApp
import com.mobdeve.s18.group5.bayanihanspots.ui.dashboard.EventsUiState
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
    val manageProgramsViewModel: ManageProgramsViewModel = viewModel()
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
                    }
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
                    onManageSpotsClick = { navController.navigate("manage_spots") },
                    onManageProgramsClick = { navController.navigate("manage_programs") },
                    onManageSignupsClick = { navController.navigate("manage_signups") }
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
                    onSignupClick = { navController.navigate("signup") }
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
            // Manage Programs Area
            composable("manage_programs") {
                val context = LocalContext.current
                LaunchedEffect(Unit) { manageProgramsViewModel.fetchUserPrograms() }
                ManageProgramsScreen(
                    programs = manageProgramsViewModel.myPrograms,
                    isLoading = manageProgramsViewModel.isLoading,
                    onBack = { navController.popBackStack() },
                    onAddProgram = { navController.navigate("add_program") },
                    onEditProgram = { programId -> navController.navigate("edit_program/$programId") },
                    onDeleteProgram = { programId ->
                        manageProgramsViewModel.deleteProgram(
                            programId = programId,
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
            composable("add_program") {
                AddProgramScreen(
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = {
                        manageProgramsViewModel.refreshPrograms()
                        navController.popBackStack()
                    }
                )
            }
            composable(route = "edit_program/{programId}", arguments = listOf(navArgument("programId") { type = NavType.StringType })) { backStackEntry ->
                val programId = backStackEntry.arguments?.getString("programId") ?: ""
                val program = manageProgramsViewModel.getProgramById(programId)
                if (program != null) {
                    EditProgramScreen(
                        program = program,
                        onBack = { navController.popBackStack() },
                        onSaveSuccess = {
                            manageProgramsViewModel.refreshPrograms()
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
            // Manage Sign ups
            composable("manage_signups"){
                ManageSignupsScreen(
                    onBack = { navController.popBackStack() }
                )
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
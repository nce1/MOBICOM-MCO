package com.mobdeve.s18.group5.bayanihanspots

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.mobdeve.s18.group5.bayanihanspots.ui.dashboard.EventsScreen
import com.mobdeve.s18.group5.bayanihanspots.ui.dashboard.EventsViewModel
import com.mobdeve.s18.group5.bayanihanspots.ui.dashboard.EventsViewModelFactory
import com.mobdeve.s18.group5.bayanihanspots.ui.home.HomeScreen
import com.mobdeve.s18.group5.bayanihanspots.ui.profile.ProfileScreen
import com.mobdeve.s18.group5.bayanihanspots.ui.profile.ProfileViewModel
import com.mobdeve.s18.group5.bayanihanspots.ui.profile.ProfileViewModelFactory
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.BayanihanSpotsTheme
import androidx.compose.ui.res.painterResource
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

class MainActivity : ComponentActivity(), OnMapsSdkInitializedCallback {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        try {
            MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LEGACY, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setContent {
            BayanihanSpotsTheme {
                MainApp(auth)
            }
        }
    }
    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d("MapsSetup", "Latest Renderer loaded")
            MapsInitializer.Renderer.LEGACY -> Log.d("MapsSetup", "Legacy Renderer loaded (Emulator Safe)")
        }
    }
}

@Composable
fun MainApp(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val isLoggedIn by rememberAuthState(auth)
    Scaffold(bottomBar = { BottomNavBar(navController, isLoggedIn) })
    { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    isLoggedIn = isLoggedIn,
                    onNavigateToDetails = { spotId ->
                        println("hello")
                    }
                )
            }
            composable("events") {
                val viewModel: EventsViewModel = viewModel(factory = EventsViewModelFactory())
                val state by viewModel.uiState.collectAsState()
                EventsScreen(
                    state = state,
                    onRefresh = { viewModel.refresh() },
                    isLoggedIn = isLoggedIn,
                    onLoginClick = { navController.navigate("login") }
                )
            }
            composable("profile") {
                val context = LocalContext.current
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context.applicationContext))
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogout = { navController.navigate("home"){
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
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
            composable("login") {
                val context = LocalContext.current
                val viewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(context.applicationContext))
                LoginScreen(viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate("profile") {
                            popUpTo("signup") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onSignupClick = { navController.navigate("signup") }
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
package com.mobdeve.s18.group5.bayanihanspots

import android.os.Bundle
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
import com.mobdeve.s18.group5.bayanihanspots.ui.dashboard.DashboardScreen
import com.mobdeve.s18.group5.bayanihanspots.ui.home.HomeScreen
import com.mobdeve.s18.group5.bayanihanspots.ui.profile.ProfileScreen
import com.mobdeve.s18.group5.bayanihanspots.ui.profile.ProfileViewModel
import com.mobdeve.s18.group5.bayanihanspots.ui.profile.ProfileViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            MainApp(auth)
        }
    }
}

@Composable
fun MainApp(auth: FirebaseAuth) {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomNavBar(navController, auth) })
    { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("home") { HomeScreen() }
            composable("dashboard") { DashboardScreen() }
            // Add notifications
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
fun BottomNavBar(navController: NavHostController, auth: FirebaseAuth) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute(navController) == "home",
            onClick = { navController.navigate("home") },
            icon = {},
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute(navController) == "dashboard",
            onClick = { navController.navigate("dashboard") },
            icon = {},
            label = { Text("Dashboard") }
        )
        NavigationBarItem(
            selected = currentRoute(navController) == "profile",
            onClick = {
                if (auth.currentUser == null) {
                    navController.navigate("login")
                } else {
                    navController.navigate("profile")
                }
            },
            icon = {},
            label = { Text("Profile") }
        )
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val backStackEntry by navController.currentBackStackEntryAsState()
    return backStackEntry?.destination?.route
}
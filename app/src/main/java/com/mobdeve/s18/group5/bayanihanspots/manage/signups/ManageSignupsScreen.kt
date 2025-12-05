package com.mobdeve.s18.group5.bayanihanspots.manage.signups

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.repository.OfflineFirstEventRepository
import com.mobdeve.s18.group5.bayanihanspots.data.signups.Signups
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSignupsScreen(onBack: () -> Unit){
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val repo = remember { OfflineFirstEventRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    var signups by remember { mutableStateOf<List<Signups>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit){
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        firestore.collection("signups")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snap, _ ->
                if (snap != null){
                    val rawList = snap.toObjects(Signups::class.java)
                    signups = rawList.sortedWith(
                        compareByDescending<Signups> { it.status == "CONFIRMED" }
                            .thenByDescending { it.timestamp }
                    )
                }
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Volu-nteering") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { p ->
        if (isLoading){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (signups.isEmpty()){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't joined any events yet.", color = Color.Gray)
            }
        } else{
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(p)
            ){
                items(signups) { signup ->
                    SignupItem(
                        signup = signup,
                        isProcessing = isProcessing,
                        onCancel = {
                            if (!isProcessing) {
                                isProcessing = true
                                scope.launch {
                                    val result = repo.leaveEvent(signup.signupId, signup.eventId)
                                    isProcessing = false
                                    result.onFailure { e ->
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SignupItem(signup: Signups, isProcessing: Boolean, onCancel: () -> Unit){
    val isConfirmed = signup.status == "CONFIRMED"
    Card(elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = if (isConfirmed) Color.White else Color(0xFFF5F5F5))){
        Column(modifier = Modifier.padding(16.dp)){
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                Text(
                    text = signup.eventTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = if (isConfirmed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = MaterialTheme.shapes.small
                ){
                    Text(
                        text = signup.status,
                        color = if (isConfirmed) Color(0xFF2E7D32) else Color(0xFFC62828),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (isConfirmed){
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isProcessing,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    modifier = Modifier.align(Alignment.End)
                ){
                    Text("Cancel Signup")
                }
            } else{
                Text(
                    text = "You have left this event.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
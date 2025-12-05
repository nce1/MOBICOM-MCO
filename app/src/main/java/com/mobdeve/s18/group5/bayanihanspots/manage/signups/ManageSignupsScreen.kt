package com.mobdeve.s18.group5.bayanihanspots.manage.signups

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.data.repository.OfflineFirstEventRepository
import com.mobdeve.s18.group5.bayanihanspots.data.signups.Signups
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

data class SignupWithEvent(
    val signup: Signups,
    val event: Event?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSignupsScreen(viewModel: ManageSignupsViewModel = viewModel(factory = ManageSignupsViewModelFactory(LocalContext.current.applicationContext as Application)), onBack: () -> Unit){
    val context = LocalContext.current

    val signupsWithEvents by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isProcessingId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Volunteering") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (signupsWithEvents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't joined any events yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(innerPadding)
            ) {
                items(signupsWithEvents) { item ->
                    if (item.event != null) {
                        SignupItem(
                            signup = item.signup,
                            event = item.event,
                            isProcessing = isProcessingId == item.signup.signupId,
                            onCancel = {
                                if (isProcessingId == null) {
                                    isProcessingId = item.signup.signupId

                                    viewModel.cancelSignup(
                                        signupId = item.signup.signupId,
                                        eventId = item.signup.eventId,
                                        onSuccess = {
                                            isProcessingId = null
                                            Toast.makeText(context, "Signup cancelled", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { msg ->
                                            isProcessingId = null
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SignupItem(signup: Signups, event: Event, isProcessing: Boolean, onCancel: () -> Unit){
    val isConfirmed = signup.status == "CONFIRMED"

    val containerColor = if (isConfirmed) Color.White else Color(0xFFF9F9F9)
    val statusBg = if (isConfirmed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val statusColor = if (isConfirmed) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        elevation = CardDefaults.cardElevation(if (isConfirmed) 2.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = signup.status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatScheduleTime(event.schedule),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.locationLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Log.d("MANAGE", "Location Label: " + event.coordinates)

            Spacer(modifier = Modifier.height(8.dp))
            if (!event.host.isNullOrEmpty()) {
                Text(
                    text = "Hosted by ${event.host}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            if (isConfirmed) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        OutlinedButton(
                            onClick = onCancel,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Cancel Signup", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Text("Cancelled", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

fun formatScheduleTime(rawDate: String): String{
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
        val date = inputFormat.parse(rawDate)
        if (date != null) outputFormat.format(date) else rawDate
    } catch (e: Exception) {
        rawDate
    }
}
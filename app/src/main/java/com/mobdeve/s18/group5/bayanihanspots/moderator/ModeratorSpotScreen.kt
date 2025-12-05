package com.mobdeve.s18.group5.bayanihanspots.ui.moderator

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.mobdeve.s18.group5.bayanihanspots.data.spots.toSpot
import java.util.Date

@Composable
fun ModeratorSpotScreen(spotId: String, onBack: () -> Unit){
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var spot by remember { mutableStateOf<Spot?>(null) }
    LaunchedEffect(spotId) {
        firestore.collection("spots").document(spotId).get()
            .addOnSuccessListener { doc -> spot = doc.toSpot() }
    }
    if (spot == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BottomAppBar(containerColor = Color.White, tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            firestore.collection("spots").document(spotId)
                                .update("approvalStatus", "REJECTED")
                                .addOnSuccessListener {
                                    sendNotification(
                                        userId = spot!!.userID,
                                        title = "Request Rejected",
                                        message = "Your spot '${spot!!.name}' was declined by a moderator."
                                    )
                                    Toast.makeText(context, "Request Rejected", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.1f), contentColor = Color.Red),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reject")
                    }
                    Button(
                        onClick = {
                            firestore.collection("spots").document(spotId)
                                .update("approvalStatus", "APPROVED")
                                .addOnSuccessListener{
                                    val title: String
                                    val message: String
                                    if (spot!!.modificationType == "EDIT"){
                                        title = "Changes Approved"
                                        message = "Your edits to '${spot!!.name}' are now live."
                                    } else{
                                        title = "Spot Approved!"
                                        message = "Congratulations! '${spot!!.name}' has been added to the map."
                                    }
                                    sendNotification(spot!!.userID, title, message)
                                    Toast.makeText(context, "Approved!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Approve")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                if (spot!!.imageList.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { spot!!.imageList.size })
                    HorizontalPager(state = pagerState) { page ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(spot!!.imageList[page])
                                .crossfade(true).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (spot!!.imageList.size > 1) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${spot!!.imageList.size}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
                        Icon(Icons.Default.Image, null, Modifier.align(Alignment.Center))
                    }
                }
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).background(Color.Black.copy(0.4f), CircleShape).align(Alignment.TopStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(spot!!.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Submitted by: ${spot!!.userID}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                ModeratorField("Type", spot!!.type)
                ModeratorField("Crowd Level", spot!!.crowdLevel)

                Spacer(modifier = Modifier.height(24.dp))
                Text("Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(spot!!.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ModeratorField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
fun sendNotification(userId: String, title: String, message: String) {
    val db = FirebaseFirestore.getInstance()

    val notificationData = hashMapOf(
        "title" to title,
        "message" to message,
        "timestamp" to Date(),
        "isRead" to false
    )

    db.collection("users").document(userId)
        .collection("notifications")
        .add(notificationData)
        .addOnSuccessListener {
            println("Notification sent to $userId")
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
        }
}
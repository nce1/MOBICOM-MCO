package com.mobdeve.s18.group5.bayanihanspots.manage.spots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSpotsScreen(spots: List<Spot>, isLoading: Boolean, onBack: () -> Unit, onAddSpot: () -> Unit, onEditSpot: (String) -> Unit, onDeleteSpot: (String) -> Unit){
    var spotToDelete by remember { mutableStateOf<Spot?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Spots") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ), windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ){
            Button(
                onClick = onAddSpot,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ){
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Spot")
            }

            if (isLoading){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (spots.isEmpty()){
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't added any spots yet.", color = Color.Gray)
                }
            } else{
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    items(spots) { spot ->
                        MySpotItem(
                            spot = spot,
                            onEdit = { onEditSpot(spot.id) },
                            onDelete = { spotToDelete = spot }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (spotToDelete != null) {
        AlertDialog(
            onDismissRequest = { spotToDelete = null },
            title = { Text("Delete Spot") },
            text = { Text("Are you sure you want to delete \"${spotToDelete!!.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSpot(spotToDelete!!.id)
                        spotToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { spotToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MySpotItem(spot: Spot, onEdit: () -> Unit, onDelete: () -> Unit){
    Card(elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = Color.White)){
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically){
            val image = spot.imageList.firstOrNull()
            if (image != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(image).crossfade(true).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
                )
            } else{
                Box(
                    modifier = Modifier.size(60.dp).background(Color.LightGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ){
                    Icon(Icons.Default.Image, null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)){
                Text(spot.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val statusText: String
                val statusColor: Color
                when (spot.approvalStatus) {
                    "APPROVED" -> { statusText = "LIVE"
                        statusColor = Color(0xFF4CAF50)
                    }
                    "REJECTED" -> {
                        statusText = "REJECTED"
                        statusColor = Color.Red
                    }
                    else -> {
                        statusText = if (spot.modificationType == "EDIT") "WAITING FOR REVIEW" else "PENDING"
                        statusColor = Color(0xFFFF9800)
                    }
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Show delete button for pending or rejected spots
            if (spot.approvalStatus != "APPROVED") {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
            }
        }
    }
}
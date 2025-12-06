package com.mobdeve.s18.group5.bayanihanspots.ui.favorites

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s18.group5.bayanihanspots.data.local.BayanihanDatabase
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.mobdeve.s18.group5.bayanihanspots.data.spots.toSpot
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.AccentCoral
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SecondarySage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onSpotClick: (Spot) -> Unit,
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    val database = remember { BayanihanDatabase.getInstance(context) }
    val favoriteSpotDao = database.favoriteSpotDao()

    var favoriteSpots by remember { mutableStateOf<List<Spot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Collect favorite spot IDs
    val favoriteEntities by favoriteSpotDao.getFavoriteSpots(currentUser?.uid ?: "")
        .collectAsState(initial = emptyList())

    // Fetch spot details from Firestore when favorites change
    LaunchedEffect(favoriteEntities) {
        Log.d("FavoritesScreen", "favoriteEntities changed: ${favoriteEntities.size} items")
        Log.d("FavoritesScreen", "Current user: ${currentUser?.uid}")

        if (currentUser != null && favoriteEntities.isNotEmpty()) {
            isLoading = true
            try {
                val spots = favoriteEntities.mapNotNull { entity ->
                    try {
                        Log.d("FavoritesScreen", "Fetching spot: ${entity.spotId}")
                        val doc = firestore.collection("spots").document(entity.spotId).get().await()
                        val spot = doc.toSpot()
                        Log.d("FavoritesScreen", "Fetched spot: ${spot?.name}")
                        spot
                    } catch (e: Exception) {
                        Log.e("FavoritesScreen", "Error fetching spot ${entity.spotId}: ${e.message}")
                        null
                    }
                }
                favoriteSpots = spots
                Log.d("FavoritesScreen", "Total favorite spots loaded: ${spots.size}")
            } catch (e: Exception) {
                Log.e("FavoritesScreen", "Error fetching favorites: ${e.message}")
                e.printStackTrace()
            }
            isLoading = false
        } else {
            Log.d("FavoritesScreen", "No favorites or not logged in")
            favoriteSpots = emptyList()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Favorite Spots",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryTeal
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (currentUser == null) {
            // Not logged in state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SecondarySage),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = PrimaryTeal
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Sign in to save favorites",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Keep track of your favorite spots by signing in",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("Sign In")
                }
            }
        } else if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryTeal)
            }
        } else if (favoriteSpots.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SecondarySage),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = PrimaryTeal
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "No favorites yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Explore spots and tap the heart to save them here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Favorites list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text(
                        "${favoriteSpots.size} favorite${if (favoriteSpots.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(favoriteSpots) { spot ->
                    FavoriteSpotCard(
                        spot = spot,
                        onClick = { onSpotClick(spot) },
                        onRemoveFavorite = {
                            scope.launch {
                                favoriteSpotDao.removeFavorite(spot.id, currentUser.uid)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteSpotCard(
    spot: Spot,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SecondarySage.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                val thumbnailUrl = spot.imageList.firstOrNull()
                if (thumbnailUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = spot.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Spot Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = spot.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = spot.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type badge
                    Surface(
                        color = SecondarySage,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = spot.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryTeal,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Crowd level
                    val crowdColor = when (spot.crowdLevel) {
                        "Busy" -> AccentCoral
                        else -> PrimaryTeal
                    }
                    Surface(
                        color = crowdColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = spot.crowdLevel,
                            style = MaterialTheme.typography.labelSmall,
                            color = crowdColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Remove favorite button
            IconButton(
                onClick = onRemoveFavorite,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = AccentCoral
                )
            }
        }
    }
}


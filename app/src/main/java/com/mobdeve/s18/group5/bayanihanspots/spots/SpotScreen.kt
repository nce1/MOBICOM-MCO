package com.mobdeve.s18.group5.bayanihanspots.spots

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mobdeve.s18.group5.bayanihanspots.data.local.BayanihanDatabase
import com.mobdeve.s18.group5.bayanihanspots.data.local.entity.FavoriteSpotEntity
import com.mobdeve.s18.group5.bayanihanspots.data.review.Review
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotScreen(spot: Spot, onBack: () -> Unit){
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database for favorites
    val database = remember { BayanihanDatabase.getInstance(context) }
    val favoriteSpotDao = database.favoriteSpotDao()

    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }

    var userRating by remember { mutableStateOf(0) }
    var userComment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    var reviewToEdit by remember { mutableStateOf<Review?>(null) }

    // Favorite state
    val isFavorite by favoriteSpotDao.isFavorite(spot.id, currentUser?.uid ?: "")
        .collectAsState(initial = 0)

    LaunchedEffect(spot.id) {
        firestore.collection("spots").document(spot.id)
            .collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    reviews = snapshot.toObjects(Review::class.java)
                }
            }
    }
    val (myReview, otherReviews) = remember(reviews, currentUser){
        val mine = reviews.find { it.userID == currentUser?.uid }
        val others = reviews.filter { it.userID != currentUser?.uid }
        Pair(mine, others)
    }
    if (reviewToEdit != null){
        EditReviewDialog(
            review = reviewToEdit!!,
            onDismiss = { reviewToEdit = null },
            onConfirm = { newRating, newComment ->
                if (reviewToEdit!!.reviewID.isNotEmpty()){
                    firestore.collection("spots").document(spot.id)
                        .collection("reviews").document(reviewToEdit!!.reviewID)
                        .update("rating", newRating, "comment", newComment)
                        .addOnSuccessListener { Toast.makeText(context, "Review Updated", Toast.LENGTH_SHORT).show() }
                }
                reviewToEdit = null
            }
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()).verticalScroll(rememberScrollState())){
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)){
                if (spot.imageList.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { spot.imageList.size })
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(spot.imageList[page])
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (spot.imageList.size > 1){
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                            shape = CircleShape
                        ){
                            Text(
                                text = "${pagerState.currentPage + 1}/${spot.imageList.size}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else{
                    Box(
                        modifier = Modifier.fillMaxSize().background(SecondarySage.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ){
                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    }
                }

                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.scrim.copy(0.4f), CircleShape).align(Alignment.TopStart)
                ){
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }

            // Spot Section
            Column(modifier = Modifier.padding(24.dp)){
                Text(spot.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                    DetailBadge(text = spot.type, color = SecondarySage)
                    DetailBadge(text = "Open", color = PrimaryTeal.copy(alpha = 0.3f))
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                    DetailBadge(text = "Crowd: ${spot.crowdLevel}", color = MaterialTheme.colorScheme.surfaceVariant)
                    if (spot.distanceString.isNotBlank()){
                        DetailBadge(text = spot.distanceString.replace("•", "").trim(), color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Favorite button
                if (currentUser != null) {
                    Button(
                        onClick = {
                            scope.launch {
                                if (isFavorite > 0) {
                                    favoriteSpotDao.removeFavorite(spot.id, currentUser.uid)
                                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                                } else {
                                    favoriteSpotDao.addFavorite(
                                        FavoriteSpotEntity(
                                            spotId = spot.id,
                                            userId = currentUser.uid
                                        )
                                    )
                                    Toast.makeText(context, "Added to favorites!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFavorite > 0) AccentCoral else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            if (isFavorite > 0) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite > 0) MaterialTheme.colorScheme.onPrimary else AccentCoral,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isFavorite > 0) "Favorited" else "Add to Favorites",
                            color = if (isFavorite > 0) MaterialTheme.colorScheme.onPrimary else AccentCoral
                        )
                    }
                }


                Spacer(modifier = Modifier.height(24.dp))
                Text("About this spot", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(4.dp))
                Text(spot.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(0.8f))
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            // Review Section
            Column(modifier = Modifier.padding(24.dp)){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text("Reviews (${reviews.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    if (myReview != null) {
                        Text("You reviewed this spot", style = MaterialTheme.typography.labelSmall, color = PrimaryTeal)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (currentUser != null && myReview == null){
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rate & Review", style = MaterialTheme.typography.labelMedium)
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    (1..5).forEach { star ->
                                        Icon(
                                            imageVector = if (star <= userRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = null,
                                            tint = if (star <= userRating) Color(0xFFFFC107) else Color.Gray,
                                            modifier = Modifier.size(24.dp).clickable { userRating = star }
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = userComment,
                                    onValueChange = { userComment = it },
                                    placeholder = { Text("Experience...", style = MaterialTheme.typography.bodySmall) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    minLines = 1, maxLines = 3
                                )
                            }
                            Button(
                                onClick = {
                                    if (userRating == 0) return@Button
                                    isSubmitting = true
                                    val newReview = Review(
                                        spotID = spot.id,
                                        userID = currentUser.uid,
                                        userName = currentUser.displayName ?: "User",
                                        rating = userRating,
                                        comment = userComment
                                    )
                                    firestore.collection("spots").document(spot.id).collection("reviews")
                                        .add(newReview)
                                        .addOnSuccessListener {
                                            userComment = ""; userRating = 0; isSubmitting = false
                                        }
                                },
                                enabled = !isSubmitting && userComment.isNotBlank(),
                                modifier = Modifier.padding(start = 8.dp).align(Alignment.Bottom),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                            ) { Text("Post", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                else if (currentUser == null){
                    Box(modifier = Modifier.fillMaxWidth().background(SecondarySage.copy(0.2f), RoundedCornerShape(8.dp)).padding(16.dp)) {
                        Text("Log in to review", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (reviews.isEmpty()){
                    Text("No reviews yet. Be the first!", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                } else {
                    if (myReview != null) {
                        ReviewItem(
                            review = myReview,
                            isOwner = true,
                            onEdit = { reviewToEdit = myReview },
                            onDelete = { deleteReview(firestore, spot.id, myReview, context) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    otherReviews.forEach { review ->
                        ReviewItem(
                            review = review,
                            isOwner = false,
                            onEdit = {},
                            onDelete = {}
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
fun deleteReview(db: FirebaseFirestore, spotId: String, review: Review, context: android.content.Context){
    if (review.reviewID.isNotEmpty()){
        db.collection("spots").document(spotId)
            .collection("reviews").document(review.reviewID)
            .delete()
            .addOnSuccessListener { Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show() }
    }
}
@Composable
fun EditReviewDialog(review: Review, onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit){
    var rating by remember { mutableStateOf(review.rating) }
    var comment by remember { mutableStateOf(review.comment) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Review") },
        text = {
            Column {
                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (star <= rating) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier.size(28.dp).clickable { rating = star }
                        )
                    }
                }
                OutlinedTextField(value = comment, onValueChange = { comment = it }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(rating, comment) }) { Text("Update") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// For Review
@Composable
fun ReviewItem(review: Review, isOwner: Boolean, onEdit: () -> Unit, onDelete: () -> Unit){
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(review.userName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                repeat(review.rating) { Icon(Icons.Filled.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp)) }
            }
            Spacer(modifier = Modifier.weight(1f))

            if (isOwner){
                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp).clickable { onEdit() })
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error.copy(0.6f), modifier = Modifier.size(20.dp).clickable { onDelete() })
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(review.comment, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
    }
}

@Composable
fun DetailBadge(text: String, color: Color) {
    Surface(color = color, shape = RoundedCornerShape(6.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.onSurface)
    }
}

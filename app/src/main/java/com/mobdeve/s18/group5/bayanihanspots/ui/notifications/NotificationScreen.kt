package com.mobdeve.s18.group5.bayanihanspots.ui.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.AccentCoral
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SecondarySage
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SurfaceOffWhite
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.TextCharcoal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserNotification(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Date = Date(),
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var notifications by remember { mutableStateOf<List<UserNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Delete notification function
    fun deleteNotification(notificationId: String) {
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .collection("notifications").document(notificationId)
                .delete()
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        notifications = snapshot.toObjects(UserNotification::class.java)
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifications",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryTeal
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = SurfaceOffWhite
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryTeal)
            }
        } else if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = PrimaryTeal
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "No notifications yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextCharcoal,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You'll see updates about your activities here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCharcoal.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = notifications,
                    key = { it.id }
                ) { notification ->
                    SwipeableNotificationItem(
                        notification = notification,
                        onMarkAsRead = {
                            if (currentUser != null) {
                                firestore.collection("users").document(currentUser.uid)
                                    .collection("notifications").document(notification.id)
                                    .update("isRead", true)
                            }
                        },
                        onDelete = { deleteNotification(notification.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNotificationItem(
    notification: UserNotification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Swipe right = Mark as read
                    onMarkAsRead()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Swipe left = Delete
                    onDelete()
                    true
                }
                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.50f }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isSwipingRight = dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd
            val isSwipingLeft = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart

            val color by animateColorAsState(
                when {
                    isSwipingRight -> PrimaryTeal // Green for mark as read
                    isSwipingLeft -> AccentCoral // Red for delete
                    else -> Color.Transparent
                },
                label = "color"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                when {
                    isSwipingRight -> Icon(Icons.Default.MarkEmailRead, "Mark as Read", tint = Color.White)
                    isSwipingLeft -> Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                }
            }
        },
        content = {
            NotificationItem(notification)
        }
    )
}

@Composable
fun NotificationItem(notification: UserNotification) {
    val dateFormatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val containerColor = if (notification.isRead) SurfaceOffWhite else Color.White
    val titleWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold
    val titleColor = if (notification.isRead) TextCharcoal.copy(alpha = 0.6f) else PrimaryTeal
    val iconTint = if (notification.isRead) TextCharcoal.copy(alpha = 0.4f) else PrimaryTeal
    val iconBgColor = if (notification.isRead) SecondarySage.copy(alpha = 0.3f) else SecondarySage

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (notification.isRead) 0.dp else 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (notification.isRead) Icons.Default.Drafts else Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = titleWeight,
                        color = titleColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = dateFormatter.format(notification.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCharcoal.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notification.isRead) TextCharcoal.copy(alpha = 0.5f) else TextCharcoal.copy(alpha = 0.8f)
                )

                // Swipe hint for unread
                if (!notification.isRead) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "← Swipe to mark read • Swipe to delete →",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCharcoal.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
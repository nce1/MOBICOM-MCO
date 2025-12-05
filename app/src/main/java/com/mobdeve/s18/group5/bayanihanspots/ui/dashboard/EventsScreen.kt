package com.mobdeve.s18.group5.bayanihanspots.ui.dashboard

<<<<<<< Updated upstream
import android.util.Log
=======
import android.widget.Toast
>>>>>>> Stashed changes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< Updated upstream
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.manage.signups.formatScheduleTime
=======
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SecondarySage
>>>>>>> Stashed changes
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SurfaceOffWhite
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.TextCharcoal

@Composable
fun EventsScreen(
    state: EventsUiState,
    onRefresh: () -> Unit,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
<<<<<<< Updated upstream
    onJoinEvent: (String) -> Unit
=======
    onJoinEvent: (Event) -> Unit = {},
    onLeaveEvent: (String) -> Unit = {},
    onToggleFavorite: (Event) -> Unit = {},
    actionResult: ActionResult? = null,
    onClearActionResult: () -> Unit = {}
>>>>>>> Stashed changes
) {
    val context = LocalContext.current

    // Show toast for action results
    LaunchedEffect(actionResult) {
        actionResult?.let { result ->
            val message = when (result) {
                is ActionResult.Success -> result.message
                is ActionResult.Error -> result.message
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onClearActionResult()
        }
    }

    when (state) {
        EventsUiState.Loading -> EventsLoading()
        is EventsUiState.Error -> EventsError(state.message, onRefresh)
        is EventsUiState.Success -> EventsList(
            events = state.events,
            joinedEventIds = state.joinedEventIds,
            favoriteEventIds = state.favoriteEventIds,
            isLoggedIn = isLoggedIn,
            onLoginClick = onLoginClick,
            onJoinEvent = onJoinEvent,
            onLeaveEvent = onLeaveEvent,
            onToggleFavorite = onToggleFavorite
        )
    }
}

@Composable
private fun EventsLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryTeal)
    }
}

@Composable
private fun EventsError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = TextCharcoal)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun EventsList(
    events: List<Event>,
    joinedEventIds: Set<String>,
<<<<<<< Updated upstream
    isLoggedIn: Boolean,
=======
    favoriteEventIds: Set<String>,
>>>>>>> Stashed changes
    onLoginClick: () -> Unit,
    onJoinEvent: (Event) -> Unit,
    onLeaveEvent: (String) -> Unit,
    onToggleFavorite: (Event) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
<<<<<<< Updated upstream
                Text("Programs & Events", style = MaterialTheme.typography.headlineSmall, color = TextCharcoal)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Volunteer with nearby barangays, gardens, and pop-ups.", style = MaterialTheme.typography.bodyMedium, color = TextCharcoal)
            }
        }
        items(events) { event ->
            Log.d("EVENTS", "Max: "+ event.maxVolunteers)
            Log.d("EVENTS", "Curr: "+ event.currentVolunteers)
            val max = event.maxVolunteers ?: 0
            val current = event.currentVolunteers
            val isFull = max > 0 && current!! >= max
            val isJoined = joinedEventIds.contains(event.id)
            val isButtonEnabled = !isJoined && !isFull
            val buttonText = when {
                isJoined -> "Joined"
                isFull -> "Full"
                !isLoggedIn -> "Login to RSVP"
                else -> "Join"
            }
            Card(
=======
                Text(
                    text = "Programs & Events",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextCharcoal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Volunteer with nearby barangays, gardens, and pop-ups.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCharcoal.copy(alpha = 0.7f)
                )
            }
        }

        if (events.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No events available",
                        color = Color.Gray
                    )
                }
            }
        }

        items(events) { event ->
            EventCard(
                event = event,
                isJoined = joinedEventIds.contains(event.id),
                isFavorite = favoriteEventIds.contains(event.id),
                isLoggedIn = isLoggedIn,
                onLoginClick = onLoginClick,
                onJoinEvent = { onJoinEvent(event) },
                onLeaveEvent = { onLeaveEvent(event.id) },
                onToggleFavorite = { onToggleFavorite(event) }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun EventCard(
    event: Event,
    isJoined: Boolean,
    isFavorite: Boolean,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onJoinEvent: () -> Unit,
    onLeaveEvent: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }

    // Reset loading state when isJoined changes
    LaunchedEffect(isJoined) {
        isLoading = false
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceOffWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with title and favorite button
            Row(
>>>>>>> Stashed changes
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
<<<<<<< Updated upstream
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium, color = TextCharcoal, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${formatScheduleTime(event.schedule)} • ${event.locationLabel}", color = TextCharcoal)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(event.description, style = MaterialTheme.typography.bodyMedium, color = TextCharcoal)
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (isLoggedIn) {
                                onJoinEvent(event.id)
                            } else {
                                onLoginClick()
                            }
                        },
                        enabled = isButtonEnabled,
                        modifier = Modifier.align(Alignment.End)
                    ){
                        Text(buttonText)
=======
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextCharcoal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (isLoggedIn) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Schedule info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = PrimaryTeal
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.schedule,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCharcoal.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Location info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = PrimaryTeal
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.locationLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCharcoal.copy(alpha = 0.8f)
                )
            }

            // Volunteers count
            if (event.maxVolunteers != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryTeal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${event.currentVolunteers}/${event.maxVolunteers} volunteers",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (event.currentVolunteers >= event.maxVolunteers) Color.Red.copy(alpha = 0.8f)
                               else TextCharcoal.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextCharcoal.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            if (!isLoggedIn) {
                OutlinedButton(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login to RSVP")
                }
            } else if (isLoading) {
                // Show loading state
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = PrimaryTeal.copy(alpha = 0.5f),
                        disabledContentColor = Color.White
                    )
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isJoined) "Leaving..." else "Joining...")
                }
            } else if (isJoined) {
                // Already joined - show status and leave option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Joined status badge
                    Surface(
                        color = SecondarySage,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = PrimaryTeal
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Joined",
                                color = PrimaryTeal,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = PrimaryTeal.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Leave button
                    OutlinedButton(
                        onClick = {
                            isLoading = true
                            onLeaveEvent()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red.copy(alpha = 0.8f)
                        )
                    ) {
                        Text("Leave")
                    }
                }
            } else {
                // Not joined - show join button
                val isFull = event.maxVolunteers != null && event.currentVolunteers >= event.maxVolunteers

                Button(
                    onClick = {
                        isLoading = true
                        onJoinEvent()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFull,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    if (isFull) {
                        Text("Event Full")
                    } else {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Join & Get Reminders")
>>>>>>> Stashed changes
                    }
                }
            }
        }
    }
}
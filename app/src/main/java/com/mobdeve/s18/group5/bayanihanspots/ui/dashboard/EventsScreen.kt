package com.mobdeve.s18.group5.bayanihanspots.ui.dashboard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.manage.signups.formatScheduleTime
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.AccentCoral
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PinNatureGreen
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SecondarySage

@Composable
fun EventsScreen(
    state: EventsUiState,
    onRefresh: () -> Unit,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onJoinEvent: (String) -> Unit,
    onLeaveEvent: (String) -> Unit,
    onCheckIn: (Event) -> Unit
) {
    when (state) {
        EventsUiState.Loading -> EventsLoading()
        is EventsUiState.Error -> EventsError(state.message, onRefresh)
        is EventsUiState.Success -> EventsList(
            events = state.events,
            joinedEventIds = state.joinedEventIds,
            isLoggedIn = isLoggedIn,
            onLoginClick = onLoginClick,
            onJoinEvent = onJoinEvent,
            onLeaveEvent = onLeaveEvent,
            onCheckIn = onCheckIn
        )
    }
}

@Composable
private fun EventsLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PrimaryTeal)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Loading events...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun EventsError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AccentCoral.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AccentCoral
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Try Again")
        }
    }
}

@Composable
private fun EventsList(
    events: List<Event>,
    joinedEventIds: Set<String>,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onJoinEvent: (String) -> Unit,
    onLeaveEvent: (String) -> Unit,
    onCheckIn: (Event) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SecondarySage),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Programs & Events",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "${events.size} upcoming opportunities",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Volunteer with nearby barangays, gardens, and community programs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = SecondarySage, thickness = 1.dp)
            }
        }

        // Event Cards
        items(events) { event ->
            Log.d("EVENTS", "Max: "+ event.maxVolunteers)
            Log.d("EVENTS", "Curr: "+ event.currentVolunteers)
            val max = event.maxVolunteers ?: 0
            val current = event.currentVolunteers ?: 0
            val isFull = max > 0 && current >= max
            val isJoined = joinedEventIds.contains(event.id)

            EventCard(
                event = event,
                currentVolunteers = current,
                maxVolunteers = max,
                isJoined = isJoined,
                isFull = isFull,
                isLoggedIn = isLoggedIn,
                onJoinClick = {
                    if (isLoggedIn) onJoinEvent(event.id) else onLoginClick()
                },
                onLeaveClick = {
                    onLeaveEvent(event.id)
                },
                onCheckIn = {
                    onCheckIn(event)
                }
            )
        }

        // Footer spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    currentVolunteers: Int,
    maxVolunteers: Int,
    isJoined: Boolean,
    isFull: Boolean,
    isLoggedIn: Boolean,
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onCheckIn: () -> Unit
) {
    val buttonText = when {
        isJoined -> "Joined"
        isFull -> "Full"
        !isLoggedIn -> "Login to Join"
        else -> "Join Event"
    }
    val isButtonEnabled = !isJoined && !isFull
    val hasLocation = event.coordinates != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title Row with Event Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SecondarySage),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        event.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isJoined) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PinNatureGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "You're signed up!",
                                style = MaterialTheme.typography.labelSmall,
                                color = PinNatureGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Date/Time Pill
                InfoPill(
                    icon = Icons.Default.CalendarMonth,
                    text = formatScheduleTime(event.schedule),
                    modifier = Modifier.weight(1f)
                )
                // Location Pill
                InfoPill(
                    icon = Icons.Default.LocationOn,
                    text = event.locationLabel,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Volunteer Progress (if max is set)
            if (maxVolunteers > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        tint = if (isFull) AccentCoral else PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$currentVolunteers / $maxVolunteers volunteers",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isFull) AccentCoral else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                val progress = if (maxVolunteers > 0) currentVolunteers.toFloat() / maxVolunteers else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isFull) AccentCoral else PrimaryTeal,
                    trackColor = SecondarySage.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            if (isJoined) {
                // Show Check-in button and Cancel button for joined events
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Check-in button (only if event has location)
                    if (hasLocation) {
                        Button(
                            onClick = onCheckIn,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryTeal
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check-in at Event", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Joined status and Cancel row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Joined indicator
                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = PinNatureGreen.copy(alpha = 0.7f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Joined", fontWeight = FontWeight.SemiBold)
                        }

                        // Cancel button
                        OutlinedButton(
                            onClick = onLeaveClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AccentCoral
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(AccentCoral)
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                // Regular join button
                Button(
                    onClick = onJoinClick,
                    enabled = isButtonEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isFull -> MaterialTheme.colorScheme.surfaceVariant
                            else -> PrimaryTeal
                        },
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    if (!isLoggedIn && !isFull) {
                        Icon(
                            Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else if (!isFull) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        buttonText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SecondarySage.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryTeal,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
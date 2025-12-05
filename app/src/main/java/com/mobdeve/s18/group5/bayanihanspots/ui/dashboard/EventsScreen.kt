package com.mobdeve.s18.group5.bayanihanspots.ui.dashboard

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.manage.signups.formatScheduleTime
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SurfaceOffWhite
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.TextCharcoal

@Composable
fun EventsScreen(
    state: EventsUiState,
    onRefresh: () -> Unit,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onJoinEvent: (String) -> Unit
) {
    when (state) {
        EventsUiState.Loading -> EventsLoading()
        is EventsUiState.Error -> EventsError(state.message, onRefresh)
        is EventsUiState.Success -> EventsList(
            events = state.events,
            joinedEventIds = state.joinedEventIds,
            isLoggedIn = isLoggedIn,
            onLoginClick = onLoginClick,
            onJoinEvent = onJoinEvent
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
        CircularProgressIndicator()
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
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onJoinEvent: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceOffWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
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
                    }
                }
            }
        }
    }
}
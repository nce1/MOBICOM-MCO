package com.mobdeve.s18.group5.bayanihanspots.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobdeve.s18.group5.bayanihanspots.data.events.Event
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.SurfaceOffWhite
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.TextCharcoal

@Composable
fun EventsScreen(
    state: EventsUiState,
    onRefresh: () -> Unit,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onJoinEvent: (String) -> Unit = {}
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
    isLoggedIn: Boolean,
    joinedEventIds: Set<String>,
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
                Text(
                    text = "Programs & Events",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextCharcoal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Volunteer with nearby barangays, gardens, and pop-ups.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCharcoal
                )
            }
        }

        items(events) { event ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceOffWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium, color = TextCharcoal)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${event.schedule} • ${event.locationLabel}", color = TextCharcoal)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(event.description, style = MaterialTheme.typography.bodyMedium, color = TextCharcoal)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        if (isLoggedIn) {
                            onJoinEvent(event.id)
                        } else {
                            onLoginClick()
                        }
                    }) {
                        Text(if (isLoggedIn) "Join" else "Login to RSVP")
                    }
                }
            }
        }
    }
}

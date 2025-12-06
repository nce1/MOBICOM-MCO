package com.mobdeve.s18.group5.bayanihanspots.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobdeve.s18.group5.bayanihanspots.auth.data.model.LoggedInUser

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onNotificationsClick: () -> Unit,
    onManageSpotsClick: () -> Unit,
    onManageEventsClick: () -> Unit,
    onManageSignUpsClick: () -> Unit
) {
    val user: LoggedInUser? by viewModel.profile.observeAsState(null)
    val completedCount by viewModel.completedCount.observeAsState("00")
    val activeCount by viewModel.activeCount.observeAsState("00")
    val pendingCount by viewModel.pendingCount.observeAsState("00")
    val logoutComplete by viewModel.logoutComplete.observeAsState(false)
    if (logoutComplete){
        onLogout()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.height(64.dp))

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Picture",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = user?.displayName ?: "Loading...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = user?.email ?: "Loading...",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                ProfileRow(
                    icon = Icons.Default.Edit,
                    title = "Username",
                    subtitle = "@${user?.username ?: "..."}",
                    onClick = { /* TODO: Edit Profile */ }
                )

                Divider(modifier = Modifier.padding(start = 40.dp, top = 4.dp, bottom = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                ProfileRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "View alerts",
                    onClick = onNotificationsClick
                )

                Divider(modifier = Modifier.padding(start = 40.dp, top = 4.dp, bottom = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                ProfileRow(
                    icon = Icons.Default.Place,
                    title = "Manage Spots",
                    subtitle = "Create, Edit",
                    onClick = onManageSpotsClick
                )

                Divider(modifier = Modifier.padding(start = 40.dp, top = 4.dp, bottom = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ProfileRow(
                    icon = Icons.Default.DateRange,
                    title = "Manage Programs",
                    subtitle = "Add, Edit",
                    onClick = onManageEventsClick
                )

                Divider(modifier = Modifier.padding(start = 40.dp, top = 4.dp, bottom = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ProfileRow(
                    icon = Icons.Default.Assignment,
                    title = "Manage Signups",
                    subtitle = "Edit, Cancel",
                    onClick = onManageSignUpsClick
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = { viewModel.signOut() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) { Text("Sign Out") }
    }
}


@Composable
fun ProfileRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit){
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
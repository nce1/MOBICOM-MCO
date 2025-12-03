package com.mobdeve.s18.group5.bayanihanspots.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobdeve.s18.group5.bayanihanspots.R
import com.mobdeve.s18.group5.bayanihanspots.auth.data.model.LoggedInUser

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onLogout: () -> Unit, onManageSpotsClick: () -> Unit){
    val user: LoggedInUser? by viewModel.profile.observeAsState(null)
    val completedCount by viewModel.completedCount.observeAsState("00")
    val activeCount by viewModel.activeCount.observeAsState("00")
    val pendingCount by viewModel.pendingCount.observeAsState("00")
    val logoutComplete by viewModel.logoutComplete.observeAsState(false)
    if (logoutComplete) {
        onLogout()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F7))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_home_black_24dp),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = user?.displayName ?: "Loading...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = user?.email ?: "Loading...",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Completed",
                count = completedCount,
                backgroundColor = Color.White,
                textColor = Color.Black,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Active",
                count = activeCount,
                backgroundColor = Color.White,
                textColor = Color.Black,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pending",
                count = pendingCount,
                backgroundColor = Color.White,
                textColor = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileRow(
                    icon = R.drawable.ic_home_black_24dp,
                    title = "Username",
                    subtitle = "@${user?.username ?: "..."}",
                    onClick = { /* TODO: Implement navigation or action */ }
                )
                Divider(modifier = Modifier.padding(start = 40.dp, top = 4.dp, bottom = 4.dp), color = Color(0xFFF0F0F0))
                ProfileRow(
                    icon = R.drawable.ic_home_black_24dp,
                    title = "Manage Spots",
                    subtitle = "Create, Edit",
                    onClick = onManageSpotsClick
                )
                Divider(modifier = Modifier.padding(start = 40.dp, top = 4.dp, bottom = 4.dp), color = Color(0xFFF0F0F0))
                ProfileRow(
                    icon = R.drawable.ic_home_black_24dp,
                    title = "Manage Programs",
                    subtitle = "Add, Edit",
                    onClick = { /*  */ }
                )
                Divider(modifier = Modifier.padding(start = 40.dp, top = 4.dp, bottom = 4.dp), color = Color(0xFFF0F0F0))
                ProfileRow(
                    icon = R.drawable.ic_home_black_24dp,
                    title = "Manage Signups",
                    subtitle = "Edit, Cancel",
                    onClick = { /*  */ }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(
            onClick = { viewModel.signOut() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB00020)),
            border = BorderStroke(1.dp, Color(0xFFB00020))
        ) { Text("Sign Out") }
    }
}

@Composable
fun StatCard(title: String, count: String, backgroundColor: Color, textColor: Color, modifier: Modifier = Modifier){
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(title, fontSize = 14.sp, color = textColor)
        }
    }
}

@Composable
fun ProfileRow(icon: Int, title: String, subtitle: String, onClick: () -> Unit){
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Gray // Tint for your house icon
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, color = Color.Black)
            Text(subtitle, fontSize = 14.sp, color = Color.Gray)
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Gray
        )
    }
}
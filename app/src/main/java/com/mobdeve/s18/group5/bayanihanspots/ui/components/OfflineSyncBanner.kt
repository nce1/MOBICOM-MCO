package com.mobdeve.s18.group5.bayanihanspots.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobdeve.s18.group5.bayanihanspots.data.sync.PendingUploadManager
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.AccentCoral
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.PrimaryTeal

/**
 * Banner that shows sync status when there are pending uploads.
 * Appears at the top of screens to indicate offline data.
 */
@Composable
fun OfflineSyncBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pendingUploadManager = remember { PendingUploadManager.getInstance(context) }
    val pendingCount by pendingUploadManager.observePendingCount().collectAsState(initial = 0)

    AnimatedVisibility(
        visible = pendingCount > 0,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(AccentCoral.copy(alpha = 0.9f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = "Pending sync",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "$pendingCount item${if (pendingCount > 1) "s" else ""} waiting to sync",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Sync status indicator for use in app bars or other locations.
 */
@Composable
fun SyncStatusIndicator(
    isOnline: Boolean,
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    val icon = when {
        !isOnline -> Icons.Default.CloudOff
        pendingCount > 0 -> Icons.Default.CloudQueue
        else -> Icons.Default.CloudDone
    }

    val tint = when {
        !isOnline -> Color.Gray
        pendingCount > 0 -> AccentCoral
        else -> PrimaryTeal
    }

    val description = when {
        !isOnline -> "Offline"
        pendingCount > 0 -> "$pendingCount pending"
        else -> "Synced"
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        if (pendingCount > 0) {
            Badge(
                containerColor = AccentCoral
            ) {
                Text(
                    text = pendingCount.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}


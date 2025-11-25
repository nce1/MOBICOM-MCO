package com.mobdeve.s18.group5.bayanihanspots.spots

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mobdeve.s18.group5.bayanihanspots.data.spots.Spot
import com.mobdeve.s18.group5.bayanihanspots.ui.theme.*

@Composable
fun SpotDetailsDialog(
    spot: Spot,
    onDismiss: () -> Unit,
    onExpand: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = SurfaceOffWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = spot.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextCharcoal,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DialogBadge(text = spot.type)
                    DialogBadge(text = spot.distanceString.replace(" • ", "").replace("* ", ""))
                    DialogBadge(text = spot.crowdLevel)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = spot.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCharcoal,
                    textAlign = TextAlign.Start,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextCharcoal)
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = onExpand,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text("Expand")
                    }
                }
            }
        }
    }
}

@Composable
fun DialogBadge(text: String) {
    Surface(
        color = SecondarySage.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextCharcoal,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
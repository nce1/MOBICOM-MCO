package com.mobdeve.s18.group5.bayanihanspots.spots

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import com.mobdeve.s18.group5.bayanihanspots.data.review.Review

@Composable
fun EdihfghtReviewDialog(
    review: Review,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(review.rating) }
    var comment by remember { mutableStateOf(review.comment) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Review") },
        text = {
            Column {
                Row(modifier = Modifier.padding(bottom = 8.dp)){
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (star <= rating) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier.size(28.dp).clickable { rating = star }
                        )
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(rating, comment) }){
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


package com.example.currencyconverter_ratewise.ui.screens.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackSheet(
    onDismiss: () -> Unit,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSuccessMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Submit Feedback",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Feedback TextField
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            TextField(
                value = uiState.feedback,
                onValueChange = { viewModel.onFeedbackChange(it) },
                modifier = Modifier.fillMaxSize(),
                placeholder = {
                    Text(
                        "Let us know what you think.",
                        color = Color.Gray
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 10
            )
        }

        // Error message
        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        // Success message
        if (showSuccessMessage) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Email app opened! Thank you for your feedback.",
                color = Color(0xFF4CAF50),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                val success = viewModel.submitFeedback()
                if (success) {
                    showSuccessMessage = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Submit",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
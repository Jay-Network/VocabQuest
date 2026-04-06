package com.jworks.eigojourney.android.ui.feedback

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jworks.eigojourney.android.ui.theme.Glass

@Composable
fun FeedbackFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = Glass.Accent,
        contentColor = Glass.TextPrimary
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Send Feedback"
        )
    }
}
